package com.tuusuario.mmo.eldencraft.armor.drops;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorType;
import com.tuusuario.mmo.eldencraft.armor.config.ArmorConfig;
import org.bukkit.Difficulty;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

// Drop de armaduras RPG SOLO desde bosses / criaturas dificiles.
// La probabilidad escala con la dificultad del mundo y un poco con el "nivel"
// del jugador (uso health amplifier o killer's level si esta disponible).
public final class DropManager {

    // Tres "tiers" de fuente de drop. La idea: chusma comun NO dropea,
    // los miniboss dropean a veces, los bosses unicos casi siempre.
    private static final Set<EntityType> MINI_BOSSES = EnumSet.of(
            EntityType.IRON_GOLEM,
            EntityType.PIGLIN_BRUTE,
            EntityType.RAVAGER,
            EntityType.BREEZE,
            EntityType.ELDER_GUARDIAN
    );
    private static final Set<EntityType> BIG_BOSSES = EnumSet.of(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.WARDEN
    );

    private final EldenCraft plugin;
    private final Random random = new Random();

    public DropManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // Punto de entrada que llama el listener de muertes.
    public void handleMobDeath(LivingEntity dead, Player killer) {
        ArmorConfig cfg = plugin.armorConfig();
        if (!cfg.dropsEnabled()) return;
        if (dead == null) return;

        SourceTier tier = classify(dead);
        if (tier == SourceTier.NONE) return;

        double chance = baseChanceFor(tier, cfg);

        // multiplicador por dificultad del mundo
        Difficulty diff = dead.getWorld().getDifficulty();
        chance *= difficultyMultiplier(diff) * cfg.difficultyMultiplier();

        // si tenemos killer humano, le sumo un poco por nivel/exp
        if (killer != null) {
            int lvl = killer.getLevel();
            chance += lvl * cfg.levelMultiplier();
        }

        // capeo a 1.0 para no romper el random
        if (chance > 1.0) chance = 1.0;

        if (random.nextDouble() > chance) return;

        // Determinar rareza segun el tier:
        //  - mini boss: comun/raro casi siempre, epico raro, legendario casi nunca
        //  - big boss: directamente epico/legendario y materiales tier alto
        ArmorRarity rarity = rollRarityFor(tier);
        ArmorType type = rollTypeFor(tier);
        ArmorSlot slot = ArmorSlot.values()[random.nextInt(ArmorSlot.values().length)];

        int upgradeLevel = 0; // los drops salen sin mejorar; el jugador las sube
        ItemStack drop = plugin.armorManager().generate(type, slot, rarity, upgradeLevel);
        if (drop == null) return;

        dead.getWorld().dropItemNaturally(dead.getLocation(), drop);
    }

    private SourceTier classify(LivingEntity dead) {
        EntityType t = dead.getType();
        if (BIG_BOSSES.contains(t)) return SourceTier.BIG;
        if (MINI_BOSSES.contains(t)) return SourceTier.MINI;
        // creeper cargado cuenta como mini
        if (t == EntityType.CREEPER && dead instanceof Creeper c && c.isPowered()) return SourceTier.MINI;
        // hook para custom bosses: si el plugin marca al mob con metadata "rpg_boss" o "rpg_miniboss"
        if (dead.hasMetadata("rpg_boss")) return SourceTier.BIG;
        if (dead.hasMetadata("rpg_miniboss")) return SourceTier.MINI;
        return SourceTier.NONE;
    }

    private double baseChanceFor(SourceTier tier, ArmorConfig cfg) {
        return switch (tier) {
            case MINI -> cfg.miniBossBaseChance();
            case BIG  -> cfg.dragonBossChance();
            default   -> cfg.bossBaseChance();
        };
    }

    private double difficultyMultiplier(Difficulty d) {
        if (d == null) return 1.0;
        return switch (d) {
            case PEACEFUL -> 0.0;
            case EASY     -> 0.7;
            case NORMAL   -> 1.0;
            case HARD     -> 1.4;
        };
    }

    private ArmorRarity rollRarityFor(SourceTier tier) {
        // pesos rapidos
        int[] weights;
        if (tier == SourceTier.BIG) {
            weights = new int[]{0, 10, 60, 30}; // COMUN, RARO, EPICO, LEGENDARIO
        } else {
            weights = new int[]{55, 35, 9, 1};
        }
        int total = 0; for (int w : weights) total += w;
        int roll = random.nextInt(total);
        int acc = 0;
        ArmorRarity[] rs = ArmorRarity.values();
        for (int i = 0; i < rs.length; i++) {
            acc += weights[i];
            if (roll < acc) return rs[i];
        }
        return ArmorRarity.COMUN;
    }

    private ArmorType rollTypeFor(SourceTier tier) {
        // big boss: alta probabilidad de diamond/netherite
        if (tier == SourceTier.BIG) {
            int r = random.nextInt(10);
            if (r < 5) return ArmorType.NETHERITE;
            if (r < 9) return ArmorType.DIAMOND;
            return ArmorType.IRON;
        }
        // mini boss: rango medio
        ArmorType[] pool = {ArmorType.LEATHER, ArmorType.CHAINMAIL,
                            ArmorType.IRON, ArmorType.IRON,
                            ArmorType.GOLD, ArmorType.DIAMOND};
        return pool[random.nextInt(pool.length)];
    }

    private enum SourceTier { NONE, MINI, BIG }
}
