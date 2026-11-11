package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// todo lo de los jefes legendarios, se cargan del yml
public class BossManager {

    private final EldenCraft plugin;
    private final Map<String, BossDef> bosses = new LinkedHashMap<>();
    private final Map<java.util.UUID, org.bukkit.boss.BossBar> activeBossBars = new java.util.concurrent.ConcurrentHashMap<>();

    // clave nbt pa marcar a las entidades como jefes
    public final NamespacedKey BOSS_KEY;

    public BossManager(EldenCraft plugin) {
        this.plugin = plugin;
        this.BOSS_KEY = new NamespacedKey(plugin, "boss_id");
        loadBosses();
        startBossBarTask();
    }

    private void startBossBarTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (java.util.Map.Entry<java.util.UUID, org.bukkit.boss.BossBar> entry : activeBossBars.entrySet()) {
                java.util.UUID uuid = entry.getKey();
                org.bukkit.boss.BossBar bar = entry.getValue();
                org.bukkit.entity.Entity entity = Bukkit.getEntity(uuid);
                
                if (!(entity instanceof LivingEntity le) || le.isDead() || !le.isValid()) {
                    bar.removeAll();
                    activeBossBars.remove(uuid);
                    continue;
                }

                BossDef def = getBossOf(le);
                if (def == null) {
                    bar.removeAll();
                    activeBossBars.remove(uuid);
                    continue;
                }

                double maxHealth = le.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null 
                    ? le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() 
                    : def.maxHealth;
                double ratio = le.getHealth() / maxHealth;
                bar.setProgress(Math.max(0.0, Math.min(1.0, ratio)));

                // Cambio dinámico de color según la vida
                if (ratio > 0.6) {
                    bar.setColor(org.bukkit.boss.BarColor.RED);
                } else if (ratio > 0.25) {
                    bar.setColor(org.bukkit.boss.BarColor.YELLOW);
                } else {
                    bar.setColor(org.bukkit.boss.BarColor.PINK);
                }

                // Título dinámico con vida actual
                bar.setTitle(def.display + " §7- §c" + (int)le.getHealth() + "§7/§c" + (int)maxHealth + " HP");

                // Jugadores en rango de 40 bloques aislados por campaña (admins ven todo)
                java.util.List<Player> nearby = new java.util.ArrayList<>();
                for (Player p : le.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(le.getLocation()) <= 1600.0) {
                        boolean isAdmin = p.isOp() || p.hasPermission("eldencraft.admin");
                        if (isAdmin) {
                            nearby.add(p);
                        } else {
                            String playerCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, p);
                            var camp = plugin.getCampaignManager().getCampaign(playerCamp);
                            if (camp != null && le.getWorld().getName().equalsIgnoreCase(camp.getWorldName())) {
                                nearby.add(p);
                            } else if ("principal".equalsIgnoreCase(playerCamp) && (le.getWorld().getName().equalsIgnoreCase("world") || le.getWorld().getName().equalsIgnoreCase("world_nether") || le.getWorld().getName().equalsIgnoreCase("world_the_end"))) {
                                nearby.add(p);
                            }
                        }
                    }
                }

                java.util.List<Player> current = new java.util.ArrayList<>(bar.getPlayers());
                for (Player p : nearby) {
                    if (!current.contains(p)) {
                        bar.addPlayer(p);
                    }
                }
                for (Player p : current) {
                    if (!nearby.contains(p)) {
                        bar.removePlayer(p);
                    }
                }
            }
        }, 20L, 20L);
    }

    public void removeBossBar(java.util.UUID uuid) {
        org.bukkit.boss.BossBar bar = activeBossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void clearAllBossBars() {
        for (org.bukkit.boss.BossBar bar : activeBossBars.values()) {
            bar.removeAll();
        }
        activeBossBars.clear();
    }

    // carga o recarga el bosses.yml
    public void loadBosses() {
        bosses.clear();

        File f = new File(plugin.getDataFolder(), "bosses.yml");
        if (!f.exists()) plugin.saveResource("bosses.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);

        // mezclamos con los defaults del jar pa q no se rompa si falta algo
        try (InputStreamReader r = new InputStreamReader(plugin.getResource("bosses.yml"))) {
            cfg.setDefaults(YamlConfiguration.loadConfiguration(r));
        } catch (Exception ignored) { }

        ConfigurationSection root = cfg.getConfigurationSection("bosses");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;

            BossDef b = new BossDef();
            b.id = id;
            b.entityName = s.getString("entity", "ZOMBIE");
            b.display = ChatColor.translateAlternateColorCodes('&', s.getString("display", id));
            b.maxHealth = s.getDouble("max-health", 200);
            b.damage = s.getDouble("damage", 8);
            b.armor = s.getDouble("armor", 6);
            b.speed = s.getDouble("speed", 1.0);
            b.glowing = s.getBoolean("glowing", true);
            b.expReward = s.getInt("exp-reward", 500);
            b.goldReward = s.getInt("gold-reward", 250);
            b.announce = ChatColor.translateAlternateColorCodes('&',
                    s.getString("announce", "&6Un jefe ha aparecido!"));

            for (String entry : s.getStringList("loot")) {
                String[] p = entry.split(":");
                if (p.length < 1) continue;
                try {
                    Material mat = Material.valueOf(p[0].trim().toUpperCase());
                    int qty = (p.length >= 2) ? Math.max(1, Integer.parseInt(p[1].trim())) : 1;
                    b.loot.add(new ItemStack(mat, qty));
                } catch (Exception ignored) { /* si el item no existe en esta version lo ignoramos */ }
            }

            bosses.put(id.toLowerCase(), b);
        }

        plugin.getLogger().info("Cargados " + bosses.size() + " jefes legendarios.");
    }

    public Map<String, BossDef> getBosses() { return bosses; }

    // spawnea un jefe en la posicion q le digas
    public LivingEntity spawn(String bossId, Location loc) {
        BossDef def = bosses.get(bossId.toLowerCase());
        if (def == null || loc == null || loc.getWorld() == null) return null;

        EntityType type;
        try { type = EntityType.valueOf(def.entityName); }
        catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Boss '" + def.id + "' usa entityType desconocido en esta version: " + def.entityName);
            return null;
        }

        Entity e = loc.getWorld().spawnEntity(loc, type);
        if (!(e instanceof LivingEntity le)) {
            e.remove();
            return null;
        }

        // le ponemos el tag de jefe
        le.getPersistentDataContainer().set(BOSS_KEY, PersistentDataType.STRING, def.id);
        le.setCustomName(def.display);
        le.setCustomNameVisible(true);
        le.setRemoveWhenFarAway(false);
        le.setGlowing(def.glowing);

        // le seteamos los atributos de vida, daño, etc
        applyAttribute(le, Attribute.GENERIC_MAX_HEALTH, def.maxHealth);
        le.setHealth(Math.min(def.maxHealth, le.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
        applyAttribute(le, Attribute.GENERIC_ATTACK_DAMAGE, def.damage);
        applyAttribute(le, Attribute.GENERIC_ARMOR, def.armor);
        applyAttribute(le, Attribute.GENERIC_MOVEMENT_SPEED, baseSpeed(le) * def.speed);

        // aviso global pa q todos sepan
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l☠ §e§lJEFE INVOCADO §6§l☠");
        Bukkit.broadcastMessage(def.announce);
        Bukkit.broadcastMessage("§7Coordenadas: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ()
                + " §7(" + loc.getWorld().getName() + ")");
        // Crear boss bar
        org.bukkit.boss.BossBar bossBar = Bukkit.createBossBar(
                def.display,
                org.bukkit.boss.BarColor.RED,
                org.bukkit.boss.BarStyle.SOLID
        );
        bossBar.setVisible(true);
        activeBossBars.put(le.getUniqueId(), bossBar);

        return le;
    }

    private void applyAttribute(LivingEntity e, Attribute a, double value) {
        try {
            var inst = e.getAttribute(a);
            if (inst != null) inst.setBaseValue(value);
        } catch (Throwable ignored) { }
    }

    private double baseSpeed(LivingEntity e) {
        try {
            var inst = e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            return inst != null ? inst.getBaseValue() : 0.25;
        } catch (Throwable ignored) { return 0.25; }
    }

    // busca si una entidad es un jefe nuestro por el tag nbt
    public BossDef getBossOf(LivingEntity e) {
        String id = e.getPersistentDataContainer().get(BOSS_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        return bosses.get(id.toLowerCase());
    }

    // clase interna con los datos de un jefe en memoria
    public static class BossDef {
        public String id;
        public String entityName;
        public String display;
        public double maxHealth;
        public double damage;
        public double armor;
        public double speed;
        public boolean glowing;
        public int expReward;
        public int goldReward;
        public String announce;
        public List<ItemStack> loot = new ArrayList<>();
    }

    // devuelve un map con id -> nombre bonito, pa listarlos
    public Map<String, String> listIds() {
        Map<String, String> m = new HashMap<>();
        for (var e : bosses.entrySet()) m.put(e.getKey(), e.getValue().display);
        return m;
    }
}
