package com.tuusuario.mmo.eldencraft.skills.warrior;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class GolpePoderoso implements Skill {
    public String getName() { return "Golpe Poderoso"; }
    public double getCost() { return 15.0; }
    public int getLevelRequired() { return 1; }

    public void execute(Player player, PlayerData data) {
        // Reducido: máximo STR I (antes podía dar STR III)
        int power = Math.max(0, Math.min(1, data.getStrength() / 30));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, power));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.8f);
        player.sendMessage("§6§l» §7¡Tus músculos se tensan! Golpe Poderoso activo (5s).");
    }
}
