package com.tuusuario.mmo.eldencraft.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
// le mete 
public final class GlowEffect {

    private GlowEffect() {}

    public static void apply(Plugin plugin, Player player, ChatColor color, int durationTicks) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "ec_glow_" + color.name().toLowerCase();
        Team team = sb.getTeam(teamName);
        if (team == null) {
            try {
                team = sb.registerNewTeam(teamName);
                team.setColor(color);
            } catch (IllegalArgumentException e) {
                // Otro plugin pudo crear el equipo entre el get y el register
                team = sb.getTeam(teamName);
            }
        }
        if (team != null && !team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING, durationTicks, 0, false, false, true));

        final Team finalTeam = team;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                if (finalTeam != null) finalTeam.removeEntry(player.getName());
                player.removePotionEffect(PotionEffectType.GLOWING);
            } catch (Throwable ignored) {}
        }, durationTicks);
    }
}
