package com.tuusuario.mmo.eldencraft.campaign;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor del sistema de Campañas D&D 5e (SPEC 15.4 / C3).
 */
public class CampaignManager {

    private final EldenCraft plugin;
    private final SecureRandom random = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // Caché de miembros en memoria por campaña: campaignId -> (uuid -> CampaignMember)
    private final Map<String, Map<UUID, CampaignMember>> memberCache = new ConcurrentHashMap<>();

    public CampaignManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public List<Campaign> getAllCampaigns() {
        return plugin.getDataManager().getAllCampaigns();
    }

    public Campaign getCampaign(String id) {
        return plugin.getDataManager().getCampaign(id);
    }

    public Campaign getCampaignByJoinCode(String code) {
        return plugin.getDataManager().getCampaignByJoinCode(code);
    }

    public List<Campaign> getCampaignsForPlayer(UUID playerUuid) {
        List<Campaign> result = new ArrayList<>();
        List<Campaign> all = getAllCampaigns();
        for (Campaign c : all) {
            CampaignMember m = getMember(c.getId(), playerUuid);
            if (m != null) {
                result.add(c);
            }
        }
        return result;
    }

    public Campaign createCampaign(UUID dmUuid, String id, String name, String worldName) {
        if (id == null || !id.matches("^[a-zA-Z0-9_-]{2,32}$")) {
            throw new IllegalArgumentException("ID de campaña inválido (2-32 caracteres alfanuméricos).");
        }
        if (getCampaign(id) != null) {
            throw new IllegalArgumentException("Ya existe una campaña con el ID '" + id + "'.");
        }

        String wName = worldName != null && !worldName.isBlank() ? worldName : ("ec_camp_" + id);
        Campaign c = new Campaign(id, name != null ? name : id, dmUuid, wName);
        c.setJoinCode(generateCode());
        c.setStatus(CampaignStatus.ABIERTA);

        plugin.getDataManager().saveCampaign(c);

        // Añadir creador como DM_PRINCIPAL
        CampaignMember dmMember = new CampaignMember(id, dmUuid, CampaignRole.DM_PRINCIPAL, null, System.currentTimeMillis());
        plugin.getDataManager().saveCampaignMember(dmMember);
        cacheMember(dmMember);

        return c;
    }

    public boolean isDm(String campaignId, UUID playerUuid) {
        if (campaignId == null || playerUuid == null) return false;
        Campaign c = getCampaign(campaignId);
        if (c != null && c.getDmUuid().equals(playerUuid)) return true;
        CampaignMember m = getMember(campaignId, playerUuid);
        return m != null && m.getRole().isDm;
    }

    public boolean isOwner(String campaignId, UUID playerUuid) {
        if (campaignId == null || playerUuid == null) return false;
        Campaign c = getCampaign(campaignId);
        return c != null && c.getDmUuid().equals(playerUuid);
    }

    public CampaignRole getRole(String campaignId, UUID playerUuid) {
        CampaignMember m = getMember(campaignId, playerUuid);
        if (m != null) return m.getRole();
        Campaign c = getCampaign(campaignId);
        if (c != null && c.getDmUuid().equals(playerUuid)) return CampaignRole.DM_PRINCIPAL;
        return null;
    }

    public CampaignMember getMember(String campaignId, UUID playerUuid) {
        Map<UUID, CampaignMember> map = memberCache.get(campaignId);
        if (map != null && map.containsKey(playerUuid)) {
            return map.get(playerUuid);
        }
        CampaignMember m = plugin.getDataManager().getCampaignMember(campaignId, playerUuid);
        if (m != null) cacheMember(m);
        return m;
    }

    public List<CampaignMember> getMembers(String campaignId) {
        return plugin.getDataManager().getCampaignMembers(campaignId);
    }

    private void cacheMember(CampaignMember m) {
        memberCache.computeIfAbsent(m.getCampaignId(), k -> new ConcurrentHashMap<>()).put(m.getPlayerUuid(), m);
    }

    public boolean joinCampaign(Player player, String joinCodeOrId) {
        Campaign campaign = getCampaignByJoinCode(joinCodeOrId);
        if (campaign == null) {
            campaign = getCampaign(joinCodeOrId);
        }
        if (campaign == null) {
            player.sendMessage(ChatColor.RED + "No se encontró ninguna campaña con el código o ID especificado.");
            return false;
        }

        // 1. Comprobación de veto
        if (plugin.getDataManager().isBannedFromCampaign(campaign.getId(), player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Has sido vetado de esta campaña por el DM.");
            return false;
        }

        boolean isAdmin = player.isOp() || player.hasPermission("eldencraft.admin");
        boolean hasInvite = plugin.getDataManager().hasPendingInvite(campaign.getId(), player.getUniqueId());

        // 2. Comprobación de estado
        if (campaign.getStatus() == CampaignStatus.SOLO_INVITADOS && !hasInvite && !isAdmin && !isDm(campaign.getId(), player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Esta campaña es SOLO_INVITADOS y no tienes una invitación pendiente.");
            return false;
        }

        if ((campaign.getStatus() == CampaignStatus.CERRADA || campaign.getStatus() == CampaignStatus.ARCHIVADA || campaign.getStatus() == CampaignStatus.BORRADOR)
                && !isAdmin && !isDm(campaign.getId(), player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Esta campaña está en estado " + campaign.getStatus().displayName + " y no admite nuevos jugadores.");
            return false;
        }

        // 3. Límite de miembros
        List<CampaignMember> currentMembers = getMembers(campaign.getId());
        if (currentMembers.size() >= campaign.getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "Esta campaña ha alcanzado su límite de jugadores (" + campaign.getMaxPlayers() + ").");
            return false;
        }

        // 4. Validación de requisitos de ficha
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data != null) {
            if (data.getLevel() < campaign.getMinLevel() || data.getLevel() > campaign.getMaxLevel()) {
                player.sendMessage(ChatColor.RED + "Tu nivel (" + data.getLevel() + ") no cumple los requisitos de la campaña ("
                        + campaign.getMinLevel() + "-" + campaign.getMaxLevel() + ").");
                return false;
            }

            String allowedRaces = plugin.getDataManager().getCampaignSetting(campaign.getId(), "allowed_races", null);
            if (allowedRaces != null && !allowedRaces.isBlank() && data.getRace() != null) {
                if (!allowedRaces.toLowerCase().contains(data.getRace().name().toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "La raza " + data.getRace().displayName + " no está permitida en esta campaña.");
                    return false;
                }
            }

            String allowedClasses = plugin.getDataManager().getCampaignSetting(campaign.getId(), "allowed_classes", null);
            if (allowedClasses != null && !allowedClasses.isBlank() && data.getPlayerClass() != null) {
                if (!allowedClasses.toLowerCase().contains(data.getPlayerClass().name().toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "La clase " + data.getPlayerClass().displayName + " no está permitida en esta campaña.");
                    return false;
                }
            }

            boolean allowCustom = Boolean.parseBoolean(plugin.getDataManager().getCampaignSetting(campaign.getId(), "allow_custom", "true"));
            if (!allowCustom && data.getPlayerClass() == ClassType.GUNSLINGER) {
                player.sendMessage(ChatColor.RED + "El contenido personalizado (Pistolero) no está permitido en esta campaña.");
                return false;
            }

            data.setCampaignId(campaign.getId());
            plugin.getDataManager().save(data);
        }

        // Actualizar invitación
        plugin.getDataManager().updateInviteStatus(campaign.getId(), player.getUniqueId(), "ACEPTADA");

        String charId = plugin.getCharacterManager().getActiveCharacterId(player.getUniqueId());
        CampaignMember member = new CampaignMember(campaign.getId(), player.getUniqueId(), CampaignRole.JUGADOR, charId, System.currentTimeMillis());
        plugin.getDataManager().saveCampaignMember(member);
        cacheMember(member);

        player.sendMessage(ChatColor.GREEN + "✦ ¡Te has unido a la campaña " + ChatColor.GOLD + campaign.getName() + ChatColor.GREEN + "!");
        teleportToCampaignSpawn(player, campaign);
        return true;
    }

    public boolean invitePlayer(Player dm, String targetName, String campaignId) {
        if (!isDm(campaignId, dm.getUniqueId()) && !dm.isOp()) {
            dm.sendMessage(ChatColor.RED + "No tienes permisos de DM en esta campaña.");
            return false;
        }

        Campaign c = getCampaign(campaignId);
        if (c == null) {
            dm.sendMessage(ChatColor.RED + "Campaña no encontrada.");
            return false;
        }

        Player onlineTarget = Bukkit.getPlayer(targetName);
        UUID targetUuid = onlineTarget != null ? onlineTarget.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // Registrar invitación en base de datos
        plugin.getDataManager().saveCampaignInvite(campaignId, targetUuid, c.getJoinCode(), "PENDIENTE");

        if (onlineTarget != null) {
            dm.sendMessage(ChatColor.GREEN + "Invitación interactiva enviada a " + onlineTarget.getName() + " para la campaña " + c.getName() + ".");

            onlineTarget.sendMessage(ChatColor.GOLD + "================================================");
            onlineTarget.sendMessage(ChatColor.YELLOW + "✦ ¡Has sido invitado a la campaña " + ChatColor.GOLD + c.getName() + ChatColor.YELLOW + " por " + dm.getName() + "!");

            TextComponent msg = new TextComponent(ChatColor.WHITE + "Haz click para responder: ");
            TextComponent acceptBtn = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[Aceptar]");
            acceptBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/campana aceptar " + c.getId()));
            acceptBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click para unirte a " + c.getName()).create()));

            TextComponent rejectBtn = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + " [Rechazar]");
            rejectBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/campana rechazar " + c.getId()));
            rejectBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click para rechazar la invitación").create()));

            msg.addExtra(acceptBtn);
            msg.addExtra(rejectBtn);
            onlineTarget.spigot().sendMessage(msg);

            onlineTarget.sendMessage(ChatColor.GRAY + "O usa /campana unirse " + c.getJoinCode());
            onlineTarget.sendMessage(ChatColor.GOLD + "================================================");
        } else {
            dm.sendMessage(ChatColor.YELLOW + "El jugador " + targetName + " está desconectado. La invitación quedó pendiente en su selector para cuando entre.");
        }
        return true;
    }

    public boolean acceptInvite(Player player, String campaignId) {
        if (!plugin.getDataManager().hasPendingInvite(campaignId, player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No tienes una invitación pendiente para la campaña '" + campaignId + "'.");
            return false;
        }
        return joinCampaign(player, campaignId);
    }

    public boolean rejectInvite(Player player, String campaignId) {
        if (!plugin.getDataManager().hasPendingInvite(campaignId, player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No tienes una invitación pendiente para esa campaña.");
            return false;
        }
        plugin.getDataManager().updateInviteStatus(campaignId, player.getUniqueId(), "RECHAZADA");
        player.sendMessage(ChatColor.YELLOW + "Has rechazado la invitación a la campaña '" + campaignId + "'.");
        return true;
    }

    public boolean banPlayer(Player dm, UUID targetUuid, String campaignId) {
        if (!isDm(campaignId, dm.getUniqueId()) && !dm.isOp()) {
            dm.sendMessage(ChatColor.RED + "No tienes permisos de DM.");
            return false;
        }

        plugin.getDataManager().banPlayerFromCampaign(campaignId, targetUuid);
        Map<UUID, CampaignMember> map = memberCache.get(campaignId);
        if (map != null) map.remove(targetUuid);

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null) {
            onlineTarget.sendMessage(ChatColor.RED + "Has sido vetado de la campaña '" + campaignId + "' por el DM.");
            plugin.getLobbyManager().sendToLobby(onlineTarget);
        }

        dm.sendMessage(ChatColor.YELLOW + "Jugador vetado de la campaña.");
        return true;
    }

    public boolean transferOwnership(Player dm, UUID newDmUuid, String campaignId) {
        if (!isOwner(campaignId, dm.getUniqueId()) && !dm.isOp()) {
            dm.sendMessage(ChatColor.RED + "Solo el DM Principal puede transferir la campaña.");
            return false;
        }

        Campaign c = getCampaign(campaignId);
        if (c == null) return false;

        c.setDmUuid(newDmUuid);
        plugin.getDataManager().saveCampaign(c);

        // Actualizar roles
        setRole(dm, newDmUuid, campaignId, CampaignRole.DM_PRINCIPAL);
        setRole(dm, dm.getUniqueId(), campaignId, CampaignRole.CO_DM);

        dm.sendMessage(ChatColor.GREEN + "Campaña transferida con éxito al nuevo DM Principal.");
        return true;
    }

    public boolean kickPlayer(Player dm, UUID targetUuid, String campaignId) {
        if (!isDm(campaignId, dm.getUniqueId()) && !dm.isOp()) {
            dm.sendMessage(ChatColor.RED + "No tienes permisos de DM en esta campaña.");
            return false;
        }

        if (dm.getUniqueId().equals(targetUuid)) {
            dm.sendMessage(ChatColor.RED + "No puedes expulsarte a ti mismo de la campaña.");
            return false;
        }

        CampaignMember targetMember = getMember(campaignId, targetUuid);
        if (targetMember == null) {
            dm.sendMessage(ChatColor.RED + "Ese jugador no es miembro de esta campaña.");
            return false;
        }

        plugin.getDataManager().removeCampaignMember(campaignId, targetUuid);
        Map<UUID, CampaignMember> map = memberCache.get(campaignId);
        if (map != null) map.remove(targetUuid);

        Player onlineTarget = Bukkit.getPlayer(targetUuid);
        if (onlineTarget != null) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(targetUuid);
            if (data != null && campaignId.equalsIgnoreCase(data.getCampaignId())) {
                data.setCampaignId("principal");
                plugin.getDataManager().save(data);
            }
            onlineTarget.sendMessage(ChatColor.RED + "Has sido expulsado de la campaña '" + campaignId + "'.");
            plugin.getLobbyManager().sendToLobby(onlineTarget);
        }

        dm.sendMessage(ChatColor.YELLOW + "Jugador expulsado de la campaña con éxito.");
        return true;
    }

    public boolean setRole(Player dm, UUID targetUuid, String campaignId, CampaignRole newRole) {
        if (!isOwner(campaignId, dm.getUniqueId()) && !dm.isOp()) {
            dm.sendMessage(ChatColor.RED + "Solo el DM Principal puede cambiar roles.");
            return false;
        }

        CampaignMember m = getMember(campaignId, targetUuid);
        if (m == null) {
            m = new CampaignMember(campaignId, targetUuid, newRole, null, System.currentTimeMillis());
        } else {
            m.setRole(newRole);
        }
        plugin.getDataManager().saveCampaignMember(m);
        cacheMember(m);

        dm.sendMessage(ChatColor.GREEN + "Rol actualizado a " + newRole.displayName + ".");
        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage(ChatColor.GREEN + "Tu rol en la campaña " + campaignId + " ahora es: " + ChatColor.GOLD + newRole.displayName);
        }
        return true;
    }

    // ── Gestión Dinámica de Mundos (C3: SPEC 15.4) ───────────────────────────

    public World loadCampaignWorld(Campaign c) {
        if (c == null) return null;
        World w = Bukkit.getWorld(c.getWorldName());
        if (w == null) {
            // Verificar límite de mundos cargados (campaigns.max-loaded-worlds)
            int maxWorlds = plugin.getConfig().getInt("campaigns.max-loaded-worlds", 3);
            checkAndUnloadEmptyWorlds(maxWorlds);

            try {
                plugin.getLogger().info("[CampaignManager] Cargando mundo bajo demanda para " + c.getId() + ": " + c.getWorldName());
                WorldCreator creator = new WorldCreator(c.getWorldName());
                w = creator.createWorld();
                if (w != null) {
                    applyCampaignSettingsToWorld(c, w);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[CampaignManager] Error cargando mundo " + c.getWorldName() + ": " + e.getMessage());
            }
        }
        return w;
    }

    private void checkAndUnloadEmptyWorlds(int maxAllowed) {
        List<World> campaignWorlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith("ec_camp_")) {
                campaignWorlds.add(world);
            }
        }

        if (campaignWorlds.size() >= maxAllowed) {
            for (World cw : campaignWorlds) {
                if (cw.getPlayers().isEmpty()) {
                    plugin.getLogger().info("[CampaignManager] Descargando mundo de campaña inactivo para liberar memoria: " + cw.getName());
                    Bukkit.unloadWorld(cw, true);
                    break;
                }
            }
        }
    }

    public boolean unloadCampaignWorld(Campaign c, boolean save) {
        if (c == null) return false;
        World w = Bukkit.getWorld(c.getWorldName());
        if (w == null) return true;

        Location fallback = plugin.getLobbyManager().getLobbySpawn();
        if (fallback == null && !Bukkit.getWorlds().isEmpty()) {
            fallback = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        for (Player p : w.getPlayers()) {
            if (fallback != null) p.teleport(fallback);
            p.sendMessage(ChatColor.YELLOW + "El mundo de la campaña ha sido descargado. Fuiste llevado a una zona segura.");
        }

        return Bukkit.unloadWorld(w, save);
    }

    public void teleportToCampaignSpawn(Player player, Campaign c) {
        World w = loadCampaignWorld(c);
        if (w == null) {
            player.sendMessage(ChatColor.RED + "No se pudo cargar el mundo de la campaña.");
            return;
        }
        Location loc = new Location(w, c.getSpawnX(), c.getSpawnY(), c.getSpawnZ());
        player.teleport(loc);
        player.sendMessage(ChatColor.GREEN + "Te has trasladado a la campaña: " + ChatColor.GOLD + c.getName());
        CampaignContext.applyVisibilityIsolation(plugin, player);
    }

    public void applyCampaignSettingsToWorld(Campaign c, World w) {
        if (c == null || w == null) return;

        // Reglas automáticas de campaña (SPEC 3.4 / 15.4)
        boolean naturalRegen = plugin.getConfig().getBoolean("world-rules.natural-regeneration", false);
        boolean mobSpawning = plugin.getConfig().getBoolean("world-rules.mob-spawning", false);
        boolean keepInv = plugin.getConfig().getBoolean("world-rules.keep-inventory", true);

        w.setGameRule(GameRule.NATURAL_REGENERATION, naturalRegen);
        w.setGameRule(GameRule.DO_MOB_SPAWNING, mobSpawning);
        w.setGameRule(GameRule.KEEP_INVENTORY, keepInv);

        boolean pvp = Boolean.parseBoolean(plugin.getDataManager().getCampaignSetting(c.getId(), "pvp", "false"));
        w.setPVP(pvp);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
