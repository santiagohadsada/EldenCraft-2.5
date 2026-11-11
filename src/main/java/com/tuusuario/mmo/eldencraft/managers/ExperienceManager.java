package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

// aca esta la tabla de exp q te da cada mob, los hostiles dan mas y los pasivos casi nada
public class ExperienceManager {

    private final EldenCraft plugin;
    private final Map<EntityType, Integer> mobExpMap = new HashMap<>();

    // ── Tabla XP SRD 5.1 ─────────────────────────────────────────────────────
    // Índice = nivel del personaje (1-20), valor = XP total acumulada para ese nivel
    // # VERIFICAR: confirmar tabla XP SRD 5.1 completa
    private static final int[] XP_THRESHOLDS = {
        0,       // nivel 1  = 0 XP
        300,     // nivel 2
        900,     // nivel 3
        2700,    // nivel 4
        6500,    // nivel 5
        14000,   // nivel 6
        23000,   // nivel 7
        34000,   // nivel 8
        48000,   // nivel 9
        64000,   // nivel 10
        85000,   // nivel 11
        100000,  // nivel 12
        120000,  // nivel 13
        140000,  // nivel 14
        165000,  // nivel 15
        195000,  // nivel 16
        225000,  // nivel 17
        265000,  // nivel 18
        305000,  // nivel 19
        355000   // nivel 20
    };

    // Niveles de ASI (Ability Score Improvement)
    private static final int[] ASI_LEVELS = {4, 8, 12, 16, 19};

    public ExperienceManager(EldenCraft plugin) {
        this.plugin = plugin;
        setupMobExp();
    }

    private void setupMobExp() {
        // mobs hostiles
        put("ZOMBIE", 6);
        put("ZOMBIE_VILLAGER", 8);
        put("HUSK", 7);
        put("DROWNED", 8);
        put("SKELETON", 7);
        put("STRAY", 9);
        put("BOGGED", 9);          // 1.21+
        put("WITHER_SKELETON", 25);
        put("CREEPER", 10);
        put("SPIDER", 5);
        put("CAVE_SPIDER", 6);
        put("ENDERMAN", 18);
        put("ENDERMITE", 6);
        put("SILVERFISH", 4);
        put("SLIME", 4);
        put("MAGMA_CUBE", 5);
        put("BLAZE", 18);
        put("GHAST", 25);
        put("WITCH", 16);
        put("PHANTOM", 14);
        put("VEX", 10);
        put("PILLAGER", 12);
        put("VINDICATOR", 18);
        put("EVOKER", 30);
        put("ILLUSIONER", 30);
        put("RAVAGER", 50);
        put("HOGLIN", 14);
        put("ZOGLIN", 16);
        put("PIGLIN", 8);
        put("PIGLIN_BRUTE", 22);
        put("ZOMBIFIED_PIGLIN", 6);
        put("WARDEN", 200);
        put("BREEZE", 25);          // 1.21+
        put("GUARDIAN", 14);
        put("ELDER_GUARDIAN", 100);
        put("SHULKER", 25);

        // mobs neutrales
        put("WOLF", 4);
        put("POLAR_BEAR", 6);
        put("LLAMA", 3);
        put("TRADER_LLAMA", 3);
        put("PANDA", 3);
        put("BEE", 2);
        put("DOLPHIN", 3);
        put("FOX", 3);
        put("GOAT", 3);
        put("IRON_GOLEM", 12);
        put("SNOW_GOLEM", 4);

        // pasivos, dan re poco
        put("COW", 1);
        put("PIG", 1);
        put("CHICKEN", 1);
        put("SHEEP", 1);
        put("HORSE", 2);
        put("DONKEY", 2);
        put("MULE", 2);
        put("RABBIT", 1);
        put("MOOSHROOM", 2);
        put("CAT", 1);
        put("OCELOT", 2);
        put("PARROT", 1);
        put("VILLAGER", 1);
        put("WANDERING_TRADER", 2);
        put("AXOLOTL", 1);
        put("FROG", 1);
        put("CAMEL", 2);            // 1.20
        put("SNIFFER", 2);          // 1.20
        put("ALLAY", 1);
        put("ARMADILLO", 2);        // 1.21+
        put("GLOW_SQUID", 1);
        put("SQUID", 1);
        put("BAT", 1);
        put("COD", 1);
        put("SALMON", 1);
        put("PUFFERFISH", 1);
        put("TROPICAL_FISH", 1);
        put("TADPOLE", 1);
        put("STRIDER", 2);
        put("TURTLE", 2);

        // los jefes dan un monton
        put("WITHER", 800);
        put("ENDER_DRAGON", 1200);
    }

    // helper q ignora si el entitytype no existe en la version del server
    private void put(String entityName, int exp) {
        try {
            EntityType t = EntityType.valueOf(entityName);
            mobExpMap.put(t, exp);
        } catch (IllegalArgumentException ignored) {
            // no existe en esta version, no pasa nada lo salteamos
        }
    }

    // devuelve la exp base q da un mob
    public int getExpFromMob(EntityType type) {
        int base = mobExpMap.getOrDefault(type, 3);
        double mul = plugin.getConfig().getDouble("experience.mob-exp-multiplier", 1.0);
        return Math.max(1, (int) (base * mul));
    }

    /**
     * XP requerida para pasar del nivel actual al siguiente (tabla SRD 5.1).
     * Si useSrdTable = false en config, usa la fórmula exponencial legacy.
     */
    public int getRequiredExp(int level) {
        if (plugin.getConfig().getBoolean("experience.use-srd-table", false)) {
            return getRequiredXpForLevel(level);
        }
        // Fórmula legacy original (retrocompatibilidad)
        return (int) (150 * Math.pow(1.45, level - 1));
    }

    /**
     * XP necesaria para ir del nivel `level` al `level+1` según tabla SRD 5.1.
     * Devuelve la diferencia entre umbrales consecutivos.
     */
    public int getRequiredXpForLevel(int level) {
        if (level <= 0 || level >= 20) return Integer.MAX_VALUE;
        return XP_THRESHOLDS[level] - XP_THRESHOLDS[level - 1];
    }

    /**
     * XP total acumulada necesaria para alcanzar el nivel dado (tabla SRD 5.1).
     */
    public int getTotalXpForLevel(int level) {
        if (level <= 1 || level > 20) return 0;
        return XP_THRESHOLDS[level - 1];
    }

    // chequea si subio de nivel y le da las recompensas
    public void checkLevelUp(Player player, PlayerData data) {
        boolean leveled = false;
        while (data.getLevel() < 20) {
            int req = getRequiredExp(data.getLevel());
            if (data.getXp() >= req) {
                data.setXp(data.getXp() - req);
                data.setLevel(data.getLevel() + 1);
                onLevelUp(player, data);
                leveled = true;
            } else break;
        }
        if (data.getLevel() >= 20) data.setXp(0);

        if (leveled) {
            // actualizamos la vida maxima con la api de atributos
            applyMaxHealth(player, data);
        }
    }

    // aplica la vida maxima usando getAttribute, pq setMaxHealth ya no existe en 1.21
    public static void applyMaxHealth(Player player, PlayerData data) {
        try {
            var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(data.getMaxHealth());
                if (player.getHealth() > data.getMaxHealth()) {
                    player.setHealth(data.getMaxHealth());
                }
            }
        } catch (Throwable ignored) { }
    }

    private void onLevelUp(Player player, PlayerData data) {
        int nuevo = data.getLevel();
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.sendMessage("");

        // Usar MessageUtil si está disponible, sino fallback a strings directos
        Map<String, String> ph = new HashMap<>();
        ph.put("nivel", String.valueOf(nuevo));
        MessageUtil.sendPrefixed(player, "character.level-up", ph);

        // Verificar si es nivel de ASI
        for (int asiLevel : ASI_LEVELS) {
            if (nuevo == asiLevel) {
                Map<String, String> asiPh = new HashMap<>();
                asiPh.put("nivel", String.valueOf(nuevo));
                MessageUtil.send(player, "character.asi-available", asiPh);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                break;
            }
        }

        // Habilidades desbloqueadas (niveles hito del plugin legacy)
        int[] hitos = {1, 3, 6, 9, 12, 16, 20};
        for (int h : hitos) {
            if (nuevo == h) {
                player.sendMessage(ChatColor.AQUA + "✦ ¡Nueva habilidad desbloqueada! "
                        + ChatColor.WHITE + "Pulsa [F] para verla.");
                break;
            }
        }

        // le damos oro fijo por nivel
        int gold = plugin.getEconomyManager().getGoldPerLevel();
        if (gold > 0) {
            plugin.getEconomyManager().addGold(data, gold);
            player.sendMessage(ChatColor.GOLD + "+ " + gold + " oro");
        }

        // recompensas q se configuran en el config.yml
        plugin.getRewardManager().giveLevelReward(player, data, nuevo);
        player.sendMessage("");
    }
}
