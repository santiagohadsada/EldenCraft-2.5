package com.tuusuario.mmo.eldencraft.skills.warlock;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PrisionAlmas implements Skill {
    @Override public String getName() { return "Prisión de Almas"; }
    @Override public double getCost() { return 50.0; }
    @Override public int getLevelRequired() { return 16; }

    @Override
    public void execute(Player player, PlayerData data) {
        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        org.bukkit.Location center = player.getLocation().add(0, 1, 0);

        // Ítem visual flotante inagarrable (Aliento de dragón)
        org.bukkit.entity.ItemDisplay dragonBreath = player.getWorld().spawn(center, org.bukkit.entity.ItemDisplay.class);
        dragonBreath.setItemStack(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DRAGON_BREATH));
        dragonBreath.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
        dragonBreath.setGlowing(true);
        dragonBreath.setGlowColorOverride(org.bukkit.Color.PURPLE);

        int trapped = 0;
        for (Entity e : player.getNearbyEntities(7, 7, 7)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            mob.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 2));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 5));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            mob.damage(8.0 + data.getIntelligence() * 0.4, player);
            trapped++;
        }
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, center, 40, 2.0, 1.0, 2.0, 0.05);
        player.playSound(center, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.8f, 0.7f);
        player.sendMessage("§8✦ Prisión de Almas: §7" + trapped + " enemigos aprisionados!");

        // Aura cerrándose
        org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 100 || !player.isOnline()) {
                    task.cancel();
                    dragonBreath.remove();
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 1);
                    return;
                }

                // Particulas moradas cerrandose progresivamente hacia el centro
                double radius = 7.0 - (ticks * (7.0 / 100.0));
                for (int s = 0; s < 4; s++) {
                    double angle = ticks * 0.3 + (s * (Math.PI / 2));
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    player.getWorld().spawnParticle(Particle.DRAGON_BREATH,
                            center.clone().add(x, 0, z), 2, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.PORTAL,
                            center.clone().add(x, Math.sin(ticks*0.2), z), 5, 0, 0, 0, 0.1);
                }
            }
        }, 0L, 1L);
    }
}
