package com.tuusuario.mmo.eldencraft.campaign;

import java.util.UUID;

/**
 * Modelo de una Campaña de Rol D&D (SPEC 15.4 / F3B-2).
 */
public class Campaign {

    private String id;
    private String name;
    private String description;
    private String icon = "COMPASS";
    private UUID dmUuid;
    private String worldName;
    private double spawnX = 0.5;
    private double spawnY = 64.0;
    private double spawnZ = 0.5;
    private CampaignStatus status = CampaignStatus.ABIERTA;
    private int maxPlayers = 8;
    private int minLevel = 1;
    private int maxLevel = 20;
    private String joinCode;
    private long createdAt = System.currentTimeMillis();
    private long lastSessionAt = 0;

    public Campaign() {}

    public Campaign(String id, String name, UUID dmUuid, String worldName) {
        this.id = id;
        this.name = name;
        this.dmUuid = dmUuid;
        this.worldName = worldName != null ? worldName : ("ec_camp_" + id);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon != null ? icon : "COMPASS"; }
    public void setIcon(String icon) { this.icon = icon; }

    public UUID getDmUuid() { return dmUuid; }
    public void setDmUuid(UUID dmUuid) { this.dmUuid = dmUuid; }

    public String getWorldName() { return worldName != null ? worldName : ("ec_camp_" + id); }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public double getSpawnX() { return spawnX; }
    public void setSpawnX(double spawnX) { this.spawnX = spawnX; }

    public double getSpawnY() { return spawnY; }
    public void setSpawnY(double spawnY) { this.spawnY = spawnY; }

    public double getSpawnZ() { return spawnZ; }
    public void setSpawnZ(double spawnZ) { this.spawnZ = spawnZ; }

    public CampaignStatus getStatus() { return status != null ? status : CampaignStatus.ABIERTA; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }

    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastSessionAt() { return lastSessionAt; }
    public void setLastSessionAt(long lastSessionAt) { this.lastSessionAt = lastSessionAt; }
}
