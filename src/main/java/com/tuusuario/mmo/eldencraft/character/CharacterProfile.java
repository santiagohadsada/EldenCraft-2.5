package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Representa el perfil de un personaje en un slot (1-27).
 */
public class CharacterProfile {

    private String characterId;
    private UUID playerUuid;
    private int slot;
    private String name;
    private RaceType race;
    private ClassType playerClass;
    private String subclass;
    private int level = 1;
    private int xp = 0;
    private int gold = 0;
    private int fatigue = 0;
    private int hpCurrent = 10;
    private int hpMax = 10;

    private int baseStr = 10;
    private int baseDex = 10;
    private int baseCon = 10;
    private int baseInt = 10;
    private int baseWis = 10;
    private int baseCha = 10;

    private String campaignId = "principal";
    private long createdAt = System.currentTimeMillis();
    private long lastPlayed = System.currentTimeMillis();
    private Long deletedAt = null;
    private boolean active = false;

    private final Set<SkillDefinition> proficiencies = new HashSet<>();
    private String alignment = "Neutral";
    private String background = "Aventurero";

    public CharacterProfile() {}

    public CharacterProfile(String characterId, UUID playerUuid, int slot, String name) {
        this.characterId = characterId;
        this.playerUuid = playerUuid;
        this.slot = slot;
        this.name = name;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canBeRecovered(int graceDays) {
        if (deletedAt == null) return false;
        long limit = deletedAt + (graceDays * 24L * 3600L * 1000L);
        return System.currentTimeMillis() <= limit;
    }

    // Getters y Setters
    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }

    public UUID getPlayerUuid() { return playerUuid; }
    public void setPlayerUuid(UUID playerUuid) { this.playerUuid = playerUuid; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RaceType getRace() { return race; }
    public void setRace(RaceType race) { this.race = race; }

    public ClassType getPlayerClass() { return playerClass; }
    public void setPlayerClass(ClassType playerClass) { this.playerClass = playerClass; }

    public String getSubclass() { return subclass; }
    public void setSubclass(String subclass) { this.subclass = subclass; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

    public int getFatigue() { return fatigue; }
    public void setFatigue(int fatigue) { this.fatigue = fatigue; }

    public int getHpCurrent() { return hpCurrent; }
    public void setHpCurrent(int hpCurrent) { this.hpCurrent = hpCurrent; }

    public int getHpMax() { return hpMax; }
    public void setHpMax(int hpMax) { this.hpMax = hpMax; }

    public int getBaseStr() { return baseStr; }
    public void setBaseStr(int baseStr) { this.baseStr = baseStr; }

    public int getBaseDex() { return baseDex; }
    public void setBaseDex(int baseDex) { this.baseDex = baseDex; }

    public int getBaseCon() { return baseCon; }
    public void setBaseCon(int baseCon) { this.baseCon = baseCon; }

    public int getBaseInt() { return baseInt; }
    public void setBaseInt(int baseInt) { this.baseInt = baseInt; }

    public int getBaseWis() { return baseWis; }
    public void setBaseWis(int baseWis) { this.baseWis = baseWis; }

    public int getBaseCha() { return baseCha; }
    public void setBaseCha(int baseCha) { this.baseCha = baseCha; }

    public String getCampaignId() { return campaignId != null ? campaignId : "principal"; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(long lastPlayed) { this.lastPlayed = lastPlayed; }

    public Long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Long deletedAt) { this.deletedAt = deletedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Set<SkillDefinition> getProficiencies() { return proficiencies; }

    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
}
