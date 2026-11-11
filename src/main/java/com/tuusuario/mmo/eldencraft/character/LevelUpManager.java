package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;

/**
 * Asigna el Standard Array D&D 5e a un personaje nuevo según su clase.
 * # VERIFICAR: La asignación automática por clase es decisión de diseño, no SRD.
 */
public class LevelUpManager {

    // Standard array: 15, 14, 13, 12, 10, 8
    private static final int[] STANDARD_ARRAY = {15, 14, 13, 12, 10, 8};

    private final EldenCraft plugin;

    public LevelUpManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    /**
     * Asigna el standard array a un PlayerData según su clase.
     * Solo aplica si baseStr == 0 (nunca asignado antes).
     */
    public void assignStandardArray(PlayerData data) {
        if (data.hasStandardArray()) return; // ya tiene
        if (data.getPlayerClass() == null) return;

        ClassType cls = data.getPlayerClass();
        int[] stats = getStandardArrayForClass(cls);
        // Orden: STR, DEX, CON, INT, WIS, CHA
        data.setBaseStr(stats[0]);
        data.setBaseDex(stats[1]);
        data.setBaseCon(stats[2]);
        data.setBaseInt(stats[3]);
        data.setBaseWis(stats[4]);
        data.setBaseCha(stats[5]);
    }

    /**
     * Distribución por prioridad de clase (decisión de diseño EldenCraft).
     * Devuelve [STR, DEX, CON, INT, WIS, CHA].
     */
    public static int[] getStandardArrayForClass(ClassType cls) {
        return switch (cls) {
            case WARRIOR, PALADIN, BARBARIAN ->
                // STR→15, CON→14, WIS→13, DEX→12, CHA→10, INT→8
                new int[]{15, 12, 14, 8, 13, 10};
            case MAGE, SORCERER ->
                // INT→15, WIS→14, CON→13, DEX→12, CHA→10, STR→8
                new int[]{8, 12, 13, 15, 14, 10};
            case RANGER, ROGUE ->
                // DEX→15, WIS→14, CON→13, STR→12, INT→10, CHA→8
                new int[]{12, 15, 13, 10, 14, 8};
            case BARD, WARLOCK ->
                // CHA→15, DEX→14, CON→13, WIS→12, INT→10, STR→8
                new int[]{8, 14, 13, 10, 12, 15};
            case CLERIC, DRUID ->
                // WIS→15, CON→14, STR→13, CHA→12, DEX→10, INT→8
                new int[]{13, 10, 14, 8, 15, 12};
            case MONK ->
                // DEX→15, WIS→14, CON→13, STR→12, INT→10, CHA→8
                new int[]{12, 15, 13, 10, 14, 8};
            case GUNSLINGER ->
                // DEX→15, CON→14, INT→13, WIS→12, STR→10, CHA→8
                new int[]{10, 15, 14, 13, 12, 8};
        };
    }

    /**
     * Calcula el PG máximo D&D por nivel según la clase (hit die + mod CON).
     * Nivel 1: máximo del dado + mod CON.
     * Niveles 2+: promedio redondeado arriba + mod CON por nivel.
     * # VERIFICAR: regla SRD exacta para promedio de dado de golpe
     */
    public static int calculateMaxHp(ClassType cls, int level, int conModifier) {
        if (cls == null || level <= 0) return 10;
        int hitDie = cls.hitDie;
        // Nivel 1: máximo del dado
        int hp = hitDie + conModifier;
        // Niveles 2+: promedio redondeado arriba = (hitDie/2 + 1) + conModifier por nivel
        int avgPerLevel = (hitDie / 2 + 1) + conModifier;
        hp += (level - 1) * avgPerLevel;
        return Math.max(1, hp);
    }
}
