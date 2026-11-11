package com.tuusuario.mmo.eldencraft.armor.armor;

// Stats RPG. Algunos son ofensivos (van al damage saliente) y otros defensivos.
// Anadi DEFENSE como stat plano (resta dano) ademas del % DAMAGE_REDUCTION,
// porque el sistema de mejoras del yunque trabaja con "+defensa" en numero plano.
public enum StatType {

    DAMAGE_BONUS("% Dano", true),
    CRIT_CHANCE("% Critico", true),
    UNDEAD_DAMAGE("Dano a no muertos", true),
    LIFESTEAL("Lifesteal", true),

    EXTRA_HEALTH("Vida extra", false),
    DEFENSE("Defensa", false),                      // plano (resta puntos al dano recibido)
    DAMAGE_REDUCTION("% Reduccion de dano", false), // %
    REGENERATION("Regeneracion", false),

    SPEED_BONUS("% Velocidad", false),
    COOLDOWN_REDUCTION("% Cooldown reducido", false);

    private final String displayName;
    private final boolean offensive;

    StatType(String displayName, boolean offensive) {
        this.displayName = displayName;
        this.offensive = offensive;
    }

    public String displayName() { return displayName; }
    public boolean offensive() { return offensive; }
}
