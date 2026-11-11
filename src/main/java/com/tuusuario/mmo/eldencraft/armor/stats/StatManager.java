package com.tuusuario.mmo.eldencraft.armor.stats;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorCategory;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorStat;
import com.tuusuario.mmo.eldencraft.armor.armor.RpgArmor;
import com.tuusuario.mmo.eldencraft.armor.armor.StatType;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// Suma todos los stats de las 4 piezas equipadas, los cachea, y se encarga
// de aplicar los que necesitan tocar atributos del jugador (vida max y velocidad).
// Tambien hace el calculo de dano saliente/entrante para CombatListener.
public final class StatManager {

    private final EldenCraft plugin;
    private final Random random = new Random();
    private BukkitTask task;

    private final Map<UUID, Map<StatType, Double>> cache = new HashMap<>();
    private final Map<UUID, Double> baseHealthCache = new HashMap<>();

    public StatManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void startPassiveTask() {
        // cada 2s: regen + reaplicacion suave
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 40L);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        // restauro la vida maxima de los que estan online ahora mismo
        for (Player p : Bukkit.getOnlinePlayers()) clear(p);
    }

    // ---- Recalculo ----
    public Map<StatType, Double> recalc(Player player) {
        Map<StatType, Double> totals = new EnumMap<>(StatType.class);
        EntityEquipment eq = player.getEquipment();
        if (eq == null) {
            cache.put(player.getUniqueId(), totals);
            return totals;
        }

        ItemStack[] pieces = {eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots()};
        for (ItemStack piece : pieces) {
            if (!RpgArmor.isRpgArmor(piece)) continue;
            RpgArmor armor = RpgArmor.read(piece);
            if (armor == null) continue;

            // 1) stats base de la pieza
            for (ArmorStat s : armor.stats()) {
                totals.merge(s.type(), s.value(), Double::sum);
            }
            // 2) bonus del refuerzo (puede tener valores negativos en pesada)
            Map<StatType, Double> bonus = armor.totalReinforcementBonus();
            for (Map.Entry<StatType, Double> b : bonus.entrySet()) {
                totals.merge(b.getKey(), b.getValue(), Double::sum);
            }
            // 3) penalizacion si la pieza esta sobreforjada
            //    (la armadura "pesa" demasiado para su material base)
            if (armor.isOverforged()) {
                totals.merge(StatType.SPEED_BONUS, -2.0, Double::sum);
                totals.merge(StatType.EXTRA_HEALTH, -1.0, Double::sum);
            }
        }

        // Aplicar caps globales
        for (StatType t : totals.keySet()) {
            double v = totals.get(t);
            // permito valores negativos (penalizacion en pesadas), no aplico cap
            // por abajo. Por arriba si.
            double cap = plugin.armorConfig().cap(t);
            if (v > cap) totals.put(t, cap);
        }

        cache.put(player.getUniqueId(), totals);
        return totals;
    }

    public double get(Player player, StatType type) {
        Map<StatType, Double> totals = cache.get(player.getUniqueId());
        if (totals == null) totals = recalc(player);
        return totals.getOrDefault(type, 0.0);
    }

    // Llamar cuando cambia el equipo del jugador.
    public void onEquipmentChange(Player player) {
        recalc(player);
        applyExtraHealth(player);
        applySpeed(player);
    }

    public void clear(Player player) {
        cache.remove(player.getUniqueId());
        Double base = baseHealthCache.remove(player.getUniqueId());
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHp != null && base != null) {
            maxHp.setBaseValue(base);
            // por seguridad, si el player tenia mas vida que el base, lo recorto
            if (player.getHealth() > base) player.setHealth(base);
        }
        // velocidad: vuelvo a la default
        player.setWalkSpeed(0.2f);
    }

    // ---- Tick pasivo ----
    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            double regen = get(p, StatType.REGENERATION);
            if (regen > 0 && p.getHealth() > 0 && p.getHealth() < p.getMaxHealth()) {
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + regen));
            }
        }
    }

    // ---- Atributos ----
    public void applyExtraHealth(Player player) {
        AttributeInstance maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHp == null) return;
        double base = baseHealthCache.computeIfAbsent(player.getUniqueId(), k -> maxHp.getBaseValue());
        double extra = get(player, StatType.EXTRA_HEALTH);
        double newMax = Math.max(1.0, base + extra);
        maxHp.setBaseValue(newMax);
        // si la vida actual quedo por encima del nuevo max, ajustar
        if (player.getHealth() > newMax) player.setHealth(newMax);
    }

    public void applySpeed(Player player) {
        // los stats devuelven % positivo o negativo
        double speedPct = get(player, StatType.SPEED_BONUS) / 100.0;
        // clamp para que el slider de WalkSpeed admita el valor (vanilla: 0..1)
        float v = (float) Math.max(0.05, Math.min(1.0, 0.2 * (1.0 + speedPct)));
        player.setWalkSpeed(v);
    }

    // ---- Dano combate ----
    public double computeOutgoingDamage(Player attacker, LivingEntity victim, double baseDamage) {
        double dmg = baseDamage;
        double bonus = get(attacker, StatType.DAMAGE_BONUS) / 100.0;
        dmg *= (1.0 + bonus);

        if (isUndead(victim)) {
            double undead = get(attacker, StatType.UNDEAD_DAMAGE);
            dmg += undead;
        }

        double crit = get(attacker, StatType.CRIT_CHANCE);
        if (crit > 0 && random.nextDouble() * 100.0 < crit) {
            dmg *= 1.5;
        }

        double lifesteal = get(attacker, StatType.LIFESTEAL) / 100.0;
        if (lifesteal > 0) {
            attacker.setHealth(Math.min(attacker.getMaxHealth(),
                    attacker.getHealth() + dmg * lifesteal));
        }
        return dmg;
    }

    public double computeIncomingDamage(Player victim, double baseDamage) {
        // primero defensa plana
        double flat = get(victim, StatType.DEFENSE);
        double dmg = Math.max(0, baseDamage - flat);
        // luego porcentual (cap absoluto del 90% para que no quede inmune del todo)
        double red = get(victim, StatType.DAMAGE_REDUCTION) / 100.0;
        red = Math.min(0.9, red);
        return dmg * (1.0 - red);
    }

    private boolean isUndead(LivingEntity e) {
        if (e == null) return false;
        switch (e.getType()) {
            case ZOMBIE: case ZOMBIE_VILLAGER: case ZOMBIE_HORSE: case ZOMBIFIED_PIGLIN:
            case HUSK: case DROWNED: case SKELETON: case STRAY: case WITHER_SKELETON:
            case WITHER: case PHANTOM:
                return true;
            default:
                return false;
        }
    }

    // Penalizacion del anti-exploit (si por algun resquicio se equipo algo no permitido).
    public void applyInvalidPenalty(Player player) {
        if (!plugin.armorConfig().applyEffectsOnInvalid()) return;
        int slow = plugin.armorConfig().slownessAmplifier();
        int weak = plugin.armorConfig().weaknessAmplifier();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, slow, true, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, weak, true, false));
        // Daño por armadura prohibida eliminado por diseño:
        // no queremos castigar al jugador con daño, solo impedirle equiparla.
    }

    // helper publico opcional para debug
    public Map<StatType, Double> snapshot(Player p) {
        Map<StatType, Double> map = cache.get(p.getUniqueId());
        return map == null ? new EnumMap<>(StatType.class) : new EnumMap<>(map);
    }

    // un extra: por si quiero detectar la categoria predominante del set
    public ArmorCategory dominantCategory(Player p) {
        EntityEquipment eq = p.getEquipment();
        if (eq == null) return null;
        Map<ArmorCategory, Integer> count = new EnumMap<>(ArmorCategory.class);
        for (ItemStack piece : new ItemStack[]{eq.getHelmet(), eq.getChestplate(), eq.getLeggings(), eq.getBoots()}) {
            RpgArmor a = RpgArmor.read(piece);
            if (a != null) count.merge(a.category(), 1, Integer::sum);
        }
        ArmorCategory best = null; int max = 0;
        for (Map.Entry<ArmorCategory, Integer> e : count.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); best = e.getKey(); }
        }
        return best;
    }
}
