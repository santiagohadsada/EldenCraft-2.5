package com.tuusuario.mmo.eldencraft.armor.classes;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.PlayerManager;
import org.bukkit.entity.Player;

/**
 * Adaptador que conecta el sistema de armaduras con el PlayerManager existente
 * de EldenCraft. Devuelve el nombre de la clase del jugador (WARRIOR, MAGE,
 * RANGER, PALADIN, ROGUE, BARD, BARBARIAN) en mayusculas tal y como lo declara
 * el enum ClassType. Las restricciones de armadura en config.yml deben usar
 * exactamente estos nombres.
 */
public final class EldenCraftClassManagerAdapter implements ClassManager {

    private final EldenCraft plugin;

    public EldenCraftClassManagerAdapter(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getClassName(Player player) {
        if (player == null) return null;
        PlayerManager pm = plugin.getPlayerManager();
        if (pm == null) return null;
        PlayerData data = pm.getPlayerData(player.getUniqueId());
        if (data == null || data.getPlayerClass() == null) return null;
        return data.getPlayerClass().name();
    }
}
