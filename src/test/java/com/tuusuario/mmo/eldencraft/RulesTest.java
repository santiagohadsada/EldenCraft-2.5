package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.rules.DiceRoller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RulesTest {

    @Test
    public void testModifiers() {
        assertEquals(-5, AttributeCalculator.modifier(1));
        assertEquals(-1, AttributeCalculator.modifier(8));
        assertEquals(-1, AttributeCalculator.modifier(9));
        assertEquals(0, AttributeCalculator.modifier(10));
        assertEquals(0, AttributeCalculator.modifier(11));
        assertEquals(1, AttributeCalculator.modifier(12));
        assertEquals(2, AttributeCalculator.modifier(14));
        assertEquals(2, AttributeCalculator.modifier(15));
        assertEquals(5, AttributeCalculator.modifier(20));
    }

    @Test
    public void testProficiencyBonus() {
        assertEquals(2, AttributeCalculator.proficiencyBonus(1));
        assertEquals(2, AttributeCalculator.proficiencyBonus(4));
        assertEquals(3, AttributeCalculator.proficiencyBonus(5));
        assertEquals(3, AttributeCalculator.proficiencyBonus(8));
        assertEquals(4, AttributeCalculator.proficiencyBonus(9));
        assertEquals(4, AttributeCalculator.proficiencyBonus(12));
        assertEquals(5, AttributeCalculator.proficiencyBonus(13));
        assertEquals(5, AttributeCalculator.proficiencyBonus(16));
        assertEquals(6, AttributeCalculator.proficiencyBonus(17));
        assertEquals(6, AttributeCalculator.proficiencyBonus(20));
    }

    @Test
    public void testDiceRoller() {
        DiceRoller roller = new DiceRoller();

        for (int i = 0; i < 50; i++) {
            DiceRoller.RollResult r = roller.roll("1d20");
            assertTrue(r.total >= 1 && r.total <= 20, "Total fuera de rango 1-20: " + r.total);
            if (r.total == 20) assertTrue(r.isCritical);
            if (r.total == 1) assertTrue(r.isFumble);
        }

        for (int i = 0; i < 50; i++) {
            DiceRoller.RollResult r = roller.roll("2d6+3");
            assertTrue(r.total >= 5 && r.total <= 15, "Total fuera de rango 5-15: " + r.total);
        }
    }

    @Test
    public void testAdvantageAndDisadvantage() {
        DiceRoller roller = new DiceRoller();
        long advSum = 0;
        long disSum = 0;
        int trials = 1000;

        for (int i = 0; i < trials; i++) {
            advSum += roller.rollWithAdvantage("1d20").total;
            disSum += roller.rollWithDisadvantage("1d20").total;
        }

        double advAvg = (double) advSum / trials;
        double disAvg = (double) disSum / trials;

        assertTrue(advAvg > disAvg, "La ventaja debe tener un promedio mayor que la desventaja.");
        assertTrue(advAvg > 12.0, "La ventaja de 1d20 tiene media teórica ~13.82, obtenida: " + advAvg);
        assertTrue(disAvg < 9.5, "La desventaja de 1d20 tiene media teórica ~7.17, obtenida: " + disAvg);
    }
}
