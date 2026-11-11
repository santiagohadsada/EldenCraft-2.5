package com.tuusuario.mmo.eldencraft.rules;

/**
 * Resultado de una prueba de habilidad o salvación D&D 5e.
 */
public class CheckResult {
    public final int roll;        // resultado del d20
    public final int modifier;    // modificador total aplicado
    public final int total;       // roll + modifier
    public final int dc;          // dificultad contra la que se tira
    public final boolean success; // total >= dc

    public CheckResult(int roll, int modifier, int total, int dc, boolean success) {
        this.roll = roll;
        this.modifier = modifier;
        this.total = total;
        this.dc = dc;
        this.success = success;
    }

    public String modifierString() {
        return modifier >= 0 ? "+" + modifier : String.valueOf(modifier);
    }
}
