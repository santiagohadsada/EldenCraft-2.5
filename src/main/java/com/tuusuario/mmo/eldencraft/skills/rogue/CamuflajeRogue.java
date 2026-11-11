package com.tuusuario.mmo.eldencraft.skills.rogue;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class CamuflajeRogue implements Skill {
    @Override
    public String getName() { return "Camuflaje"; }
    @Override
    public double getCost() { return 20.0; }
    @Override
    public int getLevelRequired() { return 3; }

    @Override
    public void execute(Player player, PlayerData data) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
        player.playSound(player.getLocation(), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1f, 1f);
        player.sendMessage("§8[!] §7Te ocultas en las sombras...");

        player.getWorld().spawnParticle(Particle.LARGE_SMOKE,
                player.getLocation().add(0, 1, 0), 35, 0.5, 1.0, 0.5, 0.02);
    }
}
