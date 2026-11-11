package com.tuusuario.mmo.eldencraft.rules;

import com.tuusuario.mmo.eldencraft.models.PlayerData;

/**
 * Motor de pruebas de habilidad y salvaciones D&D 5e SRD.
 */
public class CheckEngine {

    private final DiceRoller dice = new DiceRoller();

    // ── Prueba de habilidad ───────────────────────────────────────────────────

    /**
     * Realiza una prueba de habilidad: d20 + mod(atributo) + prof (si es competente) × 2 (si es experto).
     */
    public CheckResult performSkillCheck(PlayerData data, SkillDefinition skill,
                                         int dc, boolean isProficient, boolean isExpert) {
        int attrValue = getAttrValue(data, skill.attribute);
        int attrMod = AttributeCalculator.modifier(attrValue);
        int profBonus = AttributeCalculator.proficiencyBonus(data.getLevel());

        int totalMod = attrMod;
        if (isProficient) {
            totalMod += isExpert ? (profBonus * 2) : profBonus;
        }

        DiceRoller.RollResult rr = dice.roll("1d20");
        int total = rr.total + totalMod;
        return new CheckResult(rr.total, totalMod, total, dc, total >= dc);
    }

    /**
     * Realiza una salvación: d20 + mod(atributo) + prof (si tiene competencia en ese save).
     */
    public CheckResult performSavingThrow(PlayerData data, Attribute attr,
                                           int dc, boolean isProficient) {
        int attrValue = getAttrValue(data, attr);
        int attrMod = AttributeCalculator.modifier(attrValue);
        int profBonus = AttributeCalculator.proficiencyBonus(data.getLevel());

        int totalMod = attrMod + (isProficient ? profBonus : 0);
        DiceRoller.RollResult rr = dice.roll("1d20");
        int total = rr.total + totalMod;
        return new CheckResult(rr.total, totalMod, total, dc, total >= dc);
    }

    /**
     * Calcula una prueba pasiva: 10 + mod(atributo) + prof (si es competente).
     * Puede usarse para Percepción pasiva, Perspicacia pasiva, etc.
     */
    public int performPassiveCheck(PlayerData data, SkillDefinition skill, boolean isProficient) {
        int attrValue = getAttrValue(data, skill.attribute);
        int attrMod = AttributeCalculator.modifier(attrValue);
        int profBonus = AttributeCalculator.proficiencyBonus(data.getLevel());
        return 10 + attrMod + (isProficient ? profBonus : 0);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /**
     * Obtiene el valor del atributo base del PlayerData.
     * Usa los campos base (standard array) si están asignados (> 0); si no, usa el sistema legacy.
     */
    private int getAttrValue(PlayerData data, Attribute attr) {
        return switch (attr) {
            case STR -> data.getEffectiveStr();
            case DEX -> data.getEffectiveDex();
            case CON -> data.getEffectiveCon();
            case INT -> data.getEffectiveInt();
            case WIS -> data.getEffectiveWis();
            case CHA -> data.getEffectiveCha();
        };
    }
}
