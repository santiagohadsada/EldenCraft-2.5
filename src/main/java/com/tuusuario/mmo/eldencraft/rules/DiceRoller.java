package com.tuusuario.mmo.eldencraft.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor de tiradas de dados D&D 5e.
 * Soporta: d20, 2d6+3, 4d6kh3 (keep highest 3), ventaja/desventaja.
 */
public class DiceRoller {

    private static final Random RNG = new Random();
    // Patrón: NdM+/-K [khX = keep highest X]
    private static final Pattern PATTERN =
            Pattern.compile("(?:(\\d+)d(\\d+)(?:kh(\\d+))?)|([+-]?\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Resultado de una tirada de dados.
     */
    public static class RollResult {
        public final int total;
        public final List<Integer> dice;
        public final String expression;
        public final boolean isCritical; // d20 natural 20
        public final boolean isFumble;   // d20 natural 1

        public RollResult(int total, List<Integer> dice, String expression, boolean isCritical, boolean isFumble) {
            this.total = total;
            this.dice = dice;
            this.expression = expression;
            this.isCritical = isCritical;
            this.isFumble = isFumble;
        }

        /** Desglose legible, ej: "5+3+2+3" */
        public String breakdown() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < dice.size(); i++) {
                if (i > 0) sb.append('+');
                sb.append(dice.get(i));
            }
            return sb.toString();
        }
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Evalúa una expresión de dados y devuelve el resultado.
     * Ejemplos: "d20", "2d6+3", "4d6kh3"
     */
    public RollResult roll(String expression) {
        List<Integer> allDice = new ArrayList<>();
        int total = 0;
        boolean isCritical = false;
        boolean isFumble = false;
        boolean isBaseD20 = expression.trim().equalsIgnoreCase("d20")
                || expression.trim().equalsIgnoreCase("1d20");

        Matcher m = PATTERN.matcher(expression.trim().toLowerCase());
        while (m.find()) {
            if (m.group(1) != null) {
                // Es un grupo NdM[khX]
                int n = Integer.parseInt(m.group(1));
                int sides = Integer.parseInt(m.group(2));
                String khStr = m.group(3);

                List<Integer> rolled = rollDice(n, sides);

                if (khStr != null) {
                    // keep highest X
                    int keep = Integer.parseInt(khStr);
                    rolled.sort((a, b) -> b - a);
                    rolled = rolled.subList(0, Math.min(keep, rolled.size()));
                }

                for (int v : rolled) {
                    allDice.add(v);
                    total += v;
                }

                // Detectar crítico/pifia solo para 1d20 base
                if (isBaseD20 && n == 1 && sides == 20) {
                    if (rolled.get(0) == 20) isCritical = true;
                    if (rolled.get(0) == 1) isFumble = true;
                }
            } else if (m.group(4) != null) {
                // Es un modificador numérico (+3 o -2)
                int mod = Integer.parseInt(m.group(4));
                allDice.add(mod);
                total += mod;
            }
        }

        return new RollResult(total, allDice, expression, isCritical, isFumble);
    }

    /**
     * Lanza con ventaja: 2d20, mantiene el mayor.
     */
    public RollResult rollWithAdvantage(String baseDice) {
        RollResult r1 = roll(baseDice);
        RollResult r2 = roll(baseDice);
        return r1.total >= r2.total ? r1 : r2;
    }

    /**
     * Lanza con desventaja: 2d20, mantiene el menor.
     */
    public RollResult rollWithDisadvantage(String baseDice) {
        RollResult r1 = roll(baseDice);
        RollResult r2 = roll(baseDice);
        return r1.total <= r2.total ? r1 : r2;
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private List<Integer> rollDice(int n, int sides) {
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            results.add(RNG.nextInt(sides) + 1);
        }
        return results;
    }
}
