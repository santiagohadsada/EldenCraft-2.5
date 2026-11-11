package com.tuusuario.mmo.eldencraft.skills.gunslinger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DisparoPenetrante implements Skill {
    @Override public String getName() { return "Disparo Penetrante"; }
    @Override public double getCost() { return 50.0; }
    @Override public int getLevelRequired() { return 14; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(4.0));
        arrow.setDamage(arrow.getDamage() + (data.getDexterity() * 0.25));
        arrow.setPierceLevel(127); // Máximo nivel de penetración
        
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation().add(0, 1, 0), 20, 0.2, 0.2, 0.2, 0.1);
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1f, 0.5f);
        player.sendMessage("§e✦ Disparo Penetrante: §7¡El proyectil atravesará a los enemigos!");
    }
}
