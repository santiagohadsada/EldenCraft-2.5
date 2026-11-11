package com.tuusuario.mmo.eldencraft.skills.mage;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Teleportacion implements Skill {
    private static final int MAX_DISTANCE = 15;

    public String getName() { return "Teleportación"; }
    public double getCost() { return 25.0; }
    public int getLevelRequired() { return 6; }

    public void execute(Player player, PlayerData data) {
        Location origin = player.getLocation().clone();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        Location target;
        Block hit = player.getTargetBlockExact(MAX_DISTANCE, FluidCollisionMode.NEVER);
        if (hit != null) {
            // Aterriza encima del bloque al que se apunta
            target = hit.getLocation().add(0.5, 1.0, 0.5);
        } else {
            // Mirando al cielo / sin bloque: avanza MAX_DISTANCE en la direccion de la mirada
            target = eye.clone().add(dir.multiply(MAX_DISTANCE));
        }
        target.setYaw(origin.getYaw());
        target.setPitch(origin.getPitch());

        // Busca un sitio donde quepa el jugador (cabeza + pies libres)
        Location safe = findSafeLocation(target);
        if (safe == null) {
            player.sendMessage("§c§l» §7No hay un lugar seguro al que teletransportarte.");
            return;
        }

        // Efectos en el origen
        player.getWorld().spawnParticle(Particle.PORTAL,
                origin.clone().add(0, 1, 0), 60, 0.4, 1.0, 0.4, 0.5);
        player.playSound(origin, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        player.teleport(safe);

        // Efectos en el destino
        player.getWorld().spawnParticle(Particle.PORTAL,
                safe.clone().add(0, 1, 0), 60, 0.4, 1.0, 0.4, 0.5);
        player.playSound(safe, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private Location findSafeLocation(Location loc) {
        // Intenta en el sitio exacto, luego baja hasta 5 bloques buscando suelo
        for (int dy = 0; dy <= 5; dy++) {
            Location candidate = loc.clone().subtract(0, dy, 0);
            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0, 1, 0).getBlock();
            Block ground = candidate.clone().subtract(0, 1, 0).getBlock();
            if (isPassable(feet) && isPassable(head) && !isPassable(ground) && !isDangerous(ground)) {
                return candidate;
            }
        }
        // Sin suelo: si al menos cabe (cabeza + pies), aterriza igualmente (caera en el aire)
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        if (isPassable(feet) && isPassable(head)) return loc;
        return null;
    }

    private boolean isPassable(Block b) {
        return b.isPassable();
    }

    private boolean isDangerous(Block b) {
        Material m = b.getType();
        return m == Material.LAVA || m == Material.FIRE || m == Material.SOUL_FIRE
                || m == Material.MAGMA_BLOCK || m == Material.CAMPFIRE || m == Material.SOUL_CAMPFIRE;
    }
}
