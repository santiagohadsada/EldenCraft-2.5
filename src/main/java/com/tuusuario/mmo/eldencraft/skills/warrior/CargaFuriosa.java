package com.tuusuario.mmo.eldencraft.skills.warrior;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class CargaFuriosa implements Skill {
    public String getName() { return "Carga Furiosa"; }
    public double getCost() { return 25.0; }
    public int getLevelRequired() { return 6; }

    public void execute(Player player, PlayerData data) {
        // Reducido: SPEED I + STR 0
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 0));
        player.setVelocity(player.getLocation().getDirection().multiply(1.4).setY(0.25));
        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1f, 1f);
    }
}
