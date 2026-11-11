package com.tuusuario.mmo.eldencraft.armor.armor;

import com.tuusuario.mmo.eldencraft.armor.util.Keys;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// modelo de una pieza rpg en memoria
// los datos se meten y sacan del item con PersistentDataContainer
// y se muestran en el lore pa q el jugador los vea
public final class RpgArmor {

    private final ArmorType type;
    private final ArmorSlot slot;
    private final ArmorRarity rarity;
    private final int upgradeLevel;
    private final List<ArmorStat> stats;
    private final String customName;
    private final ArmorType reinforcement; // material secundario (puede ser null)

    public RpgArmor(ArmorType type,
                    ArmorSlot slot,
                    ArmorRarity rarity,
                    int upgradeLevel,
                    List<ArmorStat> stats,
                    String customName) {
        this(type, slot, rarity, upgradeLevel, stats, customName, null);
    }

    public RpgArmor(ArmorType type,
                    ArmorSlot slot,
                    ArmorRarity rarity,
                    int upgradeLevel,
                    List<ArmorStat> stats,
                    String customName,
                    ArmorType reinforcement) {
        this.type = type;
        this.slot = slot;
        this.rarity = rarity;
        this.upgradeLevel = Math.max(0, upgradeLevel);
        this.stats = stats == null ? new ArrayList<>() : new ArrayList<>(stats);
        this.customName = customName;
        this.reinforcement = reinforcement;
    }

    public ArmorType type()         { return type; }
    public ArmorSlot slot()         { return slot; }
    public ArmorCategory category() { return type.category(); }
    public ArmorRarity rarity()     { return rarity; }
    public int upgradeLevel()       { return upgradeLevel; }
    public List<ArmorStat> stats()  { return Collections.unmodifiableList(stats); }
    public String customName()      { return customName; }
    public ArmorType reinforcement(){ return reinforcement; }

    // diferencia de tier entre el refuerzo y el material base
    // si no hay refuerzo devolvemos 0 pa q no se rompa
    public int tierDiff() {
        if (reinforcement == null) return 0;
        return reinforcement.tier() - type.tier();
    }

    // bonus del refuerzo segun la categoria
    // estos numeros los sacamos del doc de diseño
    // esto devuelve el bonus por nivel de mejora
    // el StatManager despues lo multiplica por el nivel
    public Map<StatType, Double> reinforcementBonusPerLevel() {
        Map<StatType, Double> out = new EnumMap<>(StatType.class);
        if (reinforcement == null || upgradeLevel <= 0) return out;

        int diff = tierDiff();
        switch (type.category()) {
            case LIGERA:
                if (diff <= 0)        { put(out, StatType.SPEED_BONUS, 1); }
                else if (diff == 1)   { put(out, StatType.SPEED_BONUS, 2); put(out, StatType.CRIT_CHANCE, 1); }
                else if (diff == 2)   { put(out, StatType.SPEED_BONUS, 4); put(out, StatType.CRIT_CHANCE, 2); }
                else if (diff == 3)   { put(out, StatType.SPEED_BONUS, 6); put(out, StatType.CRIT_CHANCE, 3); put(out, StatType.EXTRA_HEALTH, 1); }
                else                  { put(out, StatType.SPEED_BONUS, 8); put(out, StatType.CRIT_CHANCE, 4); put(out, StatType.EXTRA_HEALTH, 2); }
                break;

            case INTERMEDIA:
                if (diff <= 0)        { put(out, StatType.DEFENSE, 1); }
                else if (diff == 1)   { put(out, StatType.DEFENSE, 2); put(out, StatType.EXTRA_HEALTH, 1); }
                else if (diff == 2)   { put(out, StatType.DEFENSE, 4); put(out, StatType.EXTRA_HEALTH, 2); }
                else if (diff == 3)   { put(out, StatType.DEFENSE, 5); put(out, StatType.EXTRA_HEALTH, 3); put(out, StatType.DAMAGE_REDUCTION, 1); }
                else                  { put(out, StatType.DEFENSE, 6); put(out, StatType.EXTRA_HEALTH, 4); put(out, StatType.DAMAGE_REDUCTION, 2); }
                break;

            case PESADA:
                if (diff <= 0)        { put(out, StatType.DEFENSE, 2);  put(out, StatType.SPEED_BONUS, -1); }
                else if (diff == 1)   { put(out, StatType.DEFENSE, 4);  put(out, StatType.EXTRA_HEALTH, 2); put(out, StatType.SPEED_BONUS, -1); }
                else if (diff == 2)   { put(out, StatType.DEFENSE, 6);  put(out, StatType.EXTRA_HEALTH, 3); put(out, StatType.SPEED_BONUS, -2); }
                else if (diff == 3)   { put(out, StatType.DEFENSE, 8);  put(out, StatType.EXTRA_HEALTH, 4); put(out, StatType.SPEED_BONUS, -3); }
                else                  { put(out, StatType.DEFENSE, 10); put(out, StatType.EXTRA_HEALTH, 5); put(out, StatType.SPEED_BONUS, -4); }
                break;
        }
        return out;
    }

    // bonus total = bonus por nivel * nivel actual
    public Map<StatType, Double> totalReinforcementBonus() {
        Map<StatType, Double> per = reinforcementBonusPerLevel();
        Map<StatType, Double> out = new EnumMap<>(StatType.class);
        for (Map.Entry<StatType, Double> e : per.entrySet()) {
            out.put(e.getKey(), e.getValue() * upgradeLevel);
        }
        return out;
    }

    private static void put(Map<StatType, Double> m, StatType t, double v) {
        m.put(t, v);
    }

    // penalizacion si le metes un refuerzo muy groso a un item basura
    // si la diferencia de tiers es >= 3
    // el item se pone "fragil" y pierde algo de salud y velocidad.
    // se aplica al final nomas
    public boolean isOverforged() {
        return reinforcement != null && tierDiff() >= 3;
    }

    public void apply(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.RPG_FLAG, PersistentDataType.BYTE, (byte) 1);
        pdc.set(Keys.TYPE,     PersistentDataType.STRING, type.name());
        pdc.set(Keys.SLOT,     PersistentDataType.STRING, slot.name());
        pdc.set(Keys.RARITY,   PersistentDataType.STRING, rarity.name());
        pdc.set(Keys.UPGRADE,  PersistentDataType.INTEGER, upgradeLevel);
        pdc.set(Keys.STATS,    PersistentDataType.STRING,
                stats.stream().map(ArmorStat::serialize).collect(Collectors.joining(";")));
        if (reinforcement != null) {
            pdc.set(Keys.REINFORCEMENT, PersistentDataType.STRING, reinforcement.name());
        } else {
            pdc.remove(Keys.REINFORCEMENT);
        }

        meta.setDisplayName(rarity.color() + (customName != null ? customName : defaultName()));
        meta.setLore(buildLore());
        item.setItemMeta(meta);
    }

    private String defaultName() {
        String mat = type.name().charAt(0) + type.name().substring(1).toLowerCase();
        return mat + " RPG";
    }

    private List<String> buildLore() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + type.category().name());
        lore.add(ChatColor.GRAY + "Material: " + ChatColor.WHITE + type.name());
        lore.add(ChatColor.GRAY + "Rareza: " + rarity.color() + rarity.displayName());

        if (upgradeLevel > 0) {
            String tag = ChatColor.YELLOW + "+" + upgradeLevel;
            if (reinforcement != null) {
                tag += ChatColor.DARK_GRAY + " [" + ChatColor.WHITE + reinforcement.name() + ChatColor.DARK_GRAY + "]";
            }
            lore.add(ChatColor.GRAY + "Mejora: " + tag);
        }

        if (isOverforged()) {
            lore.add(ChatColor.DARK_RED + "* Sobreforjada (fragil)");
        }

        // stats base q se generaron al azar
        if (!stats.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "-- Atributos base --");
            for (ArmorStat s : stats) {
                String prefix = s.type().offensive() ? ChatColor.RED.toString() : ChatColor.GREEN.toString();
                lore.add(prefix + signed(s.value()) + " " + s.type().displayName());
            }
        }

        // bonus q da la mejora del yunque
        Map<StatType, Double> bonus = totalReinforcementBonus();
        if (!bonus.isEmpty()) {
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "-- Refuerzo --");
            for (Map.Entry<StatType, Double> e : bonus.entrySet()) {
                ChatColor color = e.getValue() < 0 ? ChatColor.RED : ChatColor.AQUA;
                lore.add(color + signed(e.getValue()) + " " + e.getKey().displayName());
            }
        }
        return lore;
    }

    private String signed(double v) {
        String prefix = v >= 0 ? "+" : "";
        if (v == Math.floor(v)) return prefix + (int) v;
        return prefix + String.format("%.1f", v);
    }

    // --- leer datos de un item existente ---

    public static boolean isRpgArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        Byte flag = pdc.get(Keys.RPG_FLAG, PersistentDataType.BYTE);
        return flag != null && flag == 1;
    }

    public static RpgArmor read(ItemStack item) {
        if (!isRpgArmor(item)) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        ArmorType type   = parseEnum(ArmorType.class, pdc.get(Keys.TYPE, PersistentDataType.STRING));
        ArmorSlot slot   = parseEnum(ArmorSlot.class, pdc.get(Keys.SLOT, PersistentDataType.STRING));
        ArmorRarity rar  = ArmorRarity.fromString(pdc.get(Keys.RARITY, PersistentDataType.STRING));
        Integer up       = pdc.get(Keys.UPGRADE, PersistentDataType.INTEGER);
        String statsRaw  = pdc.get(Keys.STATS, PersistentDataType.STRING);
        ArmorType reinf  = parseEnum(ArmorType.class, pdc.get(Keys.REINFORCEMENT, PersistentDataType.STRING));

        if (type == null || slot == null) return null;

        List<ArmorStat> stats = new ArrayList<>();
        if (statsRaw != null && !statsRaw.isEmpty()) {
            for (String token : statsRaw.split(";")) {
                ArmorStat s = ArmorStat.deserialize(token);
                if (s != null) stats.add(s);
            }
        }

        String displayName = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : null;
        return new RpgArmor(type, slot, rar, up == null ? 0 : up, stats, displayName, reinf);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> clazz, String name) {
        if (name == null) return null;
        try { return Enum.valueOf(clazz, name); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
