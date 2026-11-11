package com.tuusuario.mmo.eldencraft.models;
// aca estan las clases del juego con sus stats base y si usan mana o energia
// F1: añadido campo hitDie (dado de golpe D&D 5e SRD)
public enum ClassType {
    // va: Nombre, STR, DEX, CON, INT, WIS, CHA, recurso, hitDie
    WARRIOR  ("Guerrero",    3,  1,  2,  0,  0, -1, "ENERGIA", 10),
    MAGE     ("Mago",       -1,  1, -1,  3,  2,  1, "MANA",     6),
    RANGER   ("Explorador",  1,  3,  1,  0,  2,  0, "ENERGIA", 10),
    PALADIN  ("Paladín",     2,  1,  2,  0,  3,  2, "MANA",    10),
    ROGUE    ("Ladrón",     -1,  3,  0,  2,  1,  2, "ENERGIA",  8),
    BARD     ("Bardo",       0,  2,  1,  2,  1,  3, "MANA",     8),
    BARBARIAN("Bárbaro",     4,  0,  3, -1,  0, -2, "ENERGIA", 12),
    // ── las clases nuevas q metimos despues ─────────────────────────────────
    SORCERER ("Hechicero",  -1,  1, -1,  4,  1,  2, "MANA",     6),
    WARLOCK  ("Brujo",       0,  0,  0,  3,  0,  3, "MANA",     8),
    MONK     ("Monje",       2,  3,  1,  0,  2,  0, "ENERGIA",  8),
    DRUID    ("Druida",      0,  1,  2,  2,  3,  0, "MANA",     8),
    CLERIC   ("Clérigo",     1,  0,  2,  1,  3,  2, "MANA",     8),
    GUNSLINGER("Pistolero",  0,  4,  1,  1,  1,  1, "ENERGIA",  8); // # VERIFICAR: hitDie Pistolero custom

    public final String displayName;
    public final int bStr, bDex, bCon, bInt, bWis, bCha;
    public final String resourceType; // si usa mana o energia
    public final int hitDie;          // dado de golpe (d6, d8, d10, d12) — SRD 5.1

    ClassType(String displayName, int bStr, int bDex, int bCon, int bInt, int bWis, int bCha,
              String resourceType, int hitDie) {
        this.displayName = displayName;
        this.bStr = bStr;
        this.bDex = bDex;
        this.bCon = bCon;
        this.bInt = bInt;
        this.bWis = bWis;
        this.bCha = bCha;
        this.resourceType = resourceType;
        this.hitDie = hitDie;
    }

    /** Devuelve si la clase es lanzadora de conjuros D&D 5e. */
    public boolean isCaster() {
        return switch (this) {
            case MAGE, SORCERER, WARLOCK, CLERIC, DRUID, BARD, PALADIN, RANGER -> true;
            default -> false;
        };
    }

    /** Devuelve el nombre de clase en minúscula para comparaciones con SpellDefinition.clases. */
    public String spellKey() {
        return switch (this) {
            case WARRIOR   -> "warrior";
            case MAGE      -> "mage";
            case RANGER    -> "ranger";
            case PALADIN   -> "paladin";
            case ROGUE     -> "rogue";
            case BARD      -> "bard";
            case BARBARIAN -> "barbarian";
            case SORCERER  -> "sorcerer";
            case WARLOCK   -> "warlock";
            case MONK      -> "monk";
            case DRUID     -> "druid";
            case CLERIC    -> "cleric";
            case GUNSLINGER-> "gunslinger";
        };
    }
}
