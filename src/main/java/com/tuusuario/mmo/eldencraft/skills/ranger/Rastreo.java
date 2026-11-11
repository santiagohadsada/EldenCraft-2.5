package com.tuusuario.mmo.eldencraft.skills.ranger;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class Rastreo implements Skill {
    @Override
    public String getName() { return "Rastreo"; }
    @Override
    public double getCost() { return 10.0; }
    @Override
    public int getLevelRequired() { return 1; }

    @Override
    public void execute(Player player, PlayerData data) {
        // Velocidad escala con Destreza
        int power = Math.max(0, (data.getDexterity() / 15));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, power));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1f, 1.2f);
        player.sendMessage("§a[!] §7Tus sentidos se agudizan. Rastreo activo.");
    }
}