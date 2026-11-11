package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.spells.SpellDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellSlotTable;
import com.tuusuario.mmo.eldencraft.spells.SpellValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpellTest {

    @Test
    public void testFullCasterSpellSlots() {
        // Nivel 1 Mago: 2 espacios de nivel 1
        int[] slotsLvl1 = SpellSlotTable.getSlots(ClassType.MAGE, 1);
        assertEquals(2, slotsLvl1[0]);
        assertEquals(0, slotsLvl1[1]);

        // Nivel 3 Mago: 4 de nivel 1, 2 de nivel 2
        int[] slotsLvl3 = SpellSlotTable.getSlots(ClassType.MAGE, 3);
        assertEquals(4, slotsLvl3[0]);
        assertEquals(2, slotsLvl3[1]);
        assertEquals(0, slotsLvl3[2]);

        // Nivel 5 Mago: 4 de niv 1, 3 de niv 2, 2 de niv 3
        int[] slotsLvl5 = SpellSlotTable.getSlots(ClassType.MAGE, 5);
        assertEquals(4, slotsLvl5[0]);
        assertEquals(3, slotsLvl5[1]);
        assertEquals(2, slotsLvl5[2]);
    }

    @Test
    public void testHalfCasterSpellSlots() {
        // Nivel 1 Paladín: 0 espacios
        int[] slotsLvl1 = SpellSlotTable.getSlots(ClassType.PALADIN, 1);
        assertEquals(0, slotsLvl1[0]);

        // Nivel 2 Paladín: 2 espacios de nivel 1
        int[] slotsLvl2 = SpellSlotTable.getSlots(ClassType.PALADIN, 2);
        assertEquals(2, slotsLvl2[0]);
        assertEquals(0, slotsLvl2[1]);
    }

    @Test
    public void testWarlockPactMagicSlots() {
        // Nivel 1 Brujo: 1 slot de nivel 1
        int[] slotsLvl1 = SpellSlotTable.getSlots(ClassType.WARLOCK, 1);
        assertEquals(1, slotsLvl1[0]);

        // Nivel 5 Brujo: 2 slots de nivel 3
        int[] slotsLvl5 = SpellSlotTable.getSlots(ClassType.WARLOCK, 5);
        assertEquals(0, slotsLvl5[0]);
        assertEquals(0, slotsLvl5[1]);
        assertEquals(2, slotsLvl5[2]); // nivel 3
    }

    @Test
    public void testSpellSaveDCAndAttackBonus() {
        int prof = 3;
        int castingMod = 3;

        // CD = 8 + prof + mod = 14
        assertEquals(14, AttributeCalculator.spellSaveDC(prof, castingMod));

        // Ataque = prof + mod = 6
        assertEquals(6, AttributeCalculator.spellAttackBonus(prof, castingMod));
    }

    @Test
    public void testClassRestrictions() {
        SpellDefinition curarHeridas = new SpellDefinition();
        curarHeridas.setId("curar_heridas");
        curarHeridas.setNombre("Curar Heridas");
        curarHeridas.setNivel(1);
        curarHeridas.setClases(List.of("cleric", "druid", "bard", "paladin", "ranger"));

        SpellDefinition bolaFuego = new SpellDefinition();
        bolaFuego.setId("bola_de_fuego");
        bolaFuego.setNombre("Bola de Fuego");
        bolaFuego.setNivel(3);
        bolaFuego.setClases(List.of("mage", "sorcerer"));

        // Un Mago NO puede aprender ni lanzar Curar Heridas; un Clérigo SÍ
        assertFalse(SpellValidator.canAccess(ClassType.MAGE, curarHeridas),
                "Un Mago no debe tener acceso a Curar Heridas.");
        assertTrue(SpellValidator.canAccess(ClassType.CLERIC, curarHeridas),
                "Un Clérigo debe tener acceso a Curar Heridas.");
        assertTrue(SpellValidator.canAccess(ClassType.DRUID, curarHeridas));
        assertTrue(SpellValidator.canAccess(ClassType.PALADIN, curarHeridas));

        // Bola de Fuego: Mago sí, Clérigo no, Guerrero no
        assertTrue(SpellValidator.canAccess(ClassType.MAGE, bolaFuego));
        assertTrue(SpellValidator.canAccess(ClassType.SORCERER, bolaFuego));
        assertFalse(SpellValidator.canAccess(ClassType.CLERIC, bolaFuego));
        assertFalse(SpellValidator.canAccess(ClassType.WARRIOR, bolaFuego));
    }
}
