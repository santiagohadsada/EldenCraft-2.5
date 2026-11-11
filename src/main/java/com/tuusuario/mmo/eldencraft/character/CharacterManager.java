package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.LevelUpManager;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellSlotTable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor principal de personajes multi-slot (27 slots) y cambio atómico (SPEC 15.2 / F3B-1).
 */
public class CharacterManager {

    public static final int DEFAULT_MAX_SLOTS = 27;

    private final EldenCraft plugin;
    // Jugadores que están cambiando de personaje activamente (bloqueo atómico)
    private final Set<UUID> switchingLock = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // uuid -> active characterId
    private final Map<UUID, String> activeCharacterIds = new ConcurrentHashMap<>();

    public CharacterManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public int getMaxSlots() {
        return plugin.getConfig().getInt("characters.max-slots", DEFAULT_MAX_SLOTS);
    }

    public int getDeleteGraceDays() {
        return plugin.getConfig().getInt("characters.delete-grace-days", 7);
    }

    public boolean isSwitching(UUID uuid) {
        return switchingLock.contains(uuid);
    }

    public String getActiveCharacterId(UUID uuid) {
        String id = activeCharacterIds.get(uuid);
        if (id == null && plugin.getDataManager() != null) {
            id = plugin.getDataManager().getActiveCharacterId(uuid);
            if (id != null) {
                activeCharacterIds.put(uuid, id);
            }
        }
        return id;
    }

    public int purgeExpiredDeletedCharacters() {
        if (plugin.getDataManager() == null) return 0;
        return plugin.getDataManager().purgeDeletedCharacters(getDeleteGraceDays());
    }

    public void setActiveCharacterId(UUID uuid, String charId) {
        if (charId != null) activeCharacterIds.put(uuid, charId);
        else activeCharacterIds.remove(uuid);
    }

    /**
     * Devuelve todos los perfiles de personajes para un jugador (slots 1..27).
     */
    public List<CharacterProfile> getProfiles(UUID playerUuid) {
        return plugin.getDataManager().getCharacterProfiles(playerUuid);
    }

    public CharacterProfile getProfile(String characterId) {
        return plugin.getDataManager().getCharacterProfile(characterId);
    }

    /**
     * Crea un personaje nuevo en el slot especificado.
     */
    public CharacterProfile createCharacter(UUID playerUuid, int slot, String name,
                                            RaceType race, ClassType playerClass,
                                            int[] standardArrayStats,
                                            Set<SkillDefinition> proficiencies,
                                            String alignment, String background) {
        return createCharacter(playerUuid, slot, name, race, playerClass, null, standardArrayStats, proficiencies, alignment, background);
    }

    public CharacterProfile createCharacter(UUID playerUuid, int slot, String name,
                                            RaceType race, ClassType playerClass,
                                            String subclass,
                                            int[] standardArrayStats,
                                            Set<SkillDefinition> proficiencies,
                                            String alignment, String background) {
        if (slot < 1 || slot > getMaxSlots()) {
            throw new IllegalArgumentException("Slot fuera de rango (1-" + getMaxSlots() + ").");
        }

        String charId = playerUuid.toString() + "_" + slot;
        CharacterProfile profile = new CharacterProfile(charId, playerUuid, slot, name);
        profile.setRace(race);
        profile.setPlayerClass(playerClass);
        profile.setSubclass(subclass != null ? subclass : "");
        profile.setLevel(1);
        profile.setXp(0);
        profile.setGold(plugin.getEconomyManager().getStartingGold());
        profile.setFatigue(0);

        if (standardArrayStats != null && standardArrayStats.length >= 6) {
            profile.setBaseStr(standardArrayStats[0]);
            profile.setBaseDex(standardArrayStats[1]);
            profile.setBaseCon(standardArrayStats[2]);
            profile.setBaseInt(standardArrayStats[3]);
            profile.setBaseWis(standardArrayStats[4]);
            profile.setBaseCha(standardArrayStats[5]);
        } else if (playerClass != null) {
            int[] def = LevelUpManager.getStandardArrayForClass(playerClass);
            profile.setBaseStr(def[0]);
            profile.setBaseDex(def[1]);
            profile.setBaseCon(def[2]);
            profile.setBaseInt(def[3]);
            profile.setBaseWis(def[4]);
            profile.setBaseCha(def[5]);
        }

        int conMod = AttributeCalculator.modifier(profile.getBaseCon());
        int hp = LevelUpManager.calculateMaxHp(playerClass, 1, conMod);
        profile.setHpCurrent(hp);
        profile.setHpMax(hp);

        if (proficiencies != null) {
            profile.getProficiencies().addAll(proficiencies);
        }
        profile.setAlignment(alignment != null ? alignment : "Neutral");
        profile.setBackground(background != null ? background : "Aventurero");
        profile.setCampaignId("principal");
        profile.setCreatedAt(System.currentTimeMillis());
        profile.setLastPlayed(System.currentTimeMillis());

        plugin.getDataManager().saveCharacterProfile(profile);
        return profile;
    }

    /**
     * Selección y cambio atómico de personaje para un jugador conectado.
     */
    public boolean selectCharacter(Player player, String targetCharId) {
        UUID uuid = player.getUniqueId();
        if (switchingLock.contains(uuid)) {
            player.sendMessage(ChatColor.RED + "Ya se está procesando un cambio de personaje.");
            return false;
        }

        switchingLock.add(uuid);
        try {
            // 1. Guardar estado del personaje saliente (si lo había)
            String currentId = activeCharacterIds.get(uuid);
            if (currentId != null && !currentId.equalsIgnoreCase(targetCharId)) {
                savePlayerToCharacter(player, currentId);
            }

            // 2. Cargar perfil del nuevo personaje
            CharacterProfile targetProfile = plugin.getDataManager().getCharacterProfile(targetCharId);
            if (targetProfile == null || targetProfile.isDeleted()) {
                player.sendMessage(ChatColor.RED + "El personaje seleccionado no existe o está eliminado.");
                return false;
            }

            // 3. Cargar PlayerData en PlayerManager
            PlayerData newData = plugin.getDataManager().loadCharacterData(targetCharId, uuid);
            plugin.getPlayerManager().loadPlayerData(uuid, newData);
            activeCharacterIds.put(uuid, targetCharId);

            // 4. Marcar activo en BD
            plugin.getDataManager().setActiveCharacterProfile(uuid, targetCharId);

            // 5. Restaurar inventario y ubicación
            restoreCharacterToPlayer(player, targetCharId, targetProfile, newData);

            player.sendMessage(ChatColor.GREEN + "✦ Has seleccionado a " + ChatColor.GOLD + targetProfile.getName()
                    + ChatColor.GRAY + " (" + targetProfile.getRace().displayName + " " + targetProfile.getPlayerClass().displayName
                    + " Niv " + targetProfile.getLevel() + ").");

            return true;
        } finally {
            switchingLock.remove(uuid);
        }
    }

    /**
     * Guarda el estado actual de un jugador a la tabla de inventario y ec_characters.
     */
    public void savePlayerToCharacter(Player player, String charId) {
        if (charId == null || player == null) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            plugin.getDataManager().save(data);
        }

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] extra = player.getInventory().getExtraContents();
        ItemStack[] ender = player.getEnderChest().getContents();

        String invB64 = InventorySerializer.toBase64(contents);
        String armB64 = InventorySerializer.toBase64(armor);
        String offB64 = InventorySerializer.toBase64(extra);
        String endB64 = InventorySerializer.toBase64(ender);

        Location loc = player.getLocation();
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "world";
        String gm = player.getGameMode().name();

        plugin.getDataManager().saveCharacterInventory(
                charId, invB64, armB64, offB64, endB64,
                worldName, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), gm
        );
    }

    private void restoreCharacterToPlayer(Player player, String charId, CharacterProfile profile, PlayerData data) {
        CharacterInventoryData invData = plugin.getDataManager().loadCharacterInventory(charId);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setExtraContents(new ItemStack[1]);
        player.getEnderChest().clear();

        if (invData != null && invData.inventoryData != null && !invData.inventoryData.isEmpty()) {
            player.getInventory().setContents(InventorySerializer.fromBase64(invData.inventoryData));
            player.getInventory().setArmorContents(InventorySerializer.fromBase64(invData.armorData));
            player.getInventory().setExtraContents(InventorySerializer.fromBase64(invData.offhandData));
            player.getEnderChest().setContents(InventorySerializer.fromBase64(invData.enderChestData));

            World targetWorld = Bukkit.getWorld(invData.world);
            if (targetWorld == null) targetWorld = Bukkit.getWorlds().get(0);
            Location targetLoc = new Location(targetWorld, invData.x, invData.y, invData.z, invData.yaw, invData.pitch);
            player.teleport(targetLoc);

            try {
                player.setGameMode(GameMode.valueOf(invData.gameMode));
            } catch (Exception ignored) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        } else {
            // Personaje nuevo: dar recetas y equipo inicial básico
            plugin.getStarterRecipes().giveStarterBook(player);
            World mainWorld = Bukkit.getWorld("world");
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
            }
            player.setGameMode(GameMode.SURVIVAL);
        }

        // Aplicar escala y vida de raza/clase
        if (data.getRace() != null) {
            try {
                var scaleAttr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_SCALE);
                if (scaleAttr != null) scaleAttr.setBaseValue(data.getRace().scale);
            } catch (Throwable ignored) {}
        }
        com.tuusuario.mmo.eldencraft.managers.ExperienceManager.applyMaxHealth(player, data);
        player.setHealth(Math.min(player.getMaxHealth(), data.getDndHpCurrent() > 0 ? data.getDndHpCurrent() : player.getMaxHealth()));
    }

    public boolean renameCharacter(Player player, String characterId, String newName) {
        if (newName == null || newName.length() < 3 || newName.length() > 16 || !newName.matches("^[a-zA-Z0-9_áéíóúÁÉÍÓÚñÑ ]+$")) {
            player.sendMessage(ChatColor.RED + "Nombre inválido (debe tener entre 3 y 16 caracteres alfanuméricos).");
            return false;
        }
        boolean ok = plugin.getDataManager().renameCharacter(characterId, newName);
        if (ok) {
            String activeId = getActiveCharacterId(player.getUniqueId());
            if (characterId.equalsIgnoreCase(activeId)) {
                PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                if (data != null) {
                    data.setCharacterName(newName);
                }
            }
            player.sendMessage(ChatColor.GREEN + "✦ Tu personaje ahora se llama: " + ChatColor.GOLD + newName);
            return true;
        }
        player.sendMessage(ChatColor.RED + "No se pudo renombrar el personaje.");
        return false;
    }

    /**
     * Borrado suave con confirmación y recuperación a 7 días.
     */
    public boolean softDelete(String characterId) {
        return plugin.getDataManager().softDeleteCharacter(characterId);
    }

    public boolean softDeleteCharacter(Player player, String characterId) {
        return softDelete(characterId);
    }

    public boolean recover(String characterId) {
        CharacterProfile p = plugin.getDataManager().getCharacterProfile(characterId);
        if (p == null || !p.canBeRecovered(getDeleteGraceDays())) return false;
        return plugin.getDataManager().recoverCharacter(characterId);
    }

    public boolean recoverCharacter(Player player, String characterId) {
        return recover(characterId);
    }

    /**
     * Duplica un personaje existente como nivel 1 en otro slot vacío.
     */
    public CharacterProfile duplicateAsLevel1(UUID playerUuid, String sourceCharId, int targetSlot) {
        CharacterProfile src = plugin.getDataManager().getCharacterProfile(sourceCharId);
        if (src == null) return null;

        int[] stats = new int[]{src.getBaseStr(), src.getBaseDex(), src.getBaseCon(), src.getBaseInt(), src.getBaseWis(), src.getBaseCha()};
        return createCharacter(playerUuid, targetSlot, src.getName() + " (Copia)", src.getRace(), src.getPlayerClass(), stats, src.getProficiencies(), src.getAlignment(), src.getBackground());
    }

    public CharacterProfile duplicateCharacterLevel1(Player player, String sourceCharId) {
        List<CharacterProfile> profiles = getProfiles(player.getUniqueId());
        int freeSlot = -1;
        for (int s = 1; s <= getMaxSlots(); s++) {
            final int slotCheck = s;
            if (profiles.stream().noneMatch(p -> p.getSlot() == slotCheck)) {
                freeSlot = s;
                break;
            }
        }
        if (freeSlot == -1) {
            player.sendMessage(ChatColor.RED + "No tienes slots libres para duplicar (máximo " + getMaxSlots() + ").");
            return null;
        }
        return duplicateAsLevel1(player.getUniqueId(), sourceCharId, freeSlot);
    }

    /**
     * Exporta un personaje a texto JSON estructurado.
     */
    public String exportToJson(String charId) {
        CharacterProfile p = plugin.getDataManager().getCharacterProfile(charId);
        if (p == null) return null;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":\"").append(p.getName()).append("\",");
        sb.append("\"race\":\"").append(p.getRace() != null ? p.getRace().name() : "").append("\",");
        sb.append("\"class\":\"").append(p.getPlayerClass() != null ? p.getPlayerClass().name() : "").append("\",");
        sb.append("\"level\":").append(p.getLevel()).append(",");
        sb.append("\"xp\":").append(p.getXp()).append(",");
        sb.append("\"gold\":").append(p.getGold()).append(",");
        sb.append("\"str\":").append(p.getBaseStr()).append(",");
        sb.append("\"dex\":").append(p.getBaseDex()).append(",");
        sb.append("\"con\":").append(p.getBaseCon()).append(",");
        sb.append("\"int\":").append(p.getBaseInt()).append(",");
        sb.append("\"wis\":").append(p.getBaseWis()).append(",");
        sb.append("\"cha\":").append(p.getBaseCha()).append(",");
        sb.append("\"alignment\":\"").append(p.getAlignment()).append("\",");
        sb.append("\"background\":\"").append(p.getBackground()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public String exportCharacterToJson(String charId) {
        return exportToJson(charId);
    }

    /**
     * Importa un personaje desde JSON a un slot específico.
     */
    public CharacterProfile importFromJson(UUID playerUuid, int targetSlot, String json) {
        try {
            String name = extractJsonValue(json, "name");
            String raceStr = extractJsonValue(json, "race");
            String classStr = extractJsonValue(json, "class");
            String align = extractJsonValue(json, "alignment");
            String bg = extractJsonValue(json, "background");

            int str = extractJsonInt(json, "str", 10);
            int dex = extractJsonInt(json, "dex", 10);
            int con = extractJsonInt(json, "con", 10);
            int intel = extractJsonInt(json, "int", 10);
            int wis = extractJsonInt(json, "wis", 10);
            int cha = extractJsonInt(json, "cha", 10);

            RaceType r = RaceType.valueOf(raceStr);
            ClassType c = ClassType.valueOf(classStr);

            return createCharacter(playerUuid, targetSlot, name != null ? name : "Importado", r, c,
                    new int[]{str, dex, con, intel, wis, cha}, null, align, bg);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int idx = json.indexOf(marker);
        if (idx == -1) return null;
        int start = idx + marker.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private int extractJsonInt(String json, String key, int def) {
        String marker = "\"" + key + "\":";
        int idx = json.indexOf(marker);
        if (idx == -1) return def;
        int start = idx + marker.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return def;
        }
    }
}
