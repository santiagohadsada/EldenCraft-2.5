package com.tuusuario.mmo.eldencraft.skills.druid;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FormaOso implements Skill {
    @Override public String getName() { return "Forma Oso"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 3; }
    @Override public int getCooldown() { return 360; } // 6 mins

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        double maxHpOriginal = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double bonusHp = 20.0 + data.getConstitution() * 1.5;
        double newMax = maxHpOriginal + bonusHp;

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMax);
        player.setHealth(Math.min(newMax, player.getHealth() + bonusHp));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6000, 0));

        player.getWorld().spawnParticle(Particle.COMPOSTER,
                player.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_POLAR_BEAR_WARNING, 1f, 0.7f);
        player.sendMessage("§2§l✦ ¡Forma Oso! §7Durará 5 minutos. ¡Vida y resistencia aumentadas!");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHpOriginal);
            if (player.getHealth() > maxHpOriginal) player.setHealth(maxHpOriginal);
            player.removePotionEffect(PotionEffectType.RESISTANCE);
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            player.sendMessage("§7✦ Forma Oso terminó. Vuelves a tu forma normal.");
        }, 6000L);
    }
}
