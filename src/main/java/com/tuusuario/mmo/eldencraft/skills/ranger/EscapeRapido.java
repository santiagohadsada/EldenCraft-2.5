package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.GlowEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

// Salto del Ciervo: gran salto vertical, velocidad alta y caída suave.
public class EscapeRapido implements Skill {

    @Override
    public String getName() { return "Salto del Ciervo"; }
    @Override
    public double getCost() { return 50.0; }
    @Override
    public int getLevelRequired() { return 20; }

    @Override
    public void execute(Player player, PlayerData data) {
        EldenCraft plugin = JavaPlugin.getPlugin(EldenCraft.class);

        // Glow verde naturaleza
        GlowEffect.apply(plugin, player, ChatColor.GREEN, 200);

        // Impulso vertical (escala suavemente con destreza)
        double impulso = 1.5 + Math.min(0.6, data.getDexterity() * 0.02);
        Vector v = player.getVelocity();
        v.setY(impulso);
        // Pequeño empujón hacia adelante para "saltar" en la dirección de la mirada
        Vector mirada = player.getLocation().getDirection().setY(0).normalize().multiply(0.7);
        v.add(mirada);
        player.setVelocity(v);

        // Buff de movilidad y caída segura
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));

        // Sonido épico de despegue
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 1f, 1.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.4f, 2.0f);
        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, 0.5f, 1.4f);

        // Explosión de partículas al saltar
        player.getWorld().spawnParticle(Particle.CLOUD,
                player.getLocation(), 50, 0.6, 0.1, 0.6, 0.1);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                player.getLocation().add(0, 1, 0), 25, 0.8, 0.8, 0.8, 0);
        player.getWorld().spawnParticle(Particle.ENCHANT,
                player.getLocation().add(0, 0.5, 0), 60, 1.0, 0.5, 1.0, 1.0);

        // Trail de partículas ENCHANT continuo durante el buff
        Particle.DustOptions forestGreen = new Particle.DustOptions(Color.fromRGB(34, 139, 34), 1.2f);
        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;
            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                if (ticks++ > 200 || !player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (ticks % 3 == 0) {
                    player.getWorld().spawnParticle(Particle.ENCHANT,
                            player.getLocation().add(0, 0.5, 0), 5, 0.3, 0.5, 0.3, 0.5);
                    player.getWorld().spawnParticle(Particle.DUST,
                            player.getLocation().add(0, 1, 0), 3, 0.4, 0.7, 0.4, 0, forestGreen);
                }
            }
        }, 0L, 1L);

        player.sendMessage("§a§l🦌 ¡SALTO DEL CIERVO! §7Saltas con la agilidad de un ciervo.");
    }
}
