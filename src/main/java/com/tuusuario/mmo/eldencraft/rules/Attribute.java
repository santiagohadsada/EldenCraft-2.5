package com.tuusuario.mmo.eldencraft.rules;

/**
 * Los seis atributos del SRD 5.1.
 */
public enum Attribute {
    STR("Fuerza"),
    DEX("Destreza"),
    CON("Constitución"),
    INT("Inteligencia"),
    WIS("Sabiduría"),
    CHA("Carisma");

    public final String nombre;

    Attribute(String nombre) {
        this.nombre = nombre;
    }

    public static Attribute fromString(String str) {
        if (str == null) return null;
        String s = str.trim().toUpperCase();
        for (Attribute a : values()) {
            if (a.name().equalsIgnoreCase(s) || a.nombre.equalsIgnoreCase(s)) {
                return a;
            }
        }
        if (s.startsWith("FUE") || s.startsWith("STR")) return STR;
        if (s.startsWith("DES") || s.startsWith("DEX")) return DEX;
        if (s.startsWith("CON")) return CON;
        if (s.startsWith("INT")) return INT;
        if (s.startsWith("SAB") || s.startsWith("WIS")) return WIS;
        if (s.startsWith("CAR") || s.startsWith("CHA")) return CHA;
        return null;
    }
}
