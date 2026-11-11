package com.tuusuario.mmo.eldencraft.rules;

/**
 * Las 18 habilidades D&D 5e SRD con su atributo asociado.
 */
public enum SkillDefinition {
    ACROBATICS    ("Acrobacias",          Attribute.DEX),
    ANIMAL_HANDLING("Trato con animales", Attribute.WIS),
    ARCANA        ("Arcanos",             Attribute.INT),
    ATHLETICS     ("Atletismo",           Attribute.STR),
    DECEPTION     ("Engaño",              Attribute.CHA),
    HISTORY       ("Historia",            Attribute.INT),
    INSIGHT       ("Perspicacia",         Attribute.WIS),
    INTIMIDATION  ("Intimidación",        Attribute.CHA),
    INVESTIGATION ("Investigación",       Attribute.INT),
    MEDICINE      ("Medicina",            Attribute.WIS),
    NATURE        ("Naturaleza",          Attribute.INT),
    PERCEPTION    ("Percepción",          Attribute.WIS),
    PERFORMANCE   ("Interpretación",      Attribute.CHA),
    PERSUASION    ("Persuasión",          Attribute.CHA),
    RELIGION      ("Religión",            Attribute.INT),
    SLEIGHT_OF_HAND("Juego de manos",     Attribute.DEX),
    STEALTH       ("Sigilo",              Attribute.DEX),
    SURVIVAL      ("Supervivencia",       Attribute.WIS);

    public final String nombre;
    public final Attribute attribute;

    SkillDefinition(String nombre, Attribute attribute) {
        this.nombre = nombre;
        this.attribute = attribute;
    }

    /** Busca por nombre parcial (insensible a mayúsculas) o nombre enum. */
    public static SkillDefinition fromString(String s) {
        for (SkillDefinition sd : values()) {
            if (sd.name().equalsIgnoreCase(s) || sd.nombre.equalsIgnoreCase(s)) return sd;
        }
        return null;
    }
}
