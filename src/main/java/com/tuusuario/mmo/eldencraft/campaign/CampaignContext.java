package com.tuusuario.mmo.eldencraft.campaign;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gestiona el contexto y aislamiento de jugadores por campaña (SPEC 15.5 / F3B-3).
 */
public final class CampaignContext {

    private CampaignContext() {}

    public static String getCampaignId(EldenCraft plugin, Player player) {
        if (player == null) return "principal";
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        return (data != null && data.getCampaignId() != null) ? data.getCampaignId() : "principal";
    }

    public static List<Player> getOnlineMembers(EldenCraft plugin, String campaignId) {
        List<Player> list = new ArrayList<>();
        String targetCamp = campaignId != null ? campaignId : "principal";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (targetCamp.equalsIgnoreCase(getCampaignId(plugin, p))) {
                list.add(p);
            }
        }
        return list;
    }

    public static List<Player> getOnlineDms(EldenCraft plugin, String campaignId) {
        List<Player> list = new ArrayList<>();
        var campMgr = plugin.getCampaignManager();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isOp() || campMgr.isDm(campaignId, p.getUniqueId())) {
                list.add(p);
            }
        }
        return list;
    }

    public static void broadcastToCampaign(EldenCraft plugin, String campaignId, String message) {
        for (Player p : getOnlineMembers(plugin, campaignId)) {
            p.sendMessage(message);
        }
    }

    public static void broadcastToCampaignDMs(EldenCraft plugin, String campaignId, String message) {
        for (Player dm : getOnlineDms(plugin, campaignId)) {
            dm.sendMessage(ChatColor.GRAY + "[DM-Log][" + campaignId + "] " + message);
        }
    }

    private static final java.util.Map<String, org.bukkit.scoreboard.Scoreboard> campaignScoreboards = new java.util.concurrent.ConcurrentHashMap<>();

    public static org.bukkit.scoreboard.Scoreboard getCampaignScoreboard(EldenCraft plugin, String campaignId) {
        String id = campaignId != null ? campaignId : "principal";
        return campaignScoreboards.computeIfAbsent(id, k -> {
            var sm = Bukkit.getScoreboardManager();
            return sm != null ? sm.getNewScoreboard() : null;
        });
    }

    public static void applyScoreboardIsolation(EldenCraft plugin, Player player) {
        if (player == null || !player.isOnline()) return;
        String campId = getCampaignId(plugin, player);
        var sb = getCampaignScoreboard(plugin, campId);
        if (sb != null) {
            player.setScoreboard(sb);
        }
    }

    public static void applyVisibilityIsolation(EldenCraft plugin, Player player) {
        if (player == null || !player.isOnline()) return;
        applyScoreboardIsolation(plugin, player);

        if (!plugin.getConfig().getBoolean("campaigns.isolate-visibility", true)) return;
        String playerCamp = getCampaignId(plugin, player);
        boolean playerIsAdmin = player.isOp() || player.hasPermission("eldencraft.admin");

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            boolean otherIsAdmin = other.isOp() || other.hasPermission("eldencraft.admin");
            String otherCamp = getCampaignId(plugin, other);

            if (playerCamp.equalsIgnoreCase(otherCamp)) {
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            } else {
                if (playerIsAdmin) {
                    player.showPlayer(plugin, other);
                } else {
                    player.hidePlayer(plugin, other);
                }

                if (otherIsAdmin) {
                    other.showPlayer(plugin, player);
                } else {
                    other.hidePlayer(plugin, player);
                }
            }
        }
    }

    public static void updateAllVisibility(EldenCraft plugin) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            applyVisibilityIsolation(plugin, p);
        }
    }

    public static boolean isDmForPlayer(EldenCraft plugin, Player dm, Player target) {
        if (dm.isOp()) return true;
        String campId = getCampaignId(plugin, target);
        return plugin.getCampaignManager().isDm(campId, dm.getUniqueId());
    }
}
