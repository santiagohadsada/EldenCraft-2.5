package com.tuusuario.mmo.eldencraft.armor.armor;

import java.util.Objects;

/**
 * Stat individual asociado a una pieza de armadura RPG.
 */
public final class ArmorStat {
    private final StatType type;
    private final double value;

    public ArmorStat(StatType type, double value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    public StatType type()  { return type; }
    public double value()   { return value; }

    public String serialize() {
        return type.name() + ":" + value;
    }

    public static ArmorStat deserialize(String raw) {
        if (raw == null || !raw.contains(":")) return null;
        String[] parts = raw.split(":", 2);
        try {
            return new ArmorStat(StatType.valueOf(parts[0]), Double.parseDouble(parts[1]));
        } catch (Exception ex) {
            return null;
        }
    }
}
