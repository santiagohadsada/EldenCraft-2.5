package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BalaExplosiva implements Skill {
    @Override public String getName() { return "Bala Explosiva"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 8; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(3.0));
        arrow.setDamage(arrow.getDamage() + (data.getDexterity() * 0.2));
        arrow.setMetadata("explosive_bullet", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        
        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2f);
        player.sendMessage("§e✦ Bala Explosiva: §7¡El proyectil explotará al impactar!");
    }
}
