package com.tuusuario.mmo.eldencraft.spells;

import com.tuusuario.mmo.eldencraft.models.ClassType;

/**
 * Tablas de espacios de conjuro SRD 5.1 para lanzadoras completas, semilanzadoras y Brujo (Pact Magic).
 * # VERIFICAR: tablas completas de espacios según SRD 5.1
 */
public class SpellSlotTable {

    private SpellSlotTable() {}

    // Lanzadoras completas: Mago, Brujo (slots propios), Clérigo, Druida, Bardo, Hechicero
    // Índice 0..19 (nivel 1..20), columnas: espacios de nivel 1..9
    private static final int[][] FULL_CASTER_SLOTS = {
        //  1  2  3  4  5  6  7  8  9
        { 2, 0, 0, 0, 0, 0, 0, 0, 0 }, // 1
        { 3, 0, 0, 0, 0, 0, 0, 0, 0 }, // 2
        { 4, 2, 0, 0, 0, 0, 0, 0, 0 }, // 3
        { 4, 3, 0, 0, 0, 0, 0, 0, 0 }, // 4
        { 4, 3, 2, 0, 0, 0, 0, 0, 0 }, // 5
        { 4, 3, 3, 0, 0, 0, 0, 0, 0 }, // 6
        { 4, 3, 3, 1, 0, 0, 0, 0, 0 }, // 7
        { 4, 3, 3, 2, 0, 0, 0, 0, 0 }, // 8
        { 4, 3, 3, 3, 1, 0, 0, 0, 0 }, // 9
        { 4, 3, 3, 3, 2, 0, 0, 0, 0 }, // 10
        { 4, 3, 3, 3, 2, 1, 0, 0, 0 }, // 11
        { 4, 3, 3, 3, 2, 1, 0, 0, 0 }, // 12
        { 4, 3, 3, 3, 2, 1, 1, 0, 0 }, // 13
        { 4, 3, 3, 3, 2, 1, 1, 0, 0 }, // 14
        { 4, 3, 3, 3, 2, 1, 1, 1, 0 }, // 15
        { 4, 3, 3, 3, 2, 1, 1, 1, 0 }, // 16
        { 4, 3, 3, 3, 2, 1, 1, 1, 1 }, // 17
        { 4, 3, 3, 3, 3, 1, 1, 1, 1 }, // 18
        { 4, 3, 3, 3, 3, 2, 1, 1, 1 }, // 19
        { 4, 3, 3, 3, 3, 2, 2, 1, 1 }  // 20
    };

    // Semilanzadoras: Paladín, Explorador (comienzan a nivel 2)
    // # VERIFICAR: tabla semilanzadoras SRD 5.1
    private static final int[][] HALF_CASTER_SLOTS = {
        //  1  2  3  4  5  6  7  8  9
        { 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // 1
        { 2, 0, 0, 0, 0, 0, 0, 0, 0 }, // 2
        { 3, 0, 0, 0, 0, 0, 0, 0, 0 }, // 3
        { 3, 0, 0, 0, 0, 0, 0, 0, 0 }, // 4
        { 4, 2, 0, 0, 0, 0, 0, 0, 0 }, // 5
        { 4, 2, 0, 0, 0, 0, 0, 0, 0 }, // 6
        { 4, 3, 0, 0, 0, 0, 0, 0, 0 }, // 7
        { 4, 3, 0, 0, 0, 0, 0, 0, 0 }, // 8
        { 4, 3, 2, 0, 0, 0, 0, 0, 0 }, // 9
        { 4, 3, 2, 0, 0, 0, 0, 0, 0 }, // 10
        { 4, 3, 3, 0, 0, 0, 0, 0, 0 }, // 11
        { 4, 3, 3, 0, 0, 0, 0, 0, 0 }, // 12
        { 4, 3, 3, 1, 0, 0, 0, 0, 0 }, // 13
        { 4, 3, 3, 1, 0, 0, 0, 0, 0 }, // 14
        { 4, 3, 3, 2, 0, 0, 0, 0, 0 }, // 15
        { 4, 3, 3, 2, 0, 0, 0, 0, 0 }, // 16
        { 4, 3, 3, 3, 1, 0, 0, 0, 0 }, // 17
        { 4, 3, 3, 3, 1, 0, 0, 0, 0 }, // 18
        { 4, 3, 3, 3, 2, 0, 0, 0, 0 }, // 19
        { 4, 3, 3, 3, 2, 0, 0, 0, 0 }  // 20
    };

    /**
     * Devuelve los espacios de conjuro máximos [9] para una clase y nivel.
     */
    public static int[] getSlots(ClassType cls, int level) {
        int[] result = new int[9];
        if (cls == null || level <= 0) return result;
        int clampedLevel = Math.min(20, Math.max(1, level));

        if (cls == ClassType.WARLOCK) {
            return getWarlockSlots(clampedLevel);
        }

        if (cls == ClassType.MAGE || cls == ClassType.CLERIC || cls == ClassType.DRUID
                || cls == ClassType.BARD || cls == ClassType.SORCERER) {
            System.arraycopy(FULL_CASTER_SLOTS[clampedLevel - 1], 0, result, 0, 9);
            return result;
        }

        if (cls == ClassType.PALADIN || cls == ClassType.RANGER) {
            System.arraycopy(HALF_CASTER_SLOTS[clampedLevel - 1], 0, result, 0, 9);
            return result;
        }

        // Clases no lanzadoras: 0 espacios
        return result;
    }

    /**
     * Espacios de Magia de Pacto del Brujo (Warlock).
     * Todos los espacios son del mismo nivel y se recuperan en descanso corto.
     * # VERIFICAR: progresión de Pact Magic SRD 5.1
     */
    public static int[] getWarlockSlots(int level) {
        int[] result = new int[9];
        int clampedLevel = Math.min(20, Math.max(1, level));

        int slotCount = switch (clampedLevel) {
            case 1 -> 1;
            case 2, 3, 4, 5, 6, 7, 8, 9, 10 -> 2;
            case 11, 12, 13, 14, 15, 16 -> 3;
            default -> 4;
        };

        int slotLevel = switch (clampedLevel) {
            case 1, 2 -> 1;
            case 3, 4 -> 2;
            case 5, 6 -> 3;
            case 7, 8 -> 4;
            default -> 5; // Nivel 9+ son de nivel 5
        };

        result[slotLevel - 1] = slotCount;
        return result;
    }

    /**
     * Comprueba si una clase es lanzadora (completa, semi o brujo).
     */
    public static boolean isSpellcaster(ClassType cls) {
        if (cls == null) return false;
        return cls == ClassType.MAGE || cls == ClassType.CLERIC || cls == ClassType.DRUID
                || cls == ClassType.BARD || cls == ClassType.SORCERER
                || cls == ClassType.WARLOCK || cls == ClassType.PALADIN
                || cls == ClassType.RANGER;
    }
}
