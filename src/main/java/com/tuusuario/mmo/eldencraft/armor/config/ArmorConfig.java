package com.tuusuario.mmo.eldencraft.armor.config;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorCategory;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity;
import com.tuusuario.mmo.eldencraft.armor.armor.StatType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Wrapper "tipado" sobre config.yml.
// La idea es no andar leyendo strings y casteando en mil sitios.
public final class ArmorConfig {

    // Defaults hardcoded segun el documento de diseno. Se usan SIEMPRE
    // como fallback aunque el config.yml sea viejo o no tenga la seccion.
    private static final Map<String, Set<ArmorCategory>> DEFAULT_RESTRICTIONS;
    static {
        Map<String, Set<ArmorCategory>> m = new HashMap<>();
        m.put("MAGE",      EnumSet.of(ArmorCategory.LIGERA));
        m.put("BARD",      EnumSet.of(ArmorCategory.LIGERA));
        m.put("BARBARIAN", EnumSet.of(ArmorCategory.LIGERA));
        m.put("ROGUE",     EnumSet.of(ArmorCategory.LIGERA, ArmorCategory.INTERMEDIA));
        m.put("RANGER",    EnumSet.of(ArmorCategory.LIGERA, ArmorCategory.INTERMEDIA));
        m.put("WARRIOR",   EnumSet.allOf(ArmorCategory.class));
        m.put("PALADIN",   EnumSet.allOf(ArmorCategory.class));
        // Clases nuevas
        m.put("SORCERER",  EnumSet.of(ArmorCategory.LIGERA));
        m.put("WARLOCK",   EnumSet.of(ArmorCategory.LIGERA));
        m.put("MONK",      EnumSet.of(ArmorCategory.LIGERA));
        m.put("DRUID",     EnumSet.of(ArmorCategory.LIGERA, ArmorCategory.INTERMEDIA));
        m.put("CLERIC",    EnumSet.of(ArmorCategory.LIGERA, ArmorCategory.INTERMEDIA));
        m.put("GUNSLINGER",EnumSet.of(ArmorCategory.LIGERA, ArmorCategory.INTERMEDIA));
        DEFAULT_RESTRICTIONS = m;
    }

    private final EldenCraft plugin;

    private final Map<String, Set<ArmorCategory>> classRestrictions = new HashMap<>();
    private final Map<StatType, Double> caps = new EnumMap<>(StatType.class);
    private final Map<ArmorRarity, Integer> rarityWeights = new EnumMap<>(ArmorRarity.class);

    // anti-exploit
    private boolean removePieceOnInvalid = true;
    private boolean applyEffectsOnInvalid = true;
    private int slownessAmplifier = 1;
    private int weaknessAmplifier = 0;
    private double periodicDamage = 1.0;
    private int checkIntervalTicks = 40;

    // mejoras
    private int maxUpgradeLevel = 5;
    private int upgradeBaseCost = 5;     // exp inicial
    private int upgradeStepCost = 5;     // se suma por cada nivel siguiente

    // drops
    private boolean dropsEnabled = true;
    private double bossBaseChance = 0.05;     // mobs de la lista "fuertes"
    private double miniBossBaseChance = 0.02; // mobs intermedios (golem, brute, etc.)
    private double dragonBossChance = 0.25;   // bosses unicos: dragon, wither, warden
    private double levelMultiplier = 0.005;
    private double difficultyMultiplier = 1.0; // multiplicador segun world.getDifficulty()

    public ArmorConfig(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        // -------- restricciones de clase --------
        // Empezamos con los defaults hardcoded, luego sobreescribimos
        // con lo que diga el config si se ha definido. Asi nunca queda vacio
        // aunque el usuario tenga un config viejo de una version anterior.
        classRestrictions.clear();
        for (Map.Entry<String, Set<ArmorCategory>> e : DEFAULT_RESTRICTIONS.entrySet()) {
            classRestrictions.put(e.getKey(), EnumSet.copyOf(e.getValue()));
        }
        ConfigurationSection cr = cfg.getConfigurationSection("class-restrictions");
        if (cr != null) {
            for (String key : cr.getKeys(false)) {
                Set<ArmorCategory> set = EnumSet.noneOf(ArmorCategory.class);
                for (String s : cr.getStringList(key)) {
                    try { set.add(ArmorCategory.valueOf(s.toUpperCase())); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (!set.isEmpty()) {
                    classRestrictions.put(key.toUpperCase(), set);
                }
            }
        }

        // -------- anti-exploit --------
        ConfigurationSection ae = cfg.getConfigurationSection("anti-exploit");
        if (ae != null) {
            removePieceOnInvalid  = ae.getBoolean("remove-piece", true);
            applyEffectsOnInvalid = ae.getBoolean("apply-effects", true);
            slownessAmplifier     = ae.getInt("slowness-amplifier", 1);
            weaknessAmplifier     = ae.getInt("weakness-amplifier", 0);
            periodicDamage        = ae.getDouble("periodic-damage", 1.0);
            checkIntervalTicks    = ae.getInt("check-interval-ticks", 40);
        }

        // -------- caps globales --------
        // Defaults segun el documento de diseno: critico 40%, velocidad 20%,
        // reduccion 50%. Si lo definen en el yml lo respeto.
        caps.clear();
        caps.put(StatType.CRIT_CHANCE,        40.0);
        caps.put(StatType.SPEED_BONUS,        20.0);
        caps.put(StatType.DAMAGE_REDUCTION,   50.0);
        caps.put(StatType.DAMAGE_BONUS,       100.0);
        caps.put(StatType.COOLDOWN_REDUCTION, 40.0);
        caps.put(StatType.LIFESTEAL,          25.0);
        caps.put(StatType.EXTRA_HEALTH,       20.0);
        caps.put(StatType.DEFENSE,            40.0);
        ConfigurationSection cp = cfg.getConfigurationSection("caps");
        if (cp != null) {
            putCap(cp, "crit-chance",        StatType.CRIT_CHANCE);
            putCap(cp, "damage-bonus",       StatType.DAMAGE_BONUS);
            putCap(cp, "damage-reduction",   StatType.DAMAGE_REDUCTION);
            putCap(cp, "speed-bonus",        StatType.SPEED_BONUS);
            putCap(cp, "cooldown-reduction", StatType.COOLDOWN_REDUCTION);
            putCap(cp, "lifesteal",          StatType.LIFESTEAL);
            putCap(cp, "extra-health",       StatType.EXTRA_HEALTH);
            putCap(cp, "defense",            StatType.DEFENSE);
        }

        // -------- mejoras --------
        ConfigurationSection up = cfg.getConfigurationSection("upgrades");
        if (up != null) {
            maxUpgradeLevel = up.getInt("max-level", 5);
            upgradeBaseCost = up.getInt("base-cost", 5);
            upgradeStepCost = up.getInt("step-cost", 5);
        }

        // -------- drops --------
        rarityWeights.clear();
        ConfigurationSection drops = cfg.getConfigurationSection("drops");
        if (drops != null) {
            dropsEnabled        = drops.getBoolean("enabled", true);
            bossBaseChance      = drops.getDouble("boss-base-chance", 0.05);
            miniBossBaseChance  = drops.getDouble("miniboss-base-chance", 0.02);
            dragonBossChance    = drops.getDouble("dragon-boss-chance", 0.25);
            levelMultiplier     = drops.getDouble("level-multiplier", 0.005);
            difficultyMultiplier= drops.getDouble("difficulty-multiplier", 1.0);

            ConfigurationSection rw = drops.getConfigurationSection("rarity-weights");
            if (rw != null) {
                for (ArmorRarity r : ArmorRarity.values()) {
                    rarityWeights.put(r, rw.getInt(r.name(), defaultWeight(r)));
                }
            } else {
                for (ArmorRarity r : ArmorRarity.values()) rarityWeights.put(r, defaultWeight(r));
            }
        } else {
            for (ArmorRarity r : ArmorRarity.values()) rarityWeights.put(r, defaultWeight(r));
        }
    }

    private int defaultWeight(ArmorRarity r) {
        // pongo unas weights por defecto bastante "Diablo": comun comun, raro raro,
        // epico/legendario muy raros.
        switch (r) {
            case COMUN:      return 70;
            case RARO:       return 22;
            case EPICO:      return 7;
            case LEGENDARIO: return 1;
            default:         return 1;
        }
    }

    private void putCap(ConfigurationSection s, String key, StatType type) {
        if (s.contains(key)) caps.put(type, s.getDouble(key));
    }

    // ---- API ----
    public Set<ArmorCategory> allowedCategories(String className) {
        if (className == null) return EnumSet.allOf(ArmorCategory.class);
        return classRestrictions.getOrDefault(className.toUpperCase(),
                EnumSet.allOf(ArmorCategory.class));
    }

    public boolean isAllowed(String className, ArmorCategory category) {
        if (className == null || category == null) return true;
        Set<ArmorCategory> allowed = classRestrictions.get(className.toUpperCase());
        return allowed == null || allowed.contains(category);
    }

    public int    maxUpgradeLevel()        { return maxUpgradeLevel; }
    public int    upgradeBaseCost()        { return upgradeBaseCost; }
    public int    upgradeStepCost()        { return upgradeStepCost; }
    public double cap(StatType t)          { return caps.getOrDefault(t, Double.MAX_VALUE); }

    public boolean removePieceOnInvalid()  { return removePieceOnInvalid; }
    public boolean applyEffectsOnInvalid() { return applyEffectsOnInvalid; }
    public int     slownessAmplifier()     { return slownessAmplifier; }
    public int     weaknessAmplifier()     { return weaknessAmplifier; }
    public double  periodicDamage()        { return periodicDamage; }
    public int     checkIntervalTicks()    { return checkIntervalTicks; }

    public boolean dropsEnabled()          { return dropsEnabled; }
    public double  bossBaseChance()        { return bossBaseChance; }
    public double  miniBossBaseChance()    { return miniBossBaseChance; }
    public double  dragonBossChance()      { return dragonBossChance; }
    public double  levelMultiplier()       { return levelMultiplier; }
    public double  difficultyMultiplier()  { return difficultyMultiplier; }
    public Map<ArmorRarity, Integer> rarityWeights() { return rarityWeights; }
}
