package com.tuusuario.mmo.eldencraft.storage;

import com.tuusuario.mmo.eldencraft.EldenCraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de migraciones de esquema de base de datos.
 * Aplica migraciones numeradas para mantener la BD actualizada sin perder datos.
 */
public class SchemaManager {

    private static final int CURRENT_VERSION = 5;

    private final EldenCraft plugin;
    private final Connection connection;
    private final boolean isMySQL;

    public SchemaManager(EldenCraft plugin, Connection connection, boolean isMySQL) {
        this.plugin = plugin;
        this.connection = connection;
        this.isMySQL = isMySQL;
    }

    /**
     * Ejecuta todas las migraciones pendientes.
     */
    public void migrate() {
        try {
            ensureMetaTable();
            int version = getSchemaVersion();
            if (plugin != null) {
                plugin.getLogger().info("[SchemaManager] Versión de esquema actual: " + version);
            }

            for (int v = version + 1; v <= CURRENT_VERSION; v++) {
                backupIfSQLite(v);
                applyMigration(v);
                setSchemaVersion(v);
                if (plugin != null) {
                    plugin.getLogger().info("[SchemaManager] Migración v" + v + " aplicada.");
                }
            }

            if (version == CURRENT_VERSION && plugin != null) {
                plugin.getLogger().info("[SchemaManager] Esquema al día (v" + CURRENT_VERSION + ").");
            }
        } catch (SQLException e) {
            if (plugin != null) {
                plugin.getLogger().severe("[SchemaManager] Error en migración: " + e.getMessage());
            } else {
                e.printStackTrace();
            }
            throw new RuntimeException("Error en migración SchemaManager: " + e.getMessage(), e);
        }
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    private void ensureMetaTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_meta (" +
                "  key_name VARCHAR(64) PRIMARY KEY, " +
                "  value VARCHAR(255)" +
                ");"
            );
        }
    }

    // ── Versión ───────────────────────────────────────────────────────────────

    public int getSchemaVersion() throws SQLException {
        ensureMetaTable();
        String sql = "SELECT value FROM ec_meta WHERE key_name = 'schema_version';";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString("value"));
            }
        }
        return 0;
    }

    public void setSchemaVersion(int version) throws SQLException {
        String sql;
        if (isMySQL) {
            sql = "INSERT INTO ec_meta (key_name, value) VALUES ('schema_version', '" + version + "') " +
                  "ON DUPLICATE KEY UPDATE value = '" + version + "';";
        } else {
            sql = "INSERT OR REPLACE INTO ec_meta (key_name, value) VALUES ('schema_version', '" + version + "');";
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ── Backup SQLite ─────────────────────────────────────────────────────────

    private void backupIfSQLite(int targetVersion) {
        if (isMySQL || plugin == null) return;
        File dbFile = new File(plugin.getDataFolder(), "eldencraft.db");
        if (!dbFile.exists()) return;
        File backup = new File(plugin.getDataFolder(), "eldencraft.db.bak.v" + (targetVersion - 1));
        try {
            Files.copy(dbFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("[SchemaManager] Backup creado: " + backup.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("[SchemaManager] No se pudo crear backup: " + e.getMessage());
        }
    }

    // ── Migraciones ───────────────────────────────────────────────────────────

    public void applyMigration(int version) throws SQLException {
        switch (version) {
            case 1 -> migrateV1();
            case 2 -> migrateV2();
            case 3 -> migrateV3();
            case 4 -> migrateV4();
            case 5 -> migrateV5();
            default -> {
                if (plugin != null) {
                    plugin.getLogger().warning("[SchemaManager] Migración v" + version + " no definida.");
                }
            }
        }
    }

    /**
     * v1 — Añade hp_current e hp_max a ec_players para el sistema PG D&D.
     */
    private void migrateV1() throws SQLException {
        addColumnIfMissing("ec_players", "hp_current", "INTEGER DEFAULT 0");
        addColumnIfMissing("ec_players", "hp_max", "INTEGER DEFAULT 10");
    }

    /**
     * v2 — Añade atributos base (standard array) a ec_players.
     */
    private void migrateV2() throws SQLException {
        addColumnIfMissing("ec_players", "base_str", "INTEGER DEFAULT 10");
        addColumnIfMissing("ec_players", "base_dex", "INTEGER DEFAULT 10");
        addColumnIfMissing("ec_players", "base_con", "INTEGER DEFAULT 10");
        addColumnIfMissing("ec_players", "base_int", "INTEGER DEFAULT 10");
        addColumnIfMissing("ec_players", "base_wis", "INTEGER DEFAULT 10");
        addColumnIfMissing("ec_players", "base_cha", "INTEGER DEFAULT 10");
    }

    /**
     * v3 — Tablas para hechizos: espacios, conocidos y preparados.
     */
    private void migrateV3() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_spell_slots (" +
                "  uuid VARCHAR(36), " +
                "  slot_level INTEGER, " +
                "  max_slots INTEGER DEFAULT 0, " +
                "  used_slots INTEGER DEFAULT 0, " +
                "  PRIMARY KEY (uuid, slot_level)" +
                ");"
            );
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_spells_known (" +
                "  uuid VARCHAR(36), " +
                "  spell_id VARCHAR(64), " +
                "  PRIMARY KEY (uuid, spell_id)" +
                ");"
            );
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_spells_prepared (" +
                "  uuid VARCHAR(36), " +
                "  spell_id VARCHAR(64), " +
                "  PRIMARY KEY (uuid, spell_id)" +
                ");"
            );
        }
    }

    /**
     * v4 (LOTE 1B: F3B-1) — Multi-personajes (27 slots) y re-asociación de datos de uuid a character_id.
     */
    private void migrateV4() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 1. Tabla de personajes
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_characters (" +
                "  character_id VARCHAR(64) PRIMARY KEY, " +
                "  player_uuid VARCHAR(36) NOT NULL, " +
                "  slot INTEGER NOT NULL, " +
                "  name VARCHAR(64) NOT NULL, " +
                "  race VARCHAR(32), " +
                "  class VARCHAR(32), " +
                "  subclass VARCHAR(64), " +
                "  level INTEGER DEFAULT 1, " +
                "  xp INTEGER DEFAULT 0, " +
                "  gold INTEGER DEFAULT 0, " +
                "  fatigue INTEGER DEFAULT 0, " +
                "  hp_current INTEGER DEFAULT 10, " +
                "  hp_max INTEGER DEFAULT 10, " +
                "  base_str INTEGER DEFAULT 10, " +
                "  base_dex INTEGER DEFAULT 10, " +
                "  base_con INTEGER DEFAULT 10, " +
                "  base_int INTEGER DEFAULT 10, " +
                "  base_wis INTEGER DEFAULT 10, " +
                "  base_cha INTEGER DEFAULT 10, " +
                "  campaign_id VARCHAR(64) DEFAULT 'principal', " +
                "  created_at BIGINT DEFAULT 0, " +
                "  last_played BIGINT DEFAULT 0, " +
                "  deleted_at BIGINT, " +
                "  is_active INTEGER DEFAULT 0, " +
                "  proficiencies TEXT, " +
                "  alignment VARCHAR(32) DEFAULT 'Neutral', " +
                "  background VARCHAR(64) DEFAULT 'Aventurero', " +
                "  UNIQUE (player_uuid, slot)" +
                ");"
            );

            // 2. Tabla de inventario persistente por personaje
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_character_inventory (" +
                "  character_id VARCHAR(64) PRIMARY KEY, " +
                "  inventory_data TEXT, " +
                "  armor_data TEXT, " +
                "  offhand_data TEXT, " +
                "  ender_chest_data TEXT, " +
                "  location_world VARCHAR(64), " +
                "  location_x DOUBLE, " +
                "  location_y DOUBLE, " +
                "  location_z DOUBLE, " +
                "  location_yaw FLOAT, " +
                "  location_pitch FLOAT, " +
                "  game_mode VARCHAR(32)" +
                ");"
            );
        }

        // 3. Añadir columna character_id a tablas del Lote 1
        addColumnIfMissing("ec_spell_slots", "character_id", "VARCHAR(64)");
        addColumnIfMissing("ec_spells_known", "character_id", "VARCHAR(64)");
        addColumnIfMissing("ec_spells_prepared", "character_id", "VARCHAR(64)");
        addColumnIfMissing("ec_completed_missions", "character_id", "VARCHAR(64)");
        addColumnIfMissing("ec_active_missions", "character_id", "VARCHAR(64)");

        // 4. Migración idempotente de ec_players -> ec_characters (slot 1)
        if (tableExists("ec_players")) {
            String insertSql;
            if (isMySQL) {
                insertSql = "INSERT IGNORE INTO ec_characters (" +
                            "  character_id, player_uuid, slot, name, race, class, level, xp, gold, fatigue, " +
                            "  hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, " +
                            "  campaign_id, created_at, last_played, is_active) " +
                            "SELECT " +
                            "  CONCAT(uuid, '_1'), uuid, 1, 'Personaje 1', race, class, level, xp, gold, fatigue, " +
                            "  hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, " +
                            "  'principal', 0, 0, 1 " +
                            "FROM ec_players;";
            } else {
                insertSql = "INSERT OR IGNORE INTO ec_characters (" +
                            "  character_id, player_uuid, slot, name, race, class, level, xp, gold, fatigue, " +
                            "  hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, " +
                            "  campaign_id, created_at, last_played, is_active) " +
                            "SELECT " +
                            "  uuid || '_1', uuid, 1, 'Personaje 1', race, class, level, xp, gold, fatigue, " +
                            "  hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, " +
                            "  'principal', 0, 0, 1 " +
                            "FROM ec_players;";
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(insertSql);
            }
        }

        // 5. Re-asociar registros de lote 1 que no tengan character_id (todas las filas antiguas)
        String updateExpr = isMySQL ? "CONCAT(uuid, '_1')" : "uuid || '_1'";
        try (Statement stmt = connection.createStatement()) {
            if (tableExists("ec_spell_slots")) stmt.executeUpdate("UPDATE ec_spell_slots SET character_id = " + updateExpr + " WHERE character_id IS NULL OR character_id = '';");
            if (tableExists("ec_spells_known")) stmt.executeUpdate("UPDATE ec_spells_known SET character_id = " + updateExpr + " WHERE character_id IS NULL OR character_id = '';");
            if (tableExists("ec_spells_prepared")) stmt.executeUpdate("UPDATE ec_spells_prepared SET character_id = " + updateExpr + " WHERE character_id IS NULL OR character_id = '';");
            if (tableExists("ec_completed_missions")) stmt.executeUpdate("UPDATE ec_completed_missions SET character_id = " + updateExpr + " WHERE character_id IS NULL OR character_id = '';");
            if (tableExists("ec_active_missions")) stmt.executeUpdate("UPDATE ec_active_missions SET character_id = " + updateExpr + " WHERE character_id IS NULL OR character_id = '';");
        }
    }

    /**
     * v5 (LOTE 1B: F3B-2 & F3B-3) — Campañas, roles, miembros, notas e historial.
     */
    private void migrateV5() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_campaigns (" +
                "  id VARCHAR(64) PRIMARY KEY, " +
                "  name VARCHAR(64) NOT NULL, " +
                "  description TEXT, " +
                "  icon VARCHAR(32) DEFAULT 'BOOK', " +
                "  dm_uuid VARCHAR(36) NOT NULL, " +
                "  world_name VARCHAR(64), " +
                "  spawn_x DOUBLE DEFAULT 0, " +
                "  spawn_y DOUBLE DEFAULT 64, " +
                "  spawn_z DOUBLE DEFAULT 0, " +
                "  status VARCHAR(32) DEFAULT 'ABIERTA', " +
                "  max_players INTEGER DEFAULT 8, " +
                "  min_level INTEGER DEFAULT 1, " +
                "  max_level INTEGER DEFAULT 20, " +
                "  join_code VARCHAR(16), " +
                "  created_at BIGINT DEFAULT 0, " +
                "  last_session_at BIGINT DEFAULT 0" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_campaign_members (" +
                "  campaign_id VARCHAR(64), " +
                "  player_uuid VARCHAR(36), " +
                "  role VARCHAR(32) DEFAULT 'JUGADOR', " +
                "  character_id VARCHAR(64), " +
                "  joined_at BIGINT DEFAULT 0, " +
                "  PRIMARY KEY (campaign_id, player_uuid)" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_campaign_invites (" +
                "  campaign_id VARCHAR(64), " +
                "  player_uuid VARCHAR(36), " +
                "  invite_code VARCHAR(16), " +
                "  status VARCHAR(32) DEFAULT 'PENDIENTE', " +
                "  created_at BIGINT DEFAULT 0, " +
                "  PRIMARY KEY (campaign_id, player_uuid)" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_campaign_settings (" +
                "  campaign_id VARCHAR(64), " +
                "  setting_key VARCHAR(64), " +
                "  setting_value TEXT, " +
                "  PRIMARY KEY (campaign_id, setting_key)" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_campaign_notes (" +
                "  id INTEGER PRIMARY KEY " + (isMySQL ? "AUTO_INCREMENT" : "AUTOINCREMENT") + ", " +
                "  campaign_id VARCHAR(64), " +
                "  character_id VARCHAR(64), " +
                "  note_text TEXT, " +
                "  created_at BIGINT DEFAULT 0" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_sessions (" +
                "  session_id VARCHAR(64) PRIMARY KEY, " +
                "  campaign_id VARCHAR(64), " +
                "  started_at BIGINT DEFAULT 0, " +
                "  ended_at BIGINT DEFAULT 0, " +
                "  notes TEXT, " +
                "  xp_awarded INTEGER DEFAULT 0, " +
                "  loot_awarded TEXT" +
                ");"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ec_character_history (" +
                "  history_id INTEGER PRIMARY KEY " + (isMySQL ? "AUTO_INCREMENT" : "AUTOINCREMENT") + ", " +
                "  character_id VARCHAR(64), " +
                "  timestamp BIGINT DEFAULT 0, " +
                "  action_type VARCHAR(64), " +
                "  description TEXT, " +
                "  changed_by VARCHAR(64)" +
                ");"
            );

            // Campaña legacy por defecto
            String defaultCampaignSql = (isMySQL ? "INSERT IGNORE" : "INSERT OR IGNORE") +
                " INTO ec_campaigns (" +
                "  id, name, description, icon, dm_uuid, world_name, spawn_x, spawn_y, spawn_z, status, max_players, min_level, max_level, created_at, last_session_at" +
                ") VALUES (" +
                "  'principal', 'Principal', 'Campaña principal por defecto (legacy).', 'COMPASS', '00000000-0000-0000-0000-000000000000', 'world', 0, 64, 0, 'ABIERTA', 20, 1, 20, 0, 0" +
                ");";
            stmt.execute(defaultCampaignSql);
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void addColumnIfMissing(String table, String column, String definition) throws SQLException {
        if (!tableExists(table) || columnExists(table, column)) return;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition + ";");
            if (plugin != null) {
                plugin.getLogger().info("[SchemaManager] Columna añadida: " + table + "." + column);
            }
        }
    }

    private boolean tableExists(String table) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM " + table + " LIMIT 0;")) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean columnExists(String table, String column) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " LIMIT 0;")) {
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (meta.getColumnName(i).equalsIgnoreCase(column)) return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
}
