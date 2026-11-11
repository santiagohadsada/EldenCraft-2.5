package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignMember;
import com.tuusuario.mmo.eldencraft.campaign.CampaignRole;
import com.tuusuario.mmo.eldencraft.campaign.CampaignStatus;
import com.tuusuario.mmo.eldencraft.character.CharacterInventoryData;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import com.tuusuario.mmo.eldencraft.missions.MissionProgress;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.storage.SchemaManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final EldenCraft plugin;
    private Connection connection;
    private boolean isMySQL = false;

    public DataManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            String type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
            if ("MYSQL".equals(type)) {
                String host = plugin.getConfig().getString("database.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("database.mysql.port", 3306);
                String dbName = plugin.getConfig().getString("database.mysql.database", "eldencraft");
                String user = plugin.getConfig().getString("database.mysql.username", "root");
                String pass = plugin.getConfig().getString("database.mysql.password", "");
                boolean ssl = plugin.getConfig().getBoolean("database.mysql.useSSL", false);

                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=" + ssl
                        + "&allowPublicKeyRetrieval=true&autoReconnect=true";
                this.connection = DriverManager.getConnection(url, user, pass);
                this.isMySQL = true;
                plugin.getLogger().info("¡Conexión a la base de datos MySQL establecida con éxito!");
            } else {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                File dbFile = new File(plugin.getDataFolder(), "eldencraft.db");
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                this.connection = DriverManager.getConnection(url);
                this.isMySQL = false;
                plugin.getLogger().info("¡Conexión a la base de datos SQLite establecida con éxito!");
            }
            createTables();

            // Ejecutar migraciones de esquema
            new SchemaManager(plugin, connection, isMySQL).migrate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al cerrar conexión de la base de datos: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Sintaxis compatible para la creación de tablas en MySQL y SQLite
            stmt.execute("CREATE TABLE IF NOT EXISTS ec_players (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "race VARCHAR(32), " +
                    "class VARCHAR(32), " +
                    "level INTEGER DEFAULT 1, " +
                    "xp INTEGER DEFAULT 0, " +
                    "gold INTEGER DEFAULT 0, " +
                    "fatigue INTEGER DEFAULT 0" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS ec_completed_missions (" +
                    "uuid VARCHAR(36), " +
                    "mission_id VARCHAR(64), " +
                    "PRIMARY KEY (uuid, mission_id)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS ec_active_missions (" +
                    "uuid VARCHAR(36), " +
                    "mission_id VARCHAR(64), " +
                    "progress INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (uuid, mission_id)" +
                    ");");
        }
    }

    public PlayerData load(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        if (connection == null) {
            plugin.getLogger().warning("No hay conexión activa con la base de datos.");
            data.setGold(plugin.getEconomyManager().getStartingGold());
            return data;
        }

        // 1. Si existe un personaje activo en ec_characters, cargar ese
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT character_id FROM ec_characters WHERE player_uuid = ? AND is_active = 1 AND deleted_at IS NULL LIMIT 1;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return loadCharacterData(rs.getString("character_id"), uuid);
                }
            }
        } catch (SQLException ignored) {}

        // 2. Si no hay activo pero hay personajes, cargar el primero por slot
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT character_id FROM ec_characters WHERE player_uuid = ? AND deleted_at IS NULL ORDER BY slot ASC LIMIT 1;")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return loadCharacterData(rs.getString("character_id"), uuid);
                }
            }
        } catch (SQLException ignored) {}

        // Fallback a ec_players si no hay registros en ec_characters
        String sqlSelect = "SELECT * FROM ec_players WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sqlSelect)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String race = rs.getString("race");
                    if (race != null) {
                        try { data.setRace(RaceType.valueOf(race)); }
                        catch (IllegalArgumentException ignored) {}
                    }

                    String clazz = rs.getString("class");
                    if (clazz != null) {
                        try { data.setPlayerClass(ClassType.valueOf(clazz)); }
                        catch (IllegalArgumentException ignored) {}
                    }

                    data.setLevel(rs.getInt("level"));
                    data.setXp(rs.getInt("xp"));
                    data.setGold(rs.getInt("gold"));
                    data.setFatigue(rs.getInt("fatigue"));

                    // F1: PG D&D (columnas opcionales — pueden no existir todavía)
                    safeLoadInt(rs, "hp_current", v -> data.setDndHpCurrent(v));
                    safeLoadInt(rs, "hp_max",     v -> data.setDndHpMax(v));

                    // F2: Atributos base (columnas opcionales)
                    safeLoadInt(rs, "base_str", v -> data.setBaseStr(v));
                    safeLoadInt(rs, "base_dex", v -> data.setBaseDex(v));
                    safeLoadInt(rs, "base_con", v -> data.setBaseCon(v));
                    safeLoadInt(rs, "base_int", v -> data.setBaseInt(v));
                    safeLoadInt(rs, "base_wis", v -> data.setBaseWis(v));
                    safeLoadInt(rs, "base_cha", v -> data.setBaseCha(v));

                } else {
                    data.setGold(plugin.getEconomyManager().getStartingGold());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando datos de " + uuid + ": " + e.getMessage());
        }

        // Carga misiones completadas
        String sqlCompleted = "SELECT mission_id FROM ec_completed_missions WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sqlCompleted)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    data.getCompletedMissions().add(rs.getString("mission_id"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando misiones completadas de " + uuid + ": " + e.getMessage());
        }

        // Carga misiones activas
        String sqlActive = "SELECT mission_id, progress FROM ec_active_missions WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sqlActive)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String mId = rs.getString("mission_id");
                    int prog = rs.getInt("progress");
                    data.getActiveMissions().put(mId, new MissionProgress(mId, prog));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando misiones activas de " + uuid + ": " + e.getMessage());
        }

        // F3: Cargar espacios de conjuro
        loadSpellSlots(uuid, data);

        return data;
    }

    public synchronized void save(PlayerData data) {
        if (data == null || connection == null) return;

        // Guardar o reemplazar datos principales del jugador (sintaxis dual)
        String sqlSavePlayer;
        if (isMySQL) {
            sqlSavePlayer = "INSERT INTO ec_players (uuid, race, class, level, xp, gold, fatigue, " +
                    "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE race=VALUES(race), class=VALUES(class), level=VALUES(level), " +
                    "xp=VALUES(xp), gold=VALUES(gold), fatigue=VALUES(fatigue), " +
                    "hp_current=VALUES(hp_current), hp_max=VALUES(hp_max), " +
                    "base_str=VALUES(base_str), base_dex=VALUES(base_dex), base_con=VALUES(base_con), " +
                    "base_int=VALUES(base_int), base_wis=VALUES(base_wis), base_cha=VALUES(base_cha);";
        } else {
            sqlSavePlayer = "INSERT OR REPLACE INTO ec_players " +
                    "(uuid, race, class, level, xp, gold, fatigue, " +
                    "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sqlSavePlayer)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getRace() != null ? data.getRace().name() : null);
            stmt.setString(3, data.getPlayerClass() != null ? data.getPlayerClass().name() : null);
            stmt.setInt(4, data.getLevel());
            stmt.setInt(5, data.getXp());
            stmt.setInt(6, data.getGold());
            stmt.setInt(7, data.getFatigue());
            stmt.setInt(8, data.getDndHpCurrent());
            stmt.setInt(9, data.getDndHpMax());
            stmt.setInt(10, data.getBaseStr());
            stmt.setInt(11, data.getBaseDex());
            stmt.setInt(12, data.getBaseCon());
            stmt.setInt(13, data.getBaseInt());
            stmt.setInt(14, data.getBaseWis());
            stmt.setInt(15, data.getBaseCha());
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Fallback: intentar guardar solo las columnas básicas si las nuevas no existen aún
            saveLegacy(data);
        }

        // F3B-1: Sincronizar ec_characters si existe characterId
        if (data.getCharacterId() != null) {
            String sqlChar = isMySQL
                ? "INSERT INTO ec_characters (character_id, player_uuid, slot, name, race, class, level, xp, gold, fatigue, " +
                  "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, campaign_id, last_played, proficiencies, alignment, background) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE name=VALUES(name), race=VALUES(race), class=VALUES(class), level=VALUES(level), " +
                  "xp=VALUES(xp), gold=VALUES(gold), fatigue=VALUES(fatigue), hp_current=VALUES(hp_current), hp_max=VALUES(hp_max), " +
                  "base_str=VALUES(base_str), base_dex=VALUES(base_dex), base_con=VALUES(base_con), base_int=VALUES(base_int), base_wis=VALUES(base_wis), base_cha=VALUES(base_cha), " +
                  "campaign_id=VALUES(campaign_id), last_played=VALUES(last_played), proficiencies=VALUES(proficiencies), alignment=VALUES(alignment), background=VALUES(background);"
                : "INSERT OR REPLACE INTO ec_characters (character_id, player_uuid, slot, name, race, class, level, xp, gold, fatigue, " +
                  "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, campaign_id, last_played, proficiencies, alignment, background) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement stmt = connection.prepareStatement(sqlChar)) {
                stmt.setString(1, data.getCharacterId());
                stmt.setString(2, data.getUuid().toString());
                stmt.setInt(3, data.getSlot());
                stmt.setString(4, data.getCharacterName());
                stmt.setString(5, data.getRace() != null ? data.getRace().name() : null);
                stmt.setString(6, data.getPlayerClass() != null ? data.getPlayerClass().name() : null);
                stmt.setInt(7, data.getLevel());
                stmt.setInt(8, data.getXp());
                stmt.setInt(9, data.getGold());
                stmt.setInt(10, data.getFatigue());
                stmt.setInt(11, data.getDndHpCurrent());
                stmt.setInt(12, data.getDndHpMax());
                stmt.setInt(13, data.getBaseStr());
                stmt.setInt(14, data.getBaseDex());
                stmt.setInt(15, data.getBaseCon());
                stmt.setInt(16, data.getBaseInt());
                stmt.setInt(17, data.getBaseWis());
                stmt.setInt(18, data.getBaseCha());
                stmt.setString(19, data.getCampaignId());
                stmt.setLong(20, System.currentTimeMillis());
                StringBuilder sb = new StringBuilder();
                for (var sd : data.getProficiencies()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(sd.name());
                }
                stmt.setString(21, sb.toString());
                stmt.setString(22, data.getAlignment());
                stmt.setString(23, data.getBackground());
                stmt.executeUpdate();
            } catch (SQLException ignored) {}
        }

        // Guardar misiones completadas
        String sqlDeleteCompleted = "DELETE FROM ec_completed_missions WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteCompleted)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al limpiar misiones completadas de " + data.getUuid() + ": " + e.getMessage());
        }

        if (!data.getCompletedMissions().isEmpty()) {
            String sqlInsertCompleted = "INSERT INTO ec_completed_missions (uuid, mission_id) VALUES (?, ?);";
            try (PreparedStatement stmt = connection.prepareStatement(sqlInsertCompleted)) {
                connection.setAutoCommit(false);
                for (String missionId : data.getCompletedMissions()) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, missionId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().warning("Error al guardar misiones completadas de " + data.getUuid() + ": " + e.getMessage());
                try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }

        // Guardar misiones activas
        String sqlDeleteActive = "DELETE FROM ec_active_missions WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteActive)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al limpiar misiones activas de " + data.getUuid() + ": " + e.getMessage());
        }

        if (!data.getActiveMissions().isEmpty()) {
            String sqlInsertActive = "INSERT INTO ec_active_missions (uuid, mission_id, progress) VALUES (?, ?, ?);";
            try (PreparedStatement stmt = connection.prepareStatement(sqlInsertActive)) {
                connection.setAutoCommit(false);
                for (MissionProgress mp : data.getActiveMissions().values()) {
                    stmt.setString(1, data.getUuid().toString());
                    stmt.setString(2, mp.getMissionId());
                    stmt.setInt(3, mp.getProgress());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                plugin.getLogger().warning("Error al guardar misiones activas de " + data.getUuid() + ": " + e.getMessage());
                try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }

        // F3: Guardar espacios de conjuro
        saveSpellSlots(data);
    }

    /** Fallback: guarda solo columnas básicas cuando las columnas nuevas aún no existen. */
    private void saveLegacy(PlayerData data) {
        String sql = isMySQL
            ? "INSERT INTO ec_players (uuid, race, class, level, xp, gold, fatigue) VALUES (?, ?, ?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE race=VALUES(race), class=VALUES(class), level=VALUES(level), xp=VALUES(xp), gold=VALUES(gold), fatigue=VALUES(fatigue);"
            : "INSERT OR REPLACE INTO ec_players (uuid, race, class, level, xp, gold, fatigue) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getRace() != null ? data.getRace().name() : null);
            stmt.setString(3, data.getPlayerClass() != null ? data.getPlayerClass().name() : null);
            stmt.setInt(4, data.getLevel());
            stmt.setInt(5, data.getXp());
            stmt.setInt(6, data.getGold());
            stmt.setInt(7, data.getFatigue());
            stmt.executeUpdate();
        } catch (SQLException e2) {
            plugin.getLogger().warning("Error al guardar datos básicos de " + data.getUuid() + ": " + e2.getMessage());
        }
    }

    private void loadSpellSlots(UUID uuid, PlayerData data) {
        try {
            String sql = "SELECT slot_level, max_slots, used_slots FROM ec_spell_slots WHERE uuid = ?;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int lvl = rs.getInt("slot_level") - 1; // 0-indexed
                        if (lvl >= 0 && lvl < 9) {
                            data.getSpellSlotsMax()[lvl] = rs.getInt("max_slots");
                            data.getSpellSlotsUsed()[lvl] = rs.getInt("used_slots");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // tabla puede no existir aún (antes de v3)
        }
    }

    private void saveSpellSlots(PlayerData data) {
        try {
            String deleteSql = "DELETE FROM ec_spell_slots WHERE uuid = ?;";
            try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
                stmt.setString(1, data.getUuid().toString());
                stmt.executeUpdate();
            }

            String insertSql = isMySQL
                ? "INSERT INTO ec_spell_slots (uuid, slot_level, max_slots, used_slots) VALUES (?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE max_slots=VALUES(max_slots), used_slots=VALUES(used_slots);"
                : "INSERT OR REPLACE INTO ec_spell_slots (uuid, slot_level, max_slots, used_slots) VALUES (?, ?, ?, ?);";

            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                for (int i = 0; i < 9; i++) {
                    if (data.getSpellSlotsMax()[i] > 0) {
                        stmt.setString(1, data.getUuid().toString());
                        stmt.setInt(2, i + 1);
                        stmt.setInt(3, data.getSpellSlotsMax()[i]);
                        stmt.setInt(4, data.getSpellSlotsUsed()[i]);
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // tabla puede no existir aún
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public synchronized void delete(UUID uuid) {
        if (connection == null) return;
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_players WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString()); stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_completed_missions WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString()); stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_active_missions WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString()); stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_spell_slots WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString()); stmt.executeUpdate();
            } catch (Exception ignored) {}
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_spells_known WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString()); stmt.executeUpdate();
            } catch (Exception ignored) {}

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al borrar datos de " + uuid + ": " + e.getMessage());
            try { connection.rollback(); connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    public synchronized void addSpellKnown(String characterId, UUID uuid, String spellId) {
        if (connection == null || characterId == null || spellId == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO ec_spells_known (uuid, character_id, spell_id) VALUES (?, ?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, characterId);
            stmt.setString(3, spellId);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public synchronized void addSpellPrepared(String characterId, UUID uuid, String spellId) {
        if (connection == null || characterId == null || spellId == null) return;
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO ec_spells_prepared (uuid, character_id, spell_id) VALUES (?, ?, ?);")) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, characterId);
            stmt.setString(3, spellId);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface IntSetter { void set(int v); }

    private void safeLoadInt(ResultSet rs, String column, IntSetter setter) {
        try {
            int v = rs.getInt(column);
            if (!rs.wasNull()) setter.set(v);
        } catch (SQLException ignored) {
            // columna no existe todavía → ignorar
        }
    }

    // ── F3B-1: Multi-character y Perfiles (SPEC 15) ─────────────────────────

    public List<CharacterProfile> getCharacterProfiles(UUID playerUuid) {
        List<CharacterProfile> list = new ArrayList<>();
        if (connection == null || playerUuid == null) return list;

        String sql = "SELECT * FROM ec_characters WHERE player_uuid = ? ORDER BY slot ASC;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProfile(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error listando personajes de " + playerUuid + ": " + e.getMessage());
        }
        return list;
    }

    public CharacterProfile getCharacterProfile(String characterId) {
        if (connection == null || characterId == null) return null;
        String sql = "SELECT * FROM ec_characters WHERE character_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, characterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfile(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error obteniendo personaje " + characterId + ": " + e.getMessage());
        }
        return null;
    }

    private CharacterProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        CharacterProfile p = new CharacterProfile(
                rs.getString("character_id"),
                UUID.fromString(rs.getString("player_uuid")),
                rs.getInt("slot"),
                rs.getString("name")
        );
        String race = rs.getString("race");
        if (race != null) { try { p.setRace(RaceType.valueOf(race)); } catch (Exception ignored) {} }
        String clazz = rs.getString("class");
        if (clazz != null) { try { p.setPlayerClass(ClassType.valueOf(clazz)); } catch (Exception ignored) {} }
        p.setSubclass(rs.getString("subclass"));
        p.setLevel(rs.getInt("level"));
        p.setXp(rs.getInt("xp"));
        p.setGold(rs.getInt("gold"));
        p.setFatigue(rs.getInt("fatigue"));
        p.setHpCurrent(rs.getInt("hp_current"));
        p.setHpMax(rs.getInt("hp_max"));
        p.setBaseStr(rs.getInt("base_str"));
        p.setBaseDex(rs.getInt("base_dex"));
        p.setBaseCon(rs.getInt("base_con"));
        p.setBaseInt(rs.getInt("base_int"));
        p.setBaseWis(rs.getInt("base_wis"));
        p.setBaseCha(rs.getInt("base_cha"));
        p.setCampaignId(rs.getString("campaign_id"));
        p.setCreatedAt(rs.getLong("created_at"));
        p.setLastPlayed(rs.getLong("last_played"));
        long del = rs.getLong("deleted_at");
        if (!rs.wasNull()) p.setDeletedAt(del);
        p.setActive(rs.getInt("is_active") == 1);
        p.setAlignment(rs.getString("alignment"));
        p.setBackground(rs.getString("background"));
        String profs = rs.getString("proficiencies");
        if (profs != null && !profs.isEmpty()) {
            for (String s : profs.split(",")) {
                var sk = SkillDefinition.fromString(s.trim());
                if (sk != null) p.getProficiencies().add(sk);
            }
        }
        return p;
    }

    public synchronized void saveCharacterProfile(CharacterProfile p) {
        if (connection == null || p == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_characters (character_id, player_uuid, slot, name, race, class, subclass, level, xp, gold, fatigue, " +
              "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, campaign_id, created_at, last_played, deleted_at, is_active, proficiencies, alignment, background) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE name=VALUES(name), race=VALUES(race), class=VALUES(class), subclass=VALUES(subclass), " +
              "level=VALUES(level), xp=VALUES(xp), gold=VALUES(gold), fatigue=VALUES(fatigue), hp_current=VALUES(hp_current), hp_max=VALUES(hp_max), " +
              "base_str=VALUES(base_str), base_dex=VALUES(base_dex), base_con=VALUES(base_con), base_int=VALUES(base_int), base_wis=VALUES(base_wis), base_cha=VALUES(base_cha), " +
              "campaign_id=VALUES(campaign_id), last_played=VALUES(last_played), deleted_at=VALUES(deleted_at), is_active=VALUES(is_active), proficiencies=VALUES(proficiencies), " +
              "alignment=VALUES(alignment), background=VALUES(background);"
            : "INSERT OR REPLACE INTO ec_characters (character_id, player_uuid, slot, name, race, class, subclass, level, xp, gold, fatigue, " +
              "hp_current, hp_max, base_str, base_dex, base_con, base_int, base_wis, base_cha, campaign_id, created_at, last_played, deleted_at, is_active, proficiencies, alignment, background) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, p.getCharacterId());
            stmt.setString(2, p.getPlayerUuid().toString());
            stmt.setInt(3, p.getSlot());
            stmt.setString(4, p.getName());
            stmt.setString(5, p.getRace() != null ? p.getRace().name() : null);
            stmt.setString(6, p.getPlayerClass() != null ? p.getPlayerClass().name() : null);
            stmt.setString(7, p.getSubclass());
            stmt.setInt(8, p.getLevel());
            stmt.setInt(9, p.getXp());
            stmt.setInt(10, p.getGold());
            stmt.setInt(11, p.getFatigue());
            stmt.setInt(12, p.getHpCurrent());
            stmt.setInt(13, p.getHpMax());
            stmt.setInt(14, p.getBaseStr());
            stmt.setInt(15, p.getBaseDex());
            stmt.setInt(16, p.getBaseCon());
            stmt.setInt(17, p.getBaseInt());
            stmt.setInt(18, p.getBaseWis());
            stmt.setInt(19, p.getBaseCha());
            stmt.setString(20, p.getCampaignId());
            stmt.setLong(21, p.getCreatedAt());
            stmt.setLong(22, p.getLastPlayed());
            if (p.getDeletedAt() != null) {
                stmt.setLong(23, p.getDeletedAt());
            } else {
                stmt.setNull(23, java.sql.Types.BIGINT);
            }
            stmt.setInt(24, p.isActive() ? 1 : 0);
            StringBuilder sb = new StringBuilder();
            for (var sd : p.getProficiencies()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(sd.name());
            }
            stmt.setString(25, sb.toString());
            stmt.setString(26, p.getAlignment());
            stmt.setString(27, p.getBackground());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando perfil de personaje " + p.getCharacterId() + ": " + e.getMessage());
        }
    }

    public PlayerData loadCharacterData(String characterId, UUID playerUuid) {
        PlayerData data = new PlayerData(playerUuid, characterId);
        if (connection == null) return data;

        String sql = "SELECT * FROM ec_characters WHERE character_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, characterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    data.setSlot(rs.getInt("slot"));
                    data.setCharacterName(rs.getString("name"));
                    String race = rs.getString("race");
                    if (race != null) {
                        try { data.setRace(RaceType.valueOf(race)); } catch (Exception ignored) {}
                    }
                    String clazz = rs.getString("class");
                    if (clazz != null) {
                        try { data.setPlayerClass(ClassType.valueOf(clazz)); } catch (Exception ignored) {}
                    }
                    data.setLevel(rs.getInt("level"));
                    data.setXp(rs.getInt("xp"));
                    data.setGold(rs.getInt("gold"));
                    data.setFatigue(rs.getInt("fatigue"));
                    data.setDndHpCurrent(rs.getInt("hp_current"));
                    data.setDndHpMax(rs.getInt("hp_max"));
                    data.setBaseStr(rs.getInt("base_str"));
                    data.setBaseDex(rs.getInt("base_dex"));
                    data.setBaseCon(rs.getInt("base_con"));
                    data.setBaseInt(rs.getInt("base_int"));
                    data.setBaseWis(rs.getInt("base_wis"));
                    data.setBaseCha(rs.getInt("base_cha"));
                    data.setCampaignId(rs.getString("campaign_id"));
                    data.setAlignment(rs.getString("alignment"));
                    data.setBackground(rs.getString("background"));
                    String profs = rs.getString("proficiencies");
                    if (profs != null && !profs.isEmpty()) {
                        for (String s : profs.split(",")) {
                            var sk = SkillDefinition.fromString(s.trim());
                            if (sk != null) data.getProficiencies().add(sk);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando personaje " + characterId + ": " + e.getMessage());
        }

        // Cargar misiones y espacios de conjuro por character_id
        loadMissionsForCharacter(characterId, playerUuid, data);
        loadSpellSlotsForCharacter(characterId, playerUuid, data);

        return data;
    }

    private void loadMissionsForCharacter(String characterId, UUID playerUuid, PlayerData data) {
        String sqlCompleted = "SELECT mission_id FROM ec_completed_missions WHERE character_id = ? OR (character_id IS NULL AND uuid = ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sqlCompleted)) {
            stmt.setString(1, characterId);
            stmt.setString(2, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    data.getCompletedMissions().add(rs.getString("mission_id"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando misiones completadas de " + characterId + ": " + e.getMessage());
        }

        String sqlActive = "SELECT mission_id, progress FROM ec_active_missions WHERE character_id = ? OR (character_id IS NULL AND uuid = ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sqlActive)) {
            stmt.setString(1, characterId);
            stmt.setString(2, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String mId = rs.getString("mission_id");
                    int prog = rs.getInt("progress");
                    data.getActiveMissions().put(mId, new MissionProgress(mId, prog));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando misiones activas de " + characterId + ": " + e.getMessage());
        }
    }

    private void loadSpellSlotsForCharacter(String characterId, UUID playerUuid, PlayerData data) {
        try {
            String sql = "SELECT slot_level, max_slots, used_slots FROM ec_spell_slots WHERE character_id = ? OR (character_id IS NULL AND uuid = ?);";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, characterId);
                stmt.setString(2, playerUuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int lvl = rs.getInt("slot_level") - 1;
                        if (lvl >= 0 && lvl < 9) {
                            data.getSpellSlotsMax()[lvl] = rs.getInt("max_slots");
                            data.getSpellSlotsUsed()[lvl] = rs.getInt("used_slots");
                        }
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    public synchronized void setActiveCharacterProfile(UUID playerUuid, String characterId) {
        if (connection == null) return;
        try {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE ec_characters SET is_active = 0 WHERE player_uuid = ?;")) {
                stmt.setString(1, playerUuid.toString());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE ec_characters SET is_active = 1, last_played = ? WHERE character_id = ?;")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setString(2, characterId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error marcando personaje activo " + characterId + ": " + e.getMessage());
        }
    }

    public synchronized void saveCharacterInventory(String characterId, String inv, String armor, String offhand, String echest,
                                                     String world, double x, double y, double z, float yaw, float pitch, String gameMode) {
        if (connection == null || characterId == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_character_inventory (character_id, inventory_data, armor_data, offhand_data, ender_chest_data, " +
              "location_world, location_x, location_y, location_z, location_yaw, location_pitch, game_mode) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE inventory_data=VALUES(inventory_data), armor_data=VALUES(armor_data), " +
              "offhand_data=VALUES(offhand_data), ender_chest_data=VALUES(ender_chest_data), " +
              "location_world=VALUES(location_world), location_x=VALUES(location_x), location_y=VALUES(location_y), " +
              "location_z=VALUES(location_z), location_yaw=VALUES(location_yaw), location_pitch=VALUES(location_pitch), game_mode=VALUES(game_mode);"
            : "INSERT OR REPLACE INTO ec_character_inventory (character_id, inventory_data, armor_data, offhand_data, ender_chest_data, " +
              "location_world, location_x, location_y, location_z, location_yaw, location_pitch, game_mode) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, characterId);
            stmt.setString(2, inv);
            stmt.setString(3, armor);
            stmt.setString(4, offhand);
            stmt.setString(5, echest);
            stmt.setString(6, world);
            stmt.setDouble(7, x);
            stmt.setDouble(8, y);
            stmt.setDouble(9, z);
            stmt.setFloat(10, yaw);
            stmt.setFloat(11, pitch);
            stmt.setString(12, gameMode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando inventario de " + characterId + ": " + e.getMessage());
        }
    }

    public CharacterInventoryData loadCharacterInventory(String characterId) {
        if (connection == null || characterId == null) return null;
        String sql = "SELECT * FROM ec_character_inventory WHERE character_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, characterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CharacterInventoryData(
                            rs.getString("inventory_data"),
                            rs.getString("armor_data"),
                            rs.getString("offhand_data"),
                            rs.getString("ender_chest_data"),
                            rs.getString("location_world"),
                            rs.getDouble("location_x"),
                            rs.getDouble("location_y"),
                            rs.getDouble("location_z"),
                            rs.getFloat("location_yaw"),
                            rs.getFloat("location_pitch"),
                            rs.getString("game_mode")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error cargando inventario de " + characterId + ": " + e.getMessage());
        }
        return null;
    }

    public synchronized boolean renameCharacter(String characterId, String newName) {
        if (connection == null || characterId == null || newName == null) return false;
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE ec_characters SET name = ? WHERE character_id = ?;")) {
            stmt.setString(1, newName);
            stmt.setString(2, characterId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                logCharacterHistory(characterId, "RENOMBRAR", "Personaje renombrado a: " + newName, "JUGADOR");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al renombrar personaje " + characterId + ": " + e.getMessage());
        }
        return false;
    }

    public synchronized boolean softDeleteCharacter(String characterId) {
        if (connection == null || characterId == null) return false;
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE ec_characters SET deleted_at = ?, is_active = 0 WHERE character_id = ?;")) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, characterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al borrar personaje " + characterId + ": " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean recoverCharacter(String characterId) {
        if (connection == null || characterId == null) return false;
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE ec_characters SET deleted_at = NULL WHERE character_id = ?;")) {
            stmt.setString(1, characterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error al recuperar personaje " + characterId + ": " + e.getMessage());
            return false;
        }
    }

    public synchronized CharacterProfile getActiveCharacterProfile(UUID playerUuid) {
        if (connection == null || playerUuid == null) return null;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM ec_characters WHERE player_uuid = ? AND is_active = 1 AND deleted_at IS NULL LIMIT 1;")) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfile(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error obteniendo perfil activo de " + playerUuid + ": " + e.getMessage());
        }
        return null;
    }

    public synchronized String getActiveCharacterId(UUID playerUuid) {
        if (connection == null || playerUuid == null) return null;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT character_id FROM ec_characters WHERE player_uuid = ? AND is_active = 1 AND deleted_at IS NULL LIMIT 1;")) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("character_id");
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public synchronized int purgeDeletedCharacters(int graceDays) {
        if (connection == null) return 0;
        long cutoff = System.currentTimeMillis() - (graceDays * 24L * 3600L * 1000L);
        int count = 0;
        try {
            List<String> toPurge = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement("SELECT character_id FROM ec_characters WHERE deleted_at IS NOT NULL AND deleted_at < ?;")) {
                stmt.setLong(1, cutoff);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        toPurge.add(rs.getString("character_id"));
                    }
                }
            }

            for (String charId : toPurge) {
                logCharacterHistory(charId, "PURGA_AUTOMATICA", "Personaje purgado definitivamente tras 7 días", "SISTEMA");
                try (PreparedStatement delInv = connection.prepareStatement("DELETE FROM ec_character_inventory WHERE character_id = ?;")) {
                    delInv.setString(1, charId);
                    delInv.executeUpdate();
                }
                try (PreparedStatement delChar = connection.prepareStatement("DELETE FROM ec_characters WHERE character_id = ?;")) {
                    delChar.setString(1, charId);
                    delChar.executeUpdate();
                }
                count++;
            }
            if (count > 0 && plugin != null) {
                plugin.getLogger().info("[CharacterManager] Purgados " + count + " personajes borrados hace más de " + graceDays + " días.");
            }
        } catch (SQLException e) {
            if (plugin != null) {
                plugin.getLogger().warning("Error en purga de personajes: " + e.getMessage());
            }
        }
        return count;
    }

    // ── F3B-2: Gestión de Campañas (SPEC 15.4) ───────────────────────────────

    public List<Campaign> getAllCampaigns() {
        List<Campaign> list = new ArrayList<>();
        if (connection == null) return list;
        String sql = "SELECT * FROM ec_campaigns ORDER BY name ASC;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCampaign(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error listando campañas: " + e.getMessage());
        }
        return list;
    }

    public Campaign getCampaign(String id) {
        if (connection == null || id == null) return null;
        String sql = "SELECT * FROM ec_campaigns WHERE id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCampaign(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error obteniendo campaña " + id + ": " + e.getMessage());
        }
        return null;
    }

    public Campaign getCampaignByJoinCode(String code) {
        if (connection == null || code == null) return null;
        String sql = "SELECT * FROM ec_campaigns WHERE join_code = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToCampaign(rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error buscando campaña por código " + code + ": " + e.getMessage());
        }
        return null;
    }

    private Campaign mapResultSetToCampaign(ResultSet rs) throws SQLException {
        Campaign c = new Campaign(
                rs.getString("id"),
                rs.getString("name"),
                UUID.fromString(rs.getString("dm_uuid")),
                rs.getString("world_name")
        );
        c.setDescription(rs.getString("description"));
        c.setIcon(rs.getString("icon"));
        c.setSpawnX(rs.getDouble("spawn_x"));
        c.setSpawnY(rs.getDouble("spawn_y"));
        c.setSpawnZ(rs.getDouble("spawn_z"));
        c.setStatus(CampaignStatus.fromString(rs.getString("status")));
        c.setMaxPlayers(rs.getInt("max_players"));
        c.setMinLevel(rs.getInt("min_level"));
        c.setMaxLevel(rs.getInt("max_level"));
        c.setJoinCode(rs.getString("join_code"));
        c.setCreatedAt(rs.getLong("created_at"));
        c.setLastSessionAt(rs.getLong("last_session_at"));
        return c;
    }

    public synchronized void saveCampaign(Campaign c) {
        if (connection == null || c == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_campaigns (id, name, description, icon, dm_uuid, world_name, spawn_x, spawn_y, spawn_z, status, max_players, min_level, max_level, join_code, created_at, last_session_at) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description), icon=VALUES(icon), dm_uuid=VALUES(dm_uuid), " +
              "world_name=VALUES(world_name), spawn_x=VALUES(spawn_x), spawn_y=VALUES(spawn_y), spawn_z=VALUES(spawn_z), " +
              "status=VALUES(status), max_players=VALUES(max_players), min_level=VALUES(min_level), max_level=VALUES(max_level), " +
              "join_code=VALUES(join_code), last_session_at=VALUES(last_session_at);"
            : "INSERT OR REPLACE INTO ec_campaigns (id, name, description, icon, dm_uuid, world_name, spawn_x, spawn_y, spawn_z, status, max_players, min_level, max_level, join_code, created_at, last_session_at) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, c.getId());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getDescription());
            stmt.setString(4, c.getIcon());
            stmt.setString(5, c.getDmUuid().toString());
            stmt.setString(6, c.getWorldName());
            stmt.setDouble(7, c.getSpawnX());
            stmt.setDouble(8, c.getSpawnY());
            stmt.setDouble(9, c.getSpawnZ());
            stmt.setString(10, c.getStatus().name());
            stmt.setInt(11, c.getMaxPlayers());
            stmt.setInt(12, c.getMinLevel());
            stmt.setInt(13, c.getMaxLevel());
            stmt.setString(14, c.getJoinCode());
            stmt.setLong(15, c.getCreatedAt());
            stmt.setLong(16, c.getLastSessionAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando campaña " + c.getId() + ": " + e.getMessage());
        }
    }

    public synchronized void deleteCampaign(String id) {
        if (connection == null || id == null) return;
        try {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_campaigns WHERE id = ?;")) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_campaign_members WHERE campaign_id = ?;")) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM ec_campaign_settings WHERE campaign_id = ?;")) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error eliminando campaña " + id + ": " + e.getMessage());
        }
    }

    public List<CampaignMember> getCampaignMembers(String campaignId) {
        List<CampaignMember> list = new ArrayList<>();
        if (connection == null || campaignId == null) return list;
        String sql = "SELECT * FROM ec_campaign_members WHERE campaign_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new CampaignMember(
                            rs.getString("campaign_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            CampaignRole.fromString(rs.getString("role")),
                            rs.getString("character_id"),
                            rs.getLong("joined_at")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error listando miembros de campaña " + campaignId + ": " + e.getMessage());
        }
        return list;
    }

    public CampaignMember getCampaignMember(String campaignId, UUID playerUuid) {
        if (connection == null || campaignId == null || playerUuid == null) return null;
        String sql = "SELECT * FROM ec_campaign_members WHERE campaign_id = ? AND player_uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CampaignMember(
                            rs.getString("campaign_id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            CampaignRole.fromString(rs.getString("role")),
                            rs.getString("character_id"),
                            rs.getLong("joined_at")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error obteniendo miembro de campaña: " + e.getMessage());
        }
        return null;
    }

    public synchronized void saveCampaignMember(CampaignMember m) {
        if (connection == null || m == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_campaign_members (campaign_id, player_uuid, role, character_id, joined_at) " +
              "VALUES (?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE role=VALUES(role), character_id=VALUES(character_id), joined_at=VALUES(joined_at);"
            : "INSERT OR REPLACE INTO ec_campaign_members (campaign_id, player_uuid, role, character_id, joined_at) " +
              "VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, m.getCampaignId());
            stmt.setString(2, m.getPlayerUuid().toString());
            stmt.setString(3, m.getRole().name());
            stmt.setString(4, m.getCharacterId());
            stmt.setLong(5, m.getJoinedAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando miembro de campaña: " + e.getMessage());
        }
    }

    public synchronized void removeCampaignMember(String campaignId, UUID playerUuid) {
        if (connection == null || campaignId == null || playerUuid == null) return;
        String sql = "DELETE FROM ec_campaign_members WHERE campaign_id = ? AND player_uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error quitando miembro de campaña: " + e.getMessage());
        }
    }

    public synchronized void setCampaignSetting(String campaignId, String key, String value) {
        if (connection == null || campaignId == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_campaign_settings (campaign_id, setting_key, setting_value) VALUES (?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);"
            : "INSERT OR REPLACE INTO ec_campaign_settings (campaign_id, setting_key, setting_value) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, key);
            stmt.setString(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando ajuste de campaña: " + e.getMessage());
        }
    }

    public String getCampaignSetting(String campaignId, String key, String def) {
        if (connection == null || campaignId == null) return def;
        String sql = "SELECT setting_value FROM ec_campaign_settings WHERE campaign_id = ? AND setting_key = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("setting_value");
            }
        } catch (SQLException ignored) {}
        return def;
    }

    public Map<String, String> getCampaignSettings(String campaignId) {
        Map<String, String> map = new HashMap<>();
        if (connection == null || campaignId == null) return map;
        String sql = "SELECT setting_key, setting_value FROM ec_campaign_settings WHERE campaign_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("setting_key"), rs.getString("setting_value"));
                }
            }
        } catch (SQLException ignored) {}
        return map;
    }

    public synchronized void saveCampaignInvite(String campaignId, UUID playerUuid, String inviteCode, String status) {
        if (connection == null || campaignId == null || playerUuid == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_campaign_invites (campaign_id, player_uuid, invite_code, status, created_at) VALUES (?, ?, ?, ?, ?) " +
              "ON DUPLICATE KEY UPDATE invite_code=VALUES(invite_code), status=VALUES(status);"
            : "INSERT OR REPLACE INTO ec_campaign_invites (campaign_id, player_uuid, invite_code, status, created_at) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, playerUuid.toString());
            stmt.setString(3, inviteCode);
            stmt.setString(4, status != null ? status : "PENDIENTE");
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando invitación a campaña: " + e.getMessage());
        }
    }

    public List<String> getPendingInvites(UUID playerUuid) {
        List<String> list = new ArrayList<>();
        if (connection == null || playerUuid == null) return list;
        String sql = "SELECT campaign_id FROM ec_campaign_invites WHERE player_uuid = ? AND status = 'PENDIENTE';";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("campaign_id"));
                }
            }
        } catch (SQLException ignored) {}
        return list;
    }

    public boolean hasPendingInvite(String campaignId, UUID playerUuid) {
        if (connection == null || campaignId == null || playerUuid == null) return false;
        String sql = "SELECT 1 FROM ec_campaign_invites WHERE campaign_id = ? AND player_uuid = ? AND status = 'PENDIENTE';";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {}
        return false;
    }

    public synchronized void updateInviteStatus(String campaignId, UUID playerUuid, String status) {
        if (connection == null || campaignId == null || playerUuid == null) return;
        String sql = "UPDATE ec_campaign_invites SET status = ? WHERE campaign_id = ? AND player_uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, campaignId);
            stmt.setString(3, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public boolean isBannedFromCampaign(String campaignId, UUID playerUuid) {
        if (connection == null || campaignId == null || playerUuid == null) return false;
        String sql = "SELECT 1 FROM ec_campaign_invites WHERE campaign_id = ? AND player_uuid = ? AND status = 'VETADO';";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {}
        return false;
    }

    public synchronized void banPlayerFromCampaign(String campaignId, UUID playerUuid) {
        saveCampaignInvite(campaignId, playerUuid, null, "VETADO");
        removeCampaignMember(campaignId, playerUuid);
    }

    // ── F3B-3: Sesiones, Notas e Historial del DM (SPEC 15.5) ────────────────

    public synchronized void addCampaignNote(String campaignId, String characterId, String text) {
        if (connection == null || campaignId == null || text == null) return;
        String sql = "INSERT INTO ec_campaign_notes (campaign_id, character_id, note_text, created_at) VALUES (?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            stmt.setString(2, characterId);
            stmt.setString(3, text);
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error guardando nota de campaña: " + e.getMessage());
        }
    }

    public List<String> getCampaignNotes(String campaignId) {
        List<String> notes = new ArrayList<>();
        if (connection == null || campaignId == null) return notes;
        String sql = "SELECT note_text, created_at FROM ec_campaign_notes WHERE campaign_id = ? ORDER BY id DESC LIMIT 20;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(rs.getString("note_text"));
                }
            }
        } catch (SQLException ignored) {}
        return notes;
    }

    public synchronized void startSession(String sessionId, String campaignId) {
        if (connection == null || sessionId == null || campaignId == null) return;
        String sql = isMySQL
            ? "INSERT INTO ec_sessions (session_id, campaign_id, started_at, ended_at) VALUES (?, ?, ?, 0) " +
              "ON DUPLICATE KEY UPDATE started_at=VALUES(started_at), ended_at=0;"
            : "INSERT OR REPLACE INTO ec_sessions (session_id, campaign_id, started_at, ended_at) VALUES (?, ?, ?, 0);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sessionId);
            stmt.setString(2, campaignId);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error iniciando sesión: " + e.getMessage());
        }
    }

    public synchronized void endSession(String sessionId, String notes, int xp, String loot) {
        if (connection == null || sessionId == null) return;
        String sql = "UPDATE ec_sessions SET ended_at = ?, notes = ?, xp_awarded = ?, loot_awarded = ? WHERE session_id = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.setString(2, notes != null ? notes : "");
            stmt.setInt(3, xp);
            stmt.setString(4, loot != null ? loot : "");
            stmt.setString(5, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error terminando sesión: " + e.getMessage());
        }
    }

    public String getActiveSessionId(String campaignId) {
        if (connection == null || campaignId == null) return null;
        String sql = "SELECT session_id FROM ec_sessions WHERE campaign_id = ? AND (ended_at IS NULL OR ended_at = 0) ORDER BY started_at DESC LIMIT 1;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, campaignId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("session_id");
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public synchronized void logCharacterHistory(String characterId, String actionType, String description, String changedBy) {
        if (connection == null || characterId == null) return;
        String sql = "INSERT INTO ec_character_history (character_id, timestamp, action_type, description, changed_by) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, characterId);
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, actionType);
            stmt.setString(4, description);
            stmt.setString(5, changedBy);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
