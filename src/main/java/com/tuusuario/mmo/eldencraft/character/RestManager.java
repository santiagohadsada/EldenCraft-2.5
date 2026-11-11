package com.tuusuario.mmo.eldencraft.character;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.spells.SpellSlotTable;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona los descansos cortos y largos D&D 5e.
 * Los jugadores solicitan descansos; el DM los concede.
 */
public class RestManager {

    private final EldenCraft plugin;
    // Solicitudes pendientes: uuid → tipo ("corto" | "largo")
    private final Map<UUID, String> pendingRequests = new HashMap<>();

    public RestManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // ── Solicitudes (jugadores) ───────────────────────────────────────────────

    public void requestShortRest(Player player) {
        pendingRequests.put(player.getUniqueId(), "corto");
        MessageUtil.send(player, "rest.short-request");
        // Notificar a los DM de su campaña
        broadcastToDMs(player, "rest.dm-request", Map.of(
                "jugador", player.getName(),
                "tipo", "corto"
        ));
    }

    public void requestLongRest(Player player) {
        pendingRequests.put(player.getUniqueId(), "largo");
        MessageUtil.send(player, "rest.long-request");
        broadcastToDMs(player, "rest.dm-request", Map.of(
                "jugador", player.getName(),
                "tipo", "largo"
        ));
    }

    // ── Conceder descansos (DM) ───────────────────────────────────────────────

    /**
     * Concede un descanso corto a un jugador.
     * Recupera: dados de golpe (stub en F2 — recarga recurso a mitad).
     */
    public void grantShortRest(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // F2: Recuperar 50% del recurso (stub — dados de golpe completos en F3)
        double half = data.getMaxResource() * 0.5;
        data.setCurrentResource(Math.min(data.getMaxResource(), data.getCurrentResource() + half));

        // Warlock recupera espacios de conjuro con descanso corto
        if (data.getPlayerClass() == com.tuusuario.mmo.eldencraft.models.ClassType.WARLOCK) {
            recoverWarlockSlots(data);
        }

        pendingRequests.remove(player.getUniqueId());
        MessageUtil.send(player, "rest.short-done");
    }

    /**
     * Concede un descanso largo a un jugador.
     * Recupera: PG máximo, recurso máximo, todos los espacios de conjuro, limpia fatiga.
     */
    public void grantLongRest(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // PG D&D máximo
        if (plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) {
            data.setDndHpCurrent(data.getDndHpMax());
            // Sincronizar vida Minecraft
            syncMinecraftHealth(player, data);
        }

        // Recurso máximo
        data.setCurrentResource(data.getMaxResource());

        // Espacios de conjuro restaurados
        data.restoreAllSpellSlots();
        // Recalcular espacios máximos según clase/nivel
        if (data.getPlayerClass() != null) {
            int[] maxSlots = SpellSlotTable.getSlots(data.getPlayerClass(), data.getLevel());
            System.arraycopy(maxSlots, 0, data.getSpellSlotsMax(), 0, 9);
        }

        // Fatiga limpia
        data.setFatigue(0);

        // Concentración rota si descansó
        if (data.getConcentrationSpell() != null) {
            plugin.getConcentrationManager().breakConcentration(player.getUniqueId());
            data.setConcentrationSpell(null);
        }

        pendingRequests.remove(player.getUniqueId());
        MessageUtil.send(player, "rest.long-done");
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void recoverWarlockSlots(PlayerData data) {
        // El Brujo recupera todos sus espacios de pacto con descanso corto
        // # VERIFICAR: cantidad de espacios por nivel para Warlock
        int[] warlockSlots = SpellSlotTable.getWarlockSlots(data.getLevel());
        System.arraycopy(warlockSlots, 0, data.getSpellSlotsMax(), 0, 9);
        data.restoreAllSpellSlots();
    }

    private void syncMinecraftHealth(Player player, PlayerData data) {
        try {
            var attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                double maxHp = data.getMaxHealth();
                attr.setBaseValue(maxHp);
                player.setHealth(maxHp);
            }
        } catch (Throwable ignored) {}
    }

    private void broadcastToDMs(Player player, String messageKey, Map<String, String> placeholders) {
        String campId = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, player);
        for (Player dm : com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getOnlineDms(plugin, campId)) {
            MessageUtil.send(dm, messageKey, placeholders);
        }
    }

    public Map<UUID, String> getPendingRequests() { return pendingRequests; }
}
