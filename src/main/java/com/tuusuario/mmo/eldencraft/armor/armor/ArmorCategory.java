package com.tuusuario.mmo.eldencraft.armor.armor;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

/**
 * Categorias de armadura segun su peso/RPG.
 */
public enum ArmorCategory {
    LIGERA,
    INTERMEDIA,
    PESADA;

    private static final Set<Material> LIGHT = EnumSet.of(
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
    );

    private static final Set<Material> MEDIUM = EnumSet.of(
            Material.IRON_HELMET, Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS
    );

    private static final Set<Material> HEAVY = EnumSet.of(
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
    );

    public static ArmorCategory fromMaterial(Material material) {
        if (material == null) return null;
        if (LIGHT.contains(material)) return LIGERA;
        if (MEDIUM.contains(material)) return INTERMEDIA;
        if (HEAVY.contains(material)) return PESADA;
        return null;
    }

    public static boolean isArmor(Material material) {
        return fromMaterial(material) != null;
    }
}
