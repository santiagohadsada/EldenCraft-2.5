package com.tuusuario.mmo.eldencraft.util;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.PigZombie;

// Helper para identificar muertos vivientes en habilidades del Paladín.
public final class UndeadCheck {
    private UndeadCheck() {}

    public static boolean isUndead(Entity e) {
        return e instanceof Zombie
                || e instanceof Skeleton
                || e instanceof Stray
                || e instanceof Husk
                || e instanceof Drowned
                || e instanceof ZombieVillager
                || e instanceof PigZombie
                || e instanceof WitherSkeleton
                || e instanceof Wither
                || e instanceof Phantom
                || e instanceof Zoglin
                || e instanceof SkeletonHorse
                || e instanceof ZombieHorse;
    }
}