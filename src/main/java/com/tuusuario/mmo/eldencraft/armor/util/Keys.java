package com.tuusuario.mmo.eldencraft.armor.util;

import org.bukkit.NamespacedKey;

// Keys que uso para guardar info en los items con PersistentDataContainer.
// (es mas robusto que parsear el lore: el lore se rompe si lo tocan o
//  si otro plugin lo modifica)
public final class Keys {

    private static final String NS = "eldencraft";

    public static final NamespacedKey RPG_FLAG      = new NamespacedKey(NS, "rpg_armor");
    public static final NamespacedKey TYPE          = new NamespacedKey(NS, "armor_type");
    public static final NamespacedKey SLOT          = new NamespacedKey(NS, "armor_slot");
    public static final NamespacedKey RARITY        = new NamespacedKey(NS, "armor_rarity");
    public static final NamespacedKey UPGRADE       = new NamespacedKey(NS, "armor_upgrade");
    public static final NamespacedKey STATS         = new NamespacedKey(NS, "armor_stats");

    // Refuerzo del yunque (material secundario que se uso para mejorar la pieza).
    // Guardo el nombre del enum ArmorType.
    public static final NamespacedKey REINFORCEMENT = new NamespacedKey(NS, "armor_reinf");

    private Keys() {}
}
