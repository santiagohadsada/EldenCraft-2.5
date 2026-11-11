package com.tuusuario.mmo.eldencraft.armor.armor;

import org.bukkit.Material;

public enum ArmorSlot {
    HELMET, CHESTPLATE, LEGGINGS, BOOTS;

    public static ArmorSlot fromMaterial(Material material) {
        if (material == null) return null;
        String n = material.name();
        if (n.endsWith("_HELMET")) return HELMET;
        if (n.endsWith("_CHESTPLATE")) return CHESTPLATE;
        if (n.endsWith("_LEGGINGS")) return LEGGINGS;
        if (n.endsWith("_BOOTS")) return BOOTS;
        return null;
    }
}
