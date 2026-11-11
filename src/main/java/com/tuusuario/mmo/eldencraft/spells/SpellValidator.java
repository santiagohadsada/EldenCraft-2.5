package com.tuusuario.mmo.eldencraft.spells;

import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;

import java.util.List;

/**
 * Validador de restricciones por clase para el aprendizaje, preparación y lanzamiento de hechizos (SPEC 5.4).
 */
public class SpellValidator {

    private SpellValidator() {}

    /**
     * Valida si una clase tiene acceso a un hechizo según su lista de clases SRD.
     */
    public static boolean canAccess(ClassType cls, SpellDefinition spell) {
        if (cls == null || spell == null) return false;
        List<String> allowed = spell.getClases();
        if (allowed == null || allowed.isEmpty()) return false;

        String key = cls.spellKey();
        String enumName = cls.name().toLowerCase();
        for (String c : allowed) {
            String clean = c.trim().toLowerCase();
            if (clean.equals(key) || clean.equals(enumName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Valida si un personaje puede lanzar el hechizo dado con el nivel de espacio solicitado.
     */
    public static ValidationResult validateCast(PlayerData data, SpellDefinition spell, int castLevel) {
        if (data == null) {
            return ValidationResult.fail("Datos del jugador no disponibles.");
        }
        if (spell == null) {
            return ValidationResult.fail("Hechizo no encontrado.");
        }

        ClassType cls = data.getPlayerClass();
        if (cls == null) {
            return ValidationResult.fail("No tienes una clase asignada.");
        }

        // 1. Acceso por clase
        if (!canAccess(cls, spell)) {
            String className = cls.displayName != null ? cls.displayName : cls.name();
            return ValidationResult.fail("Los " + className + " no tienen acceso a " + spell.getNombre() + ".");
        }

        // 2. Si es truco (nivel 0): no requiere espacio de conjuro
        if (spell.getNivel() == 0) {
            return ValidationResult.ok();
        }

        // 3. Nivel de espacio solicitado debe ser >= nivel base del hechizo
        if (castLevel < spell.getNivel()) {
            return ValidationResult.fail(spell.getNombre() + " es un hechizo de nivel " + spell.getNivel() + ".");
        }

        if (castLevel > 9) {
            return ValidationResult.fail("El nivel máximo de espacio de conjuro es 9.");
        }

        // 4. Espacio de conjuro disponible
        if (!data.hasSpellSlot(castLevel)) {
            return ValidationResult.fail("No te quedan espacios de conjuro de nivel " + castLevel + ".");
        }

        return ValidationResult.ok();
    }

    public static class ValidationResult {
        public final boolean valid;
        public final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String reason) {
            return new ValidationResult(false, reason);
        }
    }
}
