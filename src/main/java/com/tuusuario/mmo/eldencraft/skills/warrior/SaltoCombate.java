/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tuusuario.mmo.eldencraft.skills.warrior;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class SaltoCombate implements Skill {
    public String getName() { return "Salto de Combate"; }
    public double getCost() { return 35.0; }
    public int getLevelRequired() { return 16; }

    public void execute(Player player, PlayerData data) {
        // Impulso físico (Vectores)
        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.7));
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 1f, 1f);
        
        // Daño en área al aterrizar (o al activarse para simplificar)
        player.getNearbyEntities(3, 3, 3).forEach(e -> {
            if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                ((org.bukkit.entity.LivingEntity) e).damage(3.0 + (data.getStrength() * 0.2));
            }
        });
    }
}