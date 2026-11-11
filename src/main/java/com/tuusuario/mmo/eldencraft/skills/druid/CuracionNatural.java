package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CuracionNatural implements Skill {
    @Override public String getName() { return "Curación Natural"; }
    @Override public double getCost() { return 25.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        int power = Math.min(2, data.getWisdom() / 8);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, power));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 1));
        player.removePotionEffect(PotionEffectType.POISON);

        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0);
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1f, 1.2f);
        player.sendMessage("§2✦ Curación Natural: §7Regeneración de la naturaleza activada (10s).");
    }
}
