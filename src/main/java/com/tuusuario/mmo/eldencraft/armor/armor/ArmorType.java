package com.tuusuario.mmo.eldencraft.armor.armor;

import org.bukkit.Material;

// Material base de la pieza. NO cambia con las mejoras.
// (la mejora solo guarda un material secundario como "refuerzo")
public enum ArmorType {

    LEATHER  (ArmorCategory.LIGERA,     1),
    CHAINMAIL(ArmorCategory.LIGERA,     2),
    IRON     (ArmorCategory.INTERMEDIA, 3),
    GOLD     (ArmorCategory.INTERMEDIA, 3),
    DIAMOND  (ArmorCategory.PESADA,     4),
    NETHERITE(ArmorCategory.PESADA,     5);

    private final ArmorCategory category;
    private final int tier;

    ArmorType(ArmorCategory category, int tier) {
        this.category = category;
        this.tier = tier;
    }

    public ArmorCategory category() { return category; }
    public int tier() { return tier; }

    public static ArmorType fromMaterial(Material material) {
        if (material == null) return null;
        String name = material.name();
        if (name.startsWith("LEATHER_"))   return LEATHER;
        if (name.startsWith("CHAINMAIL_")) return CHAINMAIL;
        if (name.startsWith("IRON_"))      return IRON;
        if (name.startsWith("GOLDEN_"))    return GOLD;
        if (name.startsWith("DIAMOND_"))   return DIAMOND;
        if (name.startsWith("NETHERITE_")) return NETHERITE;
        return null;
    }

    // Para detectar el material "lingote/cuero" suelto que se usa como refuerzo
    // en el yunque, no la pieza completa.
    public static ArmorType fromIngredient(Material m) {
        if (m == null) return null;
        switch (m) {
            case LEATHER:           return LEATHER;
            case CHAIN:             return CHAINMAIL; // por si acaso
            case IRON_INGOT:        return IRON;
            case GOLD_INGOT:        return GOLD;
            case DIAMOND:           return DIAMOND;
            case NETHERITE_INGOT:   return NETHERITE;
            default: return null;
        }
    }
}
