package com.tuusuario.mmo.eldencraft.listeners;

import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.projectiles.ProjectileSource;

// pa q las habilidades no rompan bloques ni prendan fuego
public class BlockSafetyListener implements Listener {

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity ent = event.getEntity();
        if (isPlayerLaunchedFireball(ent)) {
            // cancela q rompa bloques pero deja el dau00f1o
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        // si una bola de fuego de un jugador prende fuego lo cancelamos
        
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FIREBALL) {
            Entity igniting = event.getIgnitingEntity();
            if (igniting == null || isPlayerLaunchedFireball(igniting)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // evita q los proyectiles rompan antorchas, cultivos, etc
        
        if (isPlayerLaunchedFireball(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private boolean isPlayerLaunchedFireball(Entity ent) {
        if (!(ent instanceof Fireball || ent instanceof SmallFireball
            || ent instanceof LargeFireball || ent instanceof DragonFireball
            || ent instanceof WitherSkull)) {
            return false;
        }
        if (ent instanceof Projectile proj) {
            ProjectileSource src = proj.getShooter();
            return src instanceof Player;
        }
        return false;
    }
}
