package com.tuusuario.mmo.eldencraft.armor.armor;

import org.bukkit.ChatColor;

public enum ArmorRarity {
    COMUN(ChatColor.WHITE,    "Comun",      1, 1),
    RARO(ChatColor.AQUA,      "Raro",       2, 2),
    EPICO(ChatColor.LIGHT_PURPLE, "Epico",  3, 3),
    LEGENDARIO(ChatColor.GOLD,    "Legendario", 4, 5);

    private final ChatColor color;
    private final String displayName;
    private final int minStats;
    private final int maxStats;

    ArmorRarity(ChatColor color, String displayName, int minStats, int maxStats) {
        this.color = color;
        this.displayName = displayName;
        this.minStats = minStats;
        this.maxStats = maxStats;
    }

    public ChatColor color() { return color; }
    public String displayName() { return displayName; }
    public int minStats() { return minStats; }
    public int maxStats() { return maxStats; }

    public static ArmorRarity fromString(String s) {
        if (s == null) return COMUN;
        try { return ArmorRarity.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException ex) { return COMUN; }
    }
}
