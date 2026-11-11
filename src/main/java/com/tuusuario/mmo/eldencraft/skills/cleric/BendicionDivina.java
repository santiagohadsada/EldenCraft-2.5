package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BendicionDivina implements Skill {
    @Override public String getName() { return "Bendición Divina"; }
    @Override public double getCost() { return 30.0; }
    @Override public int getLevelRequired() { return 9; }

    @Override
    public void execute(Player player, PlayerData data) {
        int blessed = 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));

        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(6, 6, 6)) {
            if (!(e instanceof Player ally) || !plugin.getPartyManager().canHeal(player, ally)) continue;
            ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 0));
            ally.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 0));
            ally.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    ally.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
            ally.sendMessage("§f✦ ¡" + player.getName() + " te otorga Bendición Divina!");
            blessed++;
        }

        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.08);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.3f);
        player.sendMessage("§f✦ Bendición Divina: §7" + blessed + " aliado(s) bendecidos (10s).");
    }
}
