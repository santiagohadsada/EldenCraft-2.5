package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.campaign.Campaign;
import com.tuusuario.mmo.eldencraft.campaign.CampaignMember;
import com.tuusuario.mmo.eldencraft.campaign.CampaignRole;
import com.tuusuario.mmo.eldencraft.campaign.CampaignStatus;
import com.tuusuario.mmo.eldencraft.storage.SchemaManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CampaignTest {

    @Test
    public void testCampaignRoles() {
        assertTrue(CampaignRole.DM_PRINCIPAL.isDm);
        assertTrue(CampaignRole.DM_PRINCIPAL.isOwner);

        assertTrue(CampaignRole.CO_DM.isDm);
        assertFalse(CampaignRole.CO_DM.isOwner);

        assertFalse(CampaignRole.JUGADOR.isDm);
        assertFalse(CampaignRole.JUGADOR.isOwner);

        assertFalse(CampaignRole.ESPECTADOR.isDm);
        assertFalse(CampaignRole.ESPECTADOR.isOwner);
    }

    @Test
    public void testCampaignModel() {
        UUID dm = UUID.randomUUID();
        Campaign c = new Campaign("ravenloft", "Maldición de Strahd", dm, "ec_camp_ravenloft");
        c.setDescription("Gothic horror campaign");
        c.setMaxPlayers(6);
        c.setMinLevel(1);
        c.setMaxLevel(10);
        c.setStatus(CampaignStatus.ABIERTA);

        assertEquals("ravenloft", c.getId());
        assertEquals("Maldición de Strahd", c.getName());
        assertEquals("ec_camp_ravenloft", c.getWorldName());
        assertEquals(6, c.getMaxPlayers());
        assertEquals(1, c.getMinLevel());
        assertEquals(10, c.getMaxLevel());
        assertEquals(CampaignStatus.ABIERTA, c.getStatus());
    }

    @Test
    public void testCampaignPersistenceInSqlite() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            SchemaManager sm = new SchemaManager(null, conn, false);
            sm.migrate();

            UUID dm = UUID.randomUUID();
            UUID player1 = UUID.randomUUID();

            // Insertar campaña
            String insertCampaign = "INSERT INTO ec_campaigns (id, name, description, dm_uuid, world_name, status, max_players, join_code, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement stmt = conn.prepareStatement(insertCampaign)) {
                stmt.setString(1, "barovia");
                stmt.setString(2, "Barovia");
                stmt.setString(3, "Tierra de nieblas");
                stmt.setString(4, dm.toString());
                stmt.setString(5, "ec_camp_barovia");
                stmt.setString(6, CampaignStatus.ABIERTA.name());
                stmt.setInt(7, 6);
                stmt.setString(8, "STRAHD");
                stmt.setLong(9, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            // Insertar miembros
            String insertMember = "INSERT INTO ec_campaign_members (campaign_id, player_uuid, role, character_id, joined_at) " +
                    "VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement stmt = conn.prepareStatement(insertMember)) {
                stmt.setString(1, "barovia");
                stmt.setString(2, dm.toString());
                stmt.setString(3, CampaignRole.DM_PRINCIPAL.name());
                stmt.setString(4, dm + "_1");
                stmt.setLong(5, System.currentTimeMillis());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(insertMember)) {
                stmt.setString(1, "barovia");
                stmt.setString(2, player1.toString());
                stmt.setString(3, CampaignRole.JUGADOR.name());
                stmt.setString(4, player1 + "_1");
                stmt.setLong(5, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            // Validar consultas
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM ec_campaign_members WHERE campaign_id = ?;")) {
                stmt.setString(1, "barovia");
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(2, rs.getInt("total"));
                }
            }

            // Validar búsqueda por join_code
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM ec_campaigns WHERE join_code = ?;")) {
                stmt.setString(1, "STRAHD");
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("barovia", rs.getString("id"));
                    assertEquals("Barovia", rs.getString("name"));
                }
            }
        }
    }

    @Test
    public void testAllCampaignStatuses() {
        assertEquals(CampaignStatus.BORRADOR, CampaignStatus.fromString("BORRADOR"));
        assertEquals(CampaignStatus.ABIERTA, CampaignStatus.fromString("ABIERTA"));
        assertEquals(CampaignStatus.SOLO_INVITADOS, CampaignStatus.fromString("SOLO_INVITADOS"));
        assertEquals(CampaignStatus.EN_PAUSA, CampaignStatus.fromString("EN_PAUSA"));
        assertEquals(CampaignStatus.CERRADA, CampaignStatus.fromString("CERRADA"));
        assertEquals(CampaignStatus.ARCHIVADA, CampaignStatus.fromString("ARCHIVADA"));

        assertEquals("Borrador", CampaignStatus.BORRADOR.displayName);
        assertEquals("Solo Invitados", CampaignStatus.SOLO_INVITADOS.displayName);
    }

    @Test
    public void testCampaignInvitesAndBansInSqlite() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            SchemaManager sm = new SchemaManager(null, conn, false);
            sm.migrate();

            UUID target = UUID.randomUUID();
            String campId = "curse_of_strahd";

            // Insertar invitación pendiente
            String insertInvite = "INSERT INTO ec_campaign_invites (campaign_id, player_uuid, invite_code, status, created_at) VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement stmt = conn.prepareStatement(insertInvite)) {
                stmt.setString(1, campId);
                stmt.setString(2, target.toString());
                stmt.setString(3, "INV123");
                stmt.setString(4, "PENDIENTE");
                stmt.setLong(5, System.currentTimeMillis());
                stmt.executeUpdate();
            }

            // Consultar si tiene pendiente
            try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM ec_campaign_invites WHERE campaign_id = ? AND player_uuid = ? AND status = 'PENDIENTE';")) {
                stmt.setString(1, campId);
                stmt.setString(2, target.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Debe encontrar invitación pendiente");
                }
            }

            // Aceptar invitación
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE ec_campaign_invites SET status = 'ACEPTADA' WHERE campaign_id = ? AND player_uuid = ?;")) {
                stmt.setString(1, campId);
                stmt.setString(2, target.toString());
                assertEquals(1, stmt.executeUpdate());
            }

            // Vetar jugador
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE ec_campaign_invites SET status = 'VETADO' WHERE campaign_id = ? AND player_uuid = ?;")) {
                stmt.setString(1, campId);
                stmt.setString(2, target.toString());
                assertEquals(1, stmt.executeUpdate());
            }

            // Comprobar que está vetado
            try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM ec_campaign_invites WHERE campaign_id = ? AND player_uuid = ? AND status = 'VETADO';")) {
                stmt.setString(1, campId);
                stmt.setString(2, target.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Debe detectar que el jugador está vetado");
                }
            }
        }
    }
}
