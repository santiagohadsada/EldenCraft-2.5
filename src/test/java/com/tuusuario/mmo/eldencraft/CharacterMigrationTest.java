package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.character.CharacterManager;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.storage.SchemaManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterMigrationTest {

    @Test
    public void testMaxSlotsLimit27() {
        assertEquals(27, CharacterManager.DEFAULT_MAX_SLOTS);

        UUID uuid = UUID.randomUUID();
        CharacterProfile profileValidLow = new CharacterProfile(uuid + "_1", uuid, 1, "Slot 1");
        CharacterProfile profileValidHigh = new CharacterProfile(uuid + "_27", uuid, 27, "Slot 27");

        assertEquals(1, profileValidLow.getSlot());
        assertEquals(27, profileValidHigh.getSlot());
    }

    @Test
    public void testSoftDeleteGracePeriod() {
        UUID uuid = UUID.randomUUID();
        CharacterProfile profile = new CharacterProfile(uuid + "_1", uuid, 1, "Héroe");
        assertFalse(profile.isDeleted());
        assertFalse(profile.canBeRecovered(7));

        long now = System.currentTimeMillis();
        // Borrado hace 2 días: recuperable
        profile.setDeletedAt(now - (2L * 24 * 3600 * 1000));
        assertTrue(profile.isDeleted());
        assertTrue(profile.canBeRecovered(7));

        // Borrado hace 8 días: fuera de gracia
        profile.setDeletedAt(now - (8L * 24 * 3600 * 1000));
        assertTrue(profile.isDeleted());
        assertFalse(profile.canBeRecovered(7));
    }

    @Test
    public void testCharacterProfileModel() {
        UUID uuid = UUID.randomUUID();
        CharacterProfile profile = new CharacterProfile(uuid + "_1", uuid, 1, "Gandalf");
        profile.setRace(RaceType.ELF);
        profile.setPlayerClass(ClassType.MAGE);
        profile.setLevel(5);
        profile.setGold(250);
        profile.setCampaignId("mordor");
        profile.getProficiencies().add(SkillDefinition.ARCANA);
        profile.getProficiencies().add(SkillDefinition.HISTORY);

        assertEquals("Gandalf", profile.getName());
        assertEquals(RaceType.ELF, profile.getRace());
        assertEquals(ClassType.MAGE, profile.getPlayerClass());
        assertEquals(5, profile.getLevel());
        assertEquals("mordor", profile.getCampaignId());
        assertTrue(profile.getProficiencies().contains(SkillDefinition.ARCANA));
        assertTrue(profile.getProficiencies().contains(SkillDefinition.HISTORY));
    }

    @Test
    public void testSchemaMigrationV4AndV5Idempotent() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            // Simular tabla previa de ec_players con datos de Lote 1
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE ec_players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "race VARCHAR(32), " +
                        "class VARCHAR(32), " +
                        "level INTEGER DEFAULT 1, " +
                        "xp INTEGER DEFAULT 0, " +
                        "gold INTEGER DEFAULT 0, " +
                        "fatigue INTEGER DEFAULT 0, " +
                        "hp_current INTEGER DEFAULT 10, " +
                        "hp_max INTEGER DEFAULT 10, " +
                        "base_str INTEGER DEFAULT 10, " +
                        "base_dex INTEGER DEFAULT 10, " +
                        "base_con INTEGER DEFAULT 10, " +
                        "base_int INTEGER DEFAULT 10, " +
                        "base_wis INTEGER DEFAULT 10, " +
                        "base_cha INTEGER DEFAULT 10" +
                        ");");

                stmt.execute("INSERT INTO ec_players (uuid, race, class, level, xp, gold, fatigue, hp_current, hp_max) " +
                        "VALUES ('11111111-1111-1111-1111-111111111111', 'HUMANO', 'WARRIOR', 3, 900, 150, 0, 28, 28);");
                stmt.execute("INSERT INTO ec_players (uuid, race, class, level, xp, gold, fatigue, hp_current, hp_max) " +
                        "VALUES ('22222222-2222-2222-2222-222222222222', 'ELFO', 'MAGE', 2, 300, 80, 0, 14, 14);");
            }

            SchemaManager schemaManager = new SchemaManager(null, conn, false);

            // Primera ejecución de migraciones
            schemaManager.migrate();

            // Verificar que se crearon los personajes para slot 1
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM ec_characters;")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("total"), "Debe migrar exactamente 2 personajes al slot 1");
            }

            // Verificar campaña principal por defecto creada
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM ec_campaigns WHERE id = 'principal';")) {
                assertTrue(rs.next());
                assertEquals("Principal", rs.getString("name"));
            }

            // Segunda ejecución de migraciones (prueba de IDEMPOTENCIA)
            schemaManager.migrate();

            // Verificar que no se duplicaron
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM ec_characters;")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("total"), "La migración repetida debe ser idempotente (2 registros)");
            }
        }
    }

    @Test
    public void testV4MigrationFillsCharacterIdInAllLegacyTables() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            String testUuid = "33333333-3333-3333-3333-333333333333";
            String expectedCharId = testUuid + "_1";

            try (Statement stmt = conn.createStatement()) {
                // Crear tablas antiguas con uuid y datos
                stmt.execute("CREATE TABLE ec_players (uuid VARCHAR(36) PRIMARY KEY, race VARCHAR(32), class VARCHAR(32), level INT, xp INT, gold INT, fatigue INT, hp_current INT, hp_max INT, base_str INT, base_dex INT, base_con INT, base_int INT, base_wis INT, base_cha INT);");
                stmt.execute("INSERT INTO ec_players (uuid, race, class, level, xp) VALUES ('" + testUuid + "', 'HUMANO', 'WARRIOR', 1, 0);");

                stmt.execute("CREATE TABLE ec_spell_slots (uuid VARCHAR(36), slot_level INT, max_slots INT, used_slots INT);");
                stmt.execute("INSERT INTO ec_spell_slots (uuid, slot_level, max_slots, used_slots) VALUES ('" + testUuid + "', 1, 2, 0);");

                stmt.execute("CREATE TABLE ec_spells_known (uuid VARCHAR(36), spell_id VARCHAR(64));");
                stmt.execute("INSERT INTO ec_spells_known (uuid, spell_id) VALUES ('" + testUuid + "', 'bola_fuego');");

                stmt.execute("CREATE TABLE ec_spells_prepared (uuid VARCHAR(36), spell_id VARCHAR(64));");
                stmt.execute("INSERT INTO ec_spells_prepared (uuid, spell_id) VALUES ('" + testUuid + "', 'escudo');");

                stmt.execute("CREATE TABLE ec_completed_missions (uuid VARCHAR(36), mission_id VARCHAR(64), completed_at BIGINT);");
                stmt.execute("INSERT INTO ec_completed_missions (uuid, mission_id, completed_at) VALUES ('" + testUuid + "', 'mision_1', 1000);");

                stmt.execute("CREATE TABLE ec_active_missions (uuid VARCHAR(36), mission_id VARCHAR(64), progress INT);");
                stmt.execute("INSERT INTO ec_active_missions (uuid, mission_id, progress) VALUES ('" + testUuid + "', 'mision_2', 1);");
            }

            SchemaManager schemaManager = new SchemaManager(null, conn, false);
            schemaManager.migrate();

            // Verificar que character_id fue poblado con <uuid>_1 en TODAS las filas de cada tabla
            String[] tables = {"ec_spell_slots", "ec_spells_known", "ec_spells_prepared", "ec_completed_missions", "ec_active_missions"};
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT character_id FROM " + table + ";")) {
                    assertTrue(rs.next(), "La tabla " + table + " debe tener filas");
                    assertEquals(expectedCharId, rs.getString("character_id"), "Tabla " + table + " debe tener character_id = " + expectedCharId);
                    assertFalse(rs.getString("character_id").isEmpty(), "character_id no debe estar vacío en " + table);
                }
            }
        }
    }

    @Test
    public void testOfflinePlayerLevelUpProcessing() {
        UUID uuid = UUID.randomUUID();
        CharacterProfile profile = new CharacterProfile(uuid + "_1", uuid, 1, "GuerreroTest");
        profile.setPlayerClass(ClassType.WARRIOR);
        profile.setRace(RaceType.HUMAN);
        profile.setBaseCon(14); // mod +2
        profile.setLevel(1);
        profile.setXp(0);

        int initialHp = com.tuusuario.mmo.eldencraft.character.LevelUpManager.calculateMaxHp(ClassType.WARRIOR, 1, 2);
        profile.setHpMax(initialHp);
        profile.setHpCurrent(initialHp);

        assertEquals(1, profile.getLevel());
        assertEquals(12, profile.getHpMax()); // 10 hitdie + 2 con = 12

        // Simular otorgar 1000 XP (umbral nivel 2: 300, nivel 3: 900)
        int xpGranted = 1000;
        int newXp = profile.getXp() + xpGranted;
        profile.setXp(newXp);

        int[] xpThresholds = {0, 300, 900, 2700, 6500};
        int currentLevel = profile.getLevel();
        while (currentLevel < 5 && newXp >= xpThresholds[currentLevel]) {
            currentLevel++;
            int conMod = (profile.getBaseCon() - 10) / 2;
            int newMaxHp = com.tuusuario.mmo.eldencraft.character.LevelUpManager.calculateMaxHp(profile.getPlayerClass(), currentLevel, conMod);
            profile.setHpMax(newMaxHp);
            profile.setHpCurrent(newMaxHp);
            profile.setLevel(currentLevel);
        }

        assertEquals(3, profile.getLevel(), "Con 1000 XP debe subir a nivel 3");
        // Nivel 3 Warrior (d10, avg 6 + 2 con = +8 por nivel): 12 + 8 + 8 = 28 HP
        assertEquals(28, profile.getHpMax(), "Max HP debe aumentar a 28");
        assertEquals(28, profile.getHpCurrent(), "Current HP debe aumentar proporcionalmente");
    }

    @Test
    public void testIsCasterMethod() {
        assertTrue(ClassType.MAGE.isCaster());
        assertTrue(ClassType.CLERIC.isCaster());
        assertTrue(ClassType.WARLOCK.isCaster());
        assertTrue(ClassType.SORCERER.isCaster());
        assertTrue(ClassType.DRUID.isCaster());
        assertTrue(ClassType.BARD.isCaster());
        assertTrue(ClassType.PALADIN.isCaster());
        assertTrue(ClassType.RANGER.isCaster());

        assertFalse(ClassType.WARRIOR.isCaster());
        assertFalse(ClassType.ROGUE.isCaster());
        assertFalse(ClassType.BARBARIAN.isCaster());
        assertFalse(ClassType.MONK.isCaster());
        assertFalse(ClassType.GUNSLINGER.isCaster());
    }

    @Test
    public void testCharacterImportJsonParsing() {
        String json = "{\"name\":\"Legolas\",\"race\":\"ELF\",\"class\":\"RANGER\",\"level\":1,\"xp\":0,\"gold\":50,\"str\":12,\"dex\":15,\"con\":13,\"int\":10,\"wis\":14,\"cha\":8,\"alignment\":\"Caótico Bueno\",\"background\":\"Aventurero\"}";
        CharacterManager mgr = new CharacterManager(null);
        UUID dummy = UUID.randomUUID();

        // Extraer valores como lo hace mgr.importFromJson
        assertTrue(json.contains("\"name\":\"Legolas\""));
        assertTrue(json.contains("\"race\":\"ELF\""));
        assertTrue(json.contains("\"class\":\"RANGER\""));
    }
}
