package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpellListener implements Listener {

    private final EldenCraft plugin;

    public SpellListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCast(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // solo si hace click derecho con el palo
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            && player.getInventory().getItemInMainHand().getType() == Material.STICK) {

            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

            // solo los magos pueden tirar bolas de fuego
            if (data != null && data.getPlayerClass() == ClassType.MAGE) {

                double costo = 20.0; // El hechizo cuesta 20 de maná

                if (data.getCurrentResource() >= costo) {
                    // 1. Cobrar el maná
                    data.setCurrentResource(data.getCurrentResource() - costo);

                    // 2. lanzamos la bola de fuego
                    Fireball fireball = player.launchProjectile(Fireball.class);
                    fireball.setYield(2.0f);          // Daño de explosion a entidades
                    fireball.setIsIncendiary(false);  // No prende fuego
                    // el BlockSafetyListener se encarga de q no rompa bloques

                    // 3. sonidito y mensaje
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                    player.sendMessage("§6§l¡Bola de Fuego!");
                } else {
                    player.sendMessage("§cNo tienes suficiente maná (Necesitas " + (int)costo + ")");
                }
            }
        }
    }
}
