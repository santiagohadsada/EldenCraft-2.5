package com.tuusuario.mmo.eldencraft.models;

import com.tuusuario.mmo.eldencraft.missions.MissionProgress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private RaceType race;
    private ClassType playerClass;
    private int level = 1;
    private int xp = 0;
    private double currentResource;
    private int fatigue = 0;

    // plata del jugador
    private int gold = 0;

    // misiones activas y las q ya completo
    private final Map<String, MissionProgress> activeMissions = new HashMap<>();
    private final Set<String> completedMissions = new HashSet<>();

    // ── F1: Sistema PG D&D ────────────────────────────────────────────────────
    private int dndHpCurrent = 0;
    private int dndHpMax = 0;

    // ── F2: Atributos base (standard array) ───────────────────────────────────
    // Separados de los bonos legacy de raza+clase
    private int baseStr = 0, baseDex = 0, baseCon = 0, baseInt = 0, baseWis = 0, baseCha = 0;

    // ── F3: Hechizos ─────────────────────────────────────────────────────────
    private int[] spellSlotsMax  = new int[9]; // índice 0 = nivel 1
    private int[] spellSlotsUsed = new int[9];
    private final Set<String> spellsKnown    = new HashSet<>();
    private final Set<String> spellsPrepared = new HashSet<>();
    private String concentrationSpell = null;

    // ── F1B: Multi-personaje y Campañas (SPEC 15) ───────────────────────────
    private String characterId;
    private int slot = 1;
    private String characterName;
    private String campaignId = "principal";
    private final Set<com.tuusuario.mmo.eldencraft.rules.SkillDefinition> proficiencies = new HashSet<>();
    private String alignment = "Neutral";
    private String background = "Aventurero";

    public PlayerData(UUID uuid) {
        this(uuid, uuid.toString() + "_1");
    }

    public PlayerData(UUID uuid, String characterId) {
        this.uuid = uuid;
        this.characterId = characterId != null ? characterId : uuid.toString() + "_1";
        this.currentResource = 100.0;
    }

    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = characterId; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public String getCharacterName() { return characterName != null ? characterName : (uuid != null ? uuid.toString().substring(0, 8) : "Personaje"); }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getCampaignId() { return campaignId != null ? campaignId : "principal"; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public Set<com.tuusuario.mmo.eldencraft.rules.SkillDefinition> getProficiencies() { return proficiencies; }
    public boolean hasProficiency(com.tuusuario.mmo.eldencraft.rules.SkillDefinition skill) { return proficiencies.contains(skill); }

    public String getAlignment() { return alignment; }
    public void setAlignment(String alignment) { this.alignment = alignment; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    // ── Stats D&D (legacy: raza + clase) ─────────────────────────────────────

    /**
     * Atributos efectivos: si el standard array fue asignado (baseStr > 0) lo usa;
     * si no, cae al sistema legacy (raza + clase) para retrocompatibilidad.
     */
    public int getEffectiveStr() {
        if (baseStr > 0) return baseStr + (playerClass != null ? playerClass.bStr : 0);
        return getStrength();
    }
    public int getEffectiveDex() {
        if (baseDex > 0) return baseDex + (playerClass != null ? playerClass.bDex : 0);
        return getDexterity();
    }
    public int getEffectiveCon() {
        if (baseCon > 0) return baseCon + (playerClass != null ? playerClass.bCon : 0);
        return getConstitution();
    }
    public int getEffectiveInt() {
        if (baseInt > 0) return baseInt + (playerClass != null ? playerClass.bInt : 0);
        return getIntelligence();
    }
    public int getEffectiveWis() {
        if (baseWis > 0) return baseWis + (playerClass != null ? playerClass.bWis : 0);
        return getWisdom();
    }
    public int getEffectiveCha() {
        if (baseCha > 0) return baseCha + (playerClass != null ? playerClass.bCha : 0);
        return getCharisma();
    }

    // stats finales legacy = lo de la raza + lo de la clase, se suman
    public int getStrength()     { return (race != null ? race.str   : 10) + (playerClass != null ? playerClass.bStr : 0); }
    public int getDexterity()    { return (race != null ? race.dex   : 10) + (playerClass != null ? playerClass.bDex : 0); }
    public int getConstitution() { return (race != null ? race.con   : 10) + (playerClass != null ? playerClass.bCon : 0); }
    public int getIntelligence() { return (race != null ? race.intel : 10) + (playerClass != null ? playerClass.bInt : 0); }
    public int getWisdom()       { return (race != null ? race.wis   : 10) + (playerClass != null ? playerClass.bWis : 0); }
    public int getCharisma()     { return (race != null ? race.cha   : 10) + (playerClass != null ? playerClass.bCha : 0); }

    // vida maxima, depende de la constitucion y el nivel
    public double getMaxHealth() {
        if (race == null) return 20.0;
        int totalCon = getConstitution();
        return race.baseHealth + ((totalCon - 10) * 1.0) + (level - 1) * 1.0;
    }

    // el mana o la energia, segun la clase q seas
    public double getMaxResource() {
        if (playerClass == null) return 100.0;
        // los magicos escalan con INT, los fisicos con CON y DEX
        if ("MANA".equals(playerClass.resourceType)) {
            return 100.0 + (getIntelligence() * 5.0);
        } else {
            return 100.0 + (getConstitution() * 3.0) + (getDexterity() * 2.0);
        }
    }

    public double getCurrentResource() { return currentResource; }

    public void setCurrentResource(double val) {
        this.currentResource = Math.max(0, Math.min(val, getMaxResource()));
    }

    // getters y setters normales, nada del otro mundo
    public UUID getUuid() { return uuid; }
    public RaceType getRace() { return race; }
    public void setRace(RaceType race) {
        this.race = race;
        this.currentResource = getMaxResource();
    }
    public ClassType getPlayerClass() { return playerClass; }
    public void setPlayerClass(ClassType playerClass) {
        this.playerClass = playerClass;
        this.currentResource = getMaxResource();
    }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(level, 20)); }
    public int getXp() { return xp; }
    public int getExp() { return xp; }
    public void setXp(int xp) { this.xp = Math.max(0, xp); }
    public int getFatigue() { return fatigue; }
    public void setFatigue(int fatigue) { this.fatigue = Math.max(0, Math.min(fatigue, 100)); }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }

    public Map<String, MissionProgress> getActiveMissions() { return activeMissions; }
    public Set<String> getCompletedMissions() { return completedMissions; }

    // ── F1: PG D&D ───────────────────────────────────────────────────────────

    public int getDndHpCurrent() { return dndHpCurrent; }
    public void setDndHpCurrent(int v) { this.dndHpCurrent = Math.max(0, v); }
    public int getDndHpMax() { return dndHpMax; }
    public void setDndHpMax(int v) { this.dndHpMax = Math.max(1, v); }

    // ── F2: Atributos base ────────────────────────────────────────────────────

    public int getBaseStr() { return baseStr; }
    public void setBaseStr(int v) { this.baseStr = v; }
    public int getBaseDex() { return baseDex; }
    public void setBaseDex(int v) { this.baseDex = v; }
    public int getBaseCon() { return baseCon; }
    public void setBaseCon(int v) { this.baseCon = v; }
    public int getBaseInt() { return baseInt; }
    public void setBaseInt(int v) { this.baseInt = v; }
    public int getBaseWis() { return baseWis; }
    public void setBaseWis(int v) { this.baseWis = v; }
    public int getBaseCha() { return baseCha; }
    public void setBaseCha(int v) { this.baseCha = v; }

    /** Devuelve true si el standard array ya fue asignado. */
    public boolean hasStandardArray() { return baseStr > 0; }

    // ── F3: Hechizos ─────────────────────────────────────────────────────────

    public int[] getSpellSlotsMax()  { return spellSlotsMax; }
    public int[] getSpellSlotsUsed() { return spellSlotsUsed; }
    public Set<String> getSpellsKnown()    { return spellsKnown; }
    public Set<String> getSpellsPrepared() { return spellsPrepared; }

    public String getConcentrationSpell() { return concentrationSpell; }
    public void setConcentrationSpell(String spellId) { this.concentrationSpell = spellId; }

    /** Verifica si tiene espacio disponible de nivel dado (1-9). */
    public boolean hasSpellSlot(int spellLevel) {
        if (spellLevel < 1 || spellLevel > 9) return false;
        return spellSlotsUsed[spellLevel - 1] < spellSlotsMax[spellLevel - 1];
    }

    /** Consume un espacio de conjuro. Devuelve false si no había. */
    public boolean useSpellSlot(int spellLevel) {
        if (!hasSpellSlot(spellLevel)) return false;
        spellSlotsUsed[spellLevel - 1]++;
        return true;
    }

    /** Restaura todos los espacios de conjuro. */
    public void restoreAllSpellSlots() {
        spellSlotsUsed = new int[9];
    }
}
