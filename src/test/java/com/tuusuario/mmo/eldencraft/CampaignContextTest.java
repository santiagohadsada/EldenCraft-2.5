package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.storage.SchemaManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignContextTest {

    @Test
    public void testSessionsAndNotesPersistence() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            SchemaManager sm = new SchemaManager(null, conn, false);
            sm.migrate();

            String campId = "camp_dragon";
            String sessionId = "ses_101";

            // 1. Iniciar sesión
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO ec_sessions (session_id, campaign_id, started_at, ended_at) VALUES (?, ?, ?, 0);")) {
                stmt.setString(1, sessionId);
                stmt.setString(2, campId);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            // Verificar sesión activa
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT session_id FROM ec_sessions WHERE campaign_id = ? AND (ended_at IS NULL OR ended_at = 0);")) {
                stmt.setString(1, campId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(sessionId, rs.getString("session_id"));
                }
            }

            // 2. Finalizar sesión
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE ec_sessions SET ended_at = ?, xp_awarded = ?, notes = ? WHERE session_id = ?;")) {
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setInt(2, 500);
                stmt.setString(3, "Derrotaron al dragón joven.");
                stmt.setString(4, sessionId);
                stmt.executeUpdate();
            }

            // Verificar que no hay sesión activa pendiente
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT session_id FROM ec_sessions WHERE campaign_id = ? AND (ended_at IS NULL OR ended_at = 0);")) {
                stmt.setString(1, campId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertFalse(rs.next(), "No debe haber sesión activa pendiente tras finalizarla.");
                }
            }

            // 3. Notas de campaña
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO ec_campaign_notes (campaign_id, character_id, note_text, created_at) VALUES (?, ?, ?, ?);")) {
                stmt.setString(1, campId);
                stmt.setString(2, "char_1");
                stmt.setString(3, "Pista encontrada en la taberna.");
                stmt.setLong(4, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT note_text FROM ec_campaign_notes WHERE campaign_id = ?;")) {
                stmt.setString(1, campId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("Pista encontrada en la taberna.", rs.getString("note_text"));
                }
            }

            // 4. Historial de personaje
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO ec_character_history (character_id, timestamp, action_type, description, changed_by) VALUES (?, ?, ?, ?, ?);")) {
                stmt.setString(1, "char_1");
                stmt.setLong(2, System.currentTimeMillis());
                stmt.setString(3, "APROBACION");
                stmt.setString(4, "Ficha aprobada por DM");
                stmt.setString(5, "DM_Master");
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT action_type FROM ec_character_history WHERE character_id = ?;")) {
                stmt.setString(1, "char_1");
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("APROBACION", rs.getString("action_type"));
                }
            }
        }
    }
}
