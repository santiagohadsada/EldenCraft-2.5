package com.tuusuario.mmo.eldencraft.skills.cleric;

import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;

public class IraDivina implements Skill {

    private static final Set<EntityType> UNDEAD = Set.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK,
            EntityType.DROWNED, EntityType.SKELETON, EntityType.STRAY,
            EntityType.WITHER_SKELETON, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE,
            EntityType.PHANTOM, EntityType.WITHER, EntityType.ZOGLIN,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN_BRUTE
    );

    @Override public String getName() { return "Ira Divina"; }
    @Override public double getCost() { return 40.0; }
    @Override public int getLevelRequired() { return 12; }

    @Override
    public void execute(Player player, PlayerData data) {
        int hit = 0;
        double dmgNormal = 8.0 + data.getWisdom() * 0.4;
        double dmgUndead = dmgNormal * 2.5;

        com.tuusuario.mmo.eldencraft.EldenCraft plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(com.tuusuario.mmo.eldencraft.EldenCraft.class);
        for (Entity e : player.getNearbyEntities(8, 8, 8)) {
            if (!(e instanceof LivingEntity) || plugin.getPartyManager().isAlly(player, e)) continue;
            LivingEntity mob = (LivingEntity) e;
            boolean isUndead = UNDEAD.contains(mob.getType());
            mob.damage(isUndead ? dmgUndead : dmgNormal, player);
            player.getWorld().strikeLightningEffect(mob.getLocation());
            mob.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    mob.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
            hit++;
        }
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 40, 2.0, 1.0, 2.0, 0.1);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.3f);
        player.sendMessage("§f✦ Ira Divina: §7" + hit + " enemigos golpeados (x2.5 vs no-muertos).");
    }
}
