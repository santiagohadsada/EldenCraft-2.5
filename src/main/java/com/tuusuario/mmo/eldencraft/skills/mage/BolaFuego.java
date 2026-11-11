package com.tuusuario.mmo.eldencraft.skills.mage;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class BolaFuego implements Skill {
    public String getName() { return "Bola de Fuego"; }
    public double getCost() { return 15.0; }
    public int getLevelRequired() { return 1; }

    @Override public CastType getCastType() { return CastType.CHARGEABLE; }
    @Override public int getMaxChargeTicks() { return 40; } // 2 segundos

    @Override
    public void execute(Player player, PlayerData data) {
        execute(player, data, 1.0);
    }

    @Override
    public void execute(Player player, PlayerData data, double charge) {
        Fireball fireball = player.launchProjectile(Fireball.class);
        // Daño escala con la carga
        float power = 1.6f + (float)(data.getIntelligence() / 12.0);
        fireball.setYield(power * (float)(0.3 + (charge * 1.5))); // 30% a 180% de poder
        fireball.setIsIncendiary(false);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, (float)(1.5 - charge));
        player.sendMessage("§6¡Bola de Fuego! §7(Carga: " + (int)(charge*100) + "%)");
    }
}
