package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.managers.BossManager;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

// cuando muere un jefe: da exp + oro al killer, dropea el loot y avisa a todo el server
public class BossListener implements Listener {

    private final EldenCraft plugin;

    public BossListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        LivingEntity le = e.getEntity();
        BossManager.BossDef def = plugin.getBossManager().getBossOf(le);
        if (def == null) return;

        plugin.getBossManager().removeBossBar(le.getUniqueId());

        // sacamos los drops normales y ponemos los del jefe
        e.getDrops().clear();
        e.setDroppedExp(0);
        for (ItemStack it : def.loot) e.getDrops().add(it.clone());

        Player killer = le.getKiller();

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l✦ §e§lJEFE DERROTADO §6§l✦");
        Bukkit.broadcastMessage(def.display + " §7ha caido!");
        if (killer != null) {
            Bukkit.broadcastMessage("§7Asesino: §f" + killer.getName());
        }
        Bukkit.broadcastMessage("");

        // recompensa al q lo mato
        if (killer != null) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(killer.getUniqueId());
            if (data != null) {
                data.setXp(data.getXp() + def.expReward);
                plugin.getEconomyManager().addGold(data, def.goldReward);
                killer.sendMessage("§6+ " + def.expReward + " EXP §7y §6+ " + def.goldReward + " oro §7por matar a §f" + def.id);
                killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                plugin.getExperienceManager().checkLevelUp(killer, data);
            }
        }
    }
}
