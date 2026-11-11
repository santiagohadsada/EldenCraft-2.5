package com.tuusuario.mmo.eldencraft.campaign;

import java.util.UUID;

/**
 * Representa la membresía de un jugador en una campaña (SPEC 15.4 / F3B-2).
 */
public class CampaignMember {

    private final String campaignId;
    private final UUID playerUuid;
    private CampaignRole role;
    private String characterId;
    private long joinedAt;

    public CampaignMember(String campaignId, UUID playerUuid, CampaignRole role, String characterId, long joinedAt) {
        this.campaignId = campaignId;
        this.playerUuid = playerUuid;
        this.role = role != null ? role : CampaignRole.JUGADOR;
        this.characterId = characterId;
        this.joinedAt = joinedAt > 0 ? joinedAt : System.currentTimeMillis();
    }

    public String getCampaignId() { return campaignId; }
    public UUID getPlayerUuid() { return playerUuid; }
    public CampaignRole getRole() { return role; }
    public void setRole(CampaignRole role) { this.role = role; }
    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
}
