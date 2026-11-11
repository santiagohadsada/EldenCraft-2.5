package com.tuusuario.mmo.eldencraft.armor.armor;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.classes.ClassManager;
import com.tuusuario.mmo.eldencraft.armor.config.ArmorConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// Punto central para crear y validar armaduras RPG.
// Generacion: stats base por categoria + algunos stats aleatorios extra
// segun rareza. Los bonus por mejora se aplican aparte (StatManager).
public final class ArmorManager {

    private final EldenCraft plugin;
    private final Random random = new Random();

    public ArmorManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // ---- Validacion por clase ----
    public boolean canEquip(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;
        ArmorCategory cat = ArmorCategory.fromMaterial(item.getType());
        if (cat == null) return true; // no es armadura

        ClassManager cm = plugin.classManager();
        String className = cm.getClassName(player);
        ArmorConfig cfg = plugin.armorConfig();
        return cfg.isAllowed(className, cat);
    }

    // ---- Generacion ----
    public ItemStack generate(ArmorType type, ArmorSlot slot, ArmorRarity rarity, int upgradeLevel) {
        Material material = resolveMaterial(type, slot);
        if (material == null) return null;

        ItemStack item = new ItemStack(material);
        List<ArmorStat> stats = rollStats(type, rarity);

        String name = generateName(type, rarity);

        RpgArmor armor = new RpgArmor(type, slot, rarity, upgradeLevel, stats, name);
        armor.apply(item);
        return item;
    }

    public ItemStack generateRandom(int mobLevel) {
        ArmorType[] types = ArmorType.values();
        ArmorSlot[] slots = ArmorSlot.values();
        ArmorRarity rarity = pickWeightedRarity();
        ArmorType type = types[random.nextInt(types.length)];
        ArmorSlot slot = slots[random.nextInt(slots.length)];
        int upgrade = Math.min(plugin.armorConfig().maxUpgradeLevel(), mobLevel / 8);
        return generate(type, slot, rarity, upgrade);
    }

    public ArmorRarity pickWeightedRarity() {
        Map<ArmorRarity, Integer> w = plugin.armorConfig().rarityWeights();
        int total = w.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) return ArmorRarity.COMUN;
        int roll = random.nextInt(total);
        int acc = 0;
        for (Map.Entry<ArmorRarity, Integer> e : w.entrySet()) {
            acc += e.getValue();
            if (roll < acc) return e.getKey();
        }
        return ArmorRarity.COMUN;
    }

    private Material resolveMaterial(ArmorType type, ArmorSlot slot) {
        String prefix = switch (type) {
            case LEATHER   -> "LEATHER";
            case CHAINMAIL -> "CHAINMAIL";
            case IRON      -> "IRON";
            case GOLD      -> "GOLDEN";
            case DIAMOND   -> "DIAMOND";
            case NETHERITE -> "NETHERITE";
        };
        return Material.matchMaterial(prefix + "_" + slot.name());
    }

    // ---- Stats por categoria ----
    private List<ArmorStat> rollStats(ArmorType type, ArmorRarity rarity) {
        List<ArmorStat> result = new ArrayList<>();

        // 1) Stats BASE garantizados segun categoria (lo pidio el doc).
        //    Son pequenos pero siempre estan, asi cada categoria "se siente"
        //    diferente incluso sin mejora.
        switch (type.category()) {
            case LIGERA -> {
                result.add(new ArmorStat(StatType.SPEED_BONUS, 1));
            }
            case INTERMEDIA -> {
                result.add(new ArmorStat(StatType.DEFENSE, 1));
                result.add(new ArmorStat(StatType.EXTRA_HEALTH, 1));
            }
            case PESADA -> {
                result.add(new ArmorStat(StatType.DEFENSE, 3));
                result.add(new ArmorStat(StatType.SPEED_BONUS, -1)); // penalizacion
            }
        }

        // 2) Stats aleatorios extra por rareza
        int count = rarity.minStats() + random.nextInt(Math.max(1, rarity.maxStats() - rarity.minStats() + 1));

        List<StatType> pool = poolFor(type.category());
        Collections.shuffle(pool, random);

        Set<StatType> used = new HashSet<>();
        // los stats base ya estan, no quiero duplicarlos sumando otra vez
        for (ArmorStat s : result) used.add(s.type());

        for (int i = 0; i < pool.size() && used.size() - (result.size()) < count; i++) {
            StatType t = pool.get(i);
            if (!used.add(t)) continue;
            double value = rollValue(t, rarity);
            result.add(new ArmorStat(t, value));
        }
        return result;
    }

    private List<StatType> poolFor(ArmorCategory category) {
        return switch (category) {
            case LIGERA -> new ArrayList<>(Arrays.asList(
                    StatType.CRIT_CHANCE, StatType.SPEED_BONUS,
                    StatType.COOLDOWN_REDUCTION, StatType.LIFESTEAL,
                    StatType.DAMAGE_BONUS));
            case INTERMEDIA -> new ArrayList<>(Arrays.asList(
                    StatType.DAMAGE_BONUS, StatType.DAMAGE_REDUCTION,
                    StatType.EXTRA_HEALTH, StatType.CRIT_CHANCE,
                    StatType.DEFENSE));
            case PESADA -> new ArrayList<>(Arrays.asList(
                    StatType.EXTRA_HEALTH, StatType.DAMAGE_REDUCTION,
                    StatType.REGENERATION, StatType.UNDEAD_DAMAGE,
                    StatType.DEFENSE));
        };
    }

    private double rollValue(StatType type, ArmorRarity rarity) {
        double base = switch (type) {
            case DAMAGE_BONUS, CRIT_CHANCE, SPEED_BONUS,
                 DAMAGE_REDUCTION, COOLDOWN_REDUCTION, LIFESTEAL -> 2 + random.nextInt(5);
            case UNDEAD_DAMAGE -> 4 + random.nextInt(8);
            case EXTRA_HEALTH  -> 1 + random.nextInt(3);
            case REGENERATION  -> 0.5 + random.nextDouble();
            case DEFENSE       -> 1 + random.nextInt(3);
        };
        double mult = switch (rarity) {
            case COMUN      -> 1.0;
            case RARO       -> 1.5;
            case EPICO      -> 2.0;
            case LEGENDARIO -> 2.6;
        };
        double cap = plugin.armorConfig().cap(type);
        return Math.min(cap, Math.round(base * mult * 10.0) / 10.0);
    }

    private String generateName(ArmorType type, ArmorRarity rarity) {
        String[] suffixes = {"Sombria", "Ardiente", "Antigua", "Primordial", "del Vacio",
                             "del Alba", "de las Tormentas", "Espectral", "Sangrienta", "Eterna"};
        String mat = type.name().charAt(0) + type.name().substring(1).toLowerCase();
        return "Armadura de " + mat + " " + suffixes[random.nextInt(suffixes.length)];
    }
}
