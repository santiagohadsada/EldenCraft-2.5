/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class CuracionMagica implements Skill {
    public String getName() { return "Curación Mágica"; }
    public double getCost() { return 40.0; }
    public int getLevelRequired() { return 16; }

    public void execute(Player player, PlayerData data) {
        // Restaura 5 corazones (10 puntos) + bono por inteligencia
        double heal = 10.0 + (data.getIntelligence() / 5.0);
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heal));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
}