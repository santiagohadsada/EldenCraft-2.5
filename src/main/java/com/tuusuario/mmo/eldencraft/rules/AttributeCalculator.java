package com.tuusuario.mmo.eldencraft.rules;

/**
 * Calculadora de atributos y bonificadores SRD 5.1.
 */
public class AttributeCalculator {

    private AttributeCalculator() {}

    /**
     * Calcula el modificador de atributo: floor((valor - 10) / 2).
     * Ejemplos: 10 → 0, 15 → +2, 8 → -1, 20 → +5.
     */
    public static int modifier(int value) {
        return (int) Math.floor((value - 10) / 2.0);
    }

    /**
     * Bonificador de competencia por nivel de personaje (tabla SRD 5.1):
     * Niv 1-4 → +2 | 5-8 → +3 | 9-12 → +4 | 13-16 → +5 | 17-20 → +6
     */
    public static int proficiencyBonus(int level) {
        if (level <= 4)  return 2;
        if (level <= 8)  return 3;
        if (level <= 12) return 4;
        if (level <= 16) return 5;
        return 6; // 17-20
    }

    /**
     * CD de salvación de conjuro: 8 + bonif. competencia + modificador del atributo de lanzamiento.
     */
    public static int spellSaveDC(int profBonus, int spellcastingMod) {
        return 8 + profBonus + spellcastingMod;
    }

    /**
     * Bono de ataque de conjuro: bonif. competencia + modificador del atributo de lanzamiento.
     */
    public static int spellAttackBonus(int profBonus, int spellcastingMod) {
        return profBonus + spellcastingMod;
    }
}
