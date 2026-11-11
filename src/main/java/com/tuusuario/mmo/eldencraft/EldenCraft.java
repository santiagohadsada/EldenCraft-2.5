package com.tuusuario.mmo.eldencraft;

import com.tuusuario.mmo.eldencraft.armor.armor.ArmorManager;
import com.tuusuario.mmo.eldencraft.armor.classes.ClassManager;
import com.tuusuario.mmo.eldencraft.armor.classes.EldenCraftClassManagerAdapter;
import com.tuusuario.mmo.eldencraft.armor.command.RpgArmorCommand;
import com.tuusuario.mmo.eldencraft.armor.config.ArmorConfig;
import com.tuusuario.mmo.eldencraft.armor.drops.DropManager;
import com.tuusuario.mmo.eldencraft.armor.listeners.AnvilListener;
import com.tuusuario.mmo.eldencraft.armor.listeners.ArmorListener;
import com.tuusuario.mmo.eldencraft.armor.listeners.MobDeathListener;
import com.tuusuario.mmo.eldencraft.armor.stats.StatManager;
import com.tuusuario.mmo.eldencraft.armor.upgrade.UpgradeManager;
import com.tuusuario.mmo.eldencraft.character.RestManager;
import com.tuusuario.mmo.eldencraft.commands.*;
import com.tuusuario.mmo.eldencraft.listeners.*;
import com.tuusuario.mmo.eldencraft.listeners.MobDifficultyListener;
import com.tuusuario.mmo.eldencraft.managers.BossManager;
import com.tuusuario.mmo.eldencraft.managers.DataManager;
import com.tuusuario.mmo.eldencraft.managers.EconomyManager;
import com.tuusuario.mmo.eldencraft.managers.ExperienceManager;
import com.tuusuario.mmo.eldencraft.managers.FlyManager;
import com.tuusuario.mmo.eldencraft.managers.MissionManager;
import com.tuusuario.mmo.eldencraft.managers.PartyManager;
import com.tuusuario.mmo.eldencraft.managers.RewardManager;
import com.tuusuario.mmo.eldencraft.managers.ShopManager;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.PlayerManager;
import com.tuusuario.mmo.eldencraft.recipes.StarterRecipes;
import com.tuusuario.mmo.eldencraft.rules.ConcentrationManager;
import com.tuusuario.mmo.eldencraft.skills.SkillManager;
import com.tuusuario.mmo.eldencraft.spells.SpellLoader;
import com.tuusuario.mmo.eldencraft.spells.SpellRegistry;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class EldenCraft extends JavaPlugin {

    // todos los managers del mmo, estan en orden de dependencia si no explota xd
    private PlayerManager      playerManager;
    private SkillManager       skillManager;
    private ExperienceManager  experienceManager;
    private EconomyManager     economyManager;
    private DataManager        dataManager;
    private MissionManager     missionManager;
    private ShopManager        shopManager;
    private RewardManager      rewardManager;
    private BossManager        bossManager;
    private PartyManager       partyManager;
    private FlyManager         flyManager;
    private com.tuusuario.mmo.eldencraft.managers.CastManager castManager;

    // F2: nuevos managers
    private RestManager        restManager;

    // F3: hechizos
    private SpellRegistry      spellRegistry;
    private ConcentrationManager concentrationManager;

    // F3B: multi-personaje y lobby
    private com.tuusuario.mmo.eldencraft.character.CharacterManager characterManager;
    private com.tuusuario.mmo.eldencraft.character.LobbyManager lobbyManager;
    private com.tuusuario.mmo.eldencraft.character.gui.CharacterCreationWizard.WizardListener wizardListener;

    // F3B-2: campañas
    private com.tuusuario.mmo.eldencraft.campaign.CampaignManager campaignManager;

    // lo de las armaduras rpg q agregamos despues
    private ArmorConfig    armorConfig;
    private ClassManager   armorClassManager;
    private StatManager    armorStatManager;
    private ArmorManager   armorManager;
    private UpgradeManager armorUpgradeManager;
    private DropManager    armorDropManager;

    // este listener lo necesitamos guardado pq lo usamos en el tickHud
    private AbilityListener abilityListener;

    // las recetas q te dan al principio segun tu clase
    private StarterRecipes starterRecipes;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // F1: Inicializar sistema de mensajes
        MessageUtil.init(this);

        // ojo con el orden aca, si los moves se rompe todo
        economyManager   = new EconomyManager(this);
        dataManager      = new DataManager(this);
        dataManager.connect(); // Inicializa la conexión SQLite + migraciones SchemaManager
        playerManager    = new PlayerManager(this);
        skillManager     = new SkillManager();
        rewardManager    = new RewardManager(this);
        experienceManager= new ExperienceManager(this);
        missionManager   = new MissionManager(this);
        shopManager      = new ShopManager(this);
        bossManager      = new BossManager(this);
        partyManager     = new PartyManager(this);
        flyManager       = new FlyManager(this);
        castManager      = new com.tuusuario.mmo.eldencraft.managers.CastManager(this);

        // F2: Rest manager
        restManager = new RestManager(this);

        // F3: Hechizos
        spellRegistry = SpellRegistry.getInstance();
        new SpellLoader(this).loadAll();
        concentrationManager = new ConcentrationManager(this);

        // F3B-1: Multi-personaje y Lobby
        characterManager = new com.tuusuario.mmo.eldencraft.character.CharacterManager(this);
        lobbyManager = new com.tuusuario.mmo.eldencraft.character.LobbyManager(this);
        wizardListener = new com.tuusuario.mmo.eldencraft.character.gui.CharacterCreationWizard.WizardListener(this);

        // F3B-2: Campañas
        campaignManager = new com.tuusuario.mmo.eldencraft.campaign.CampaignManager(this);

        // inicializamos la wea de armaduras
        armorConfig         = new ArmorConfig(this);
        armorConfig.reload();
        armorClassManager   = new EldenCraftClassManagerAdapter(this);
        armorStatManager    = new StatManager(this);
        armorManager        = new ArmorManager(this);
        armorUpgradeManager = new UpgradeManager(this);
        armorDropManager    = new DropManager(this);

        // recetas iniciales, tiene q ir despues del armorManager o se cae
        starterRecipes = new StarterRecipes(this);
        starterRecipes.register();

        // F1: Aplicar reglas de mundo configurables
        applyWorldRules();

        // registramos todos los listeners, son re varios
        abilityListener = new AbilityListener(this);
        var pm = getServer().getPluginManager();
        pm.registerEvents(new SelectionGUI(this), this);
        pm.registerEvents(new ClassSelectionGUI(this), this);
        pm.registerEvents(abilityListener, this);

        // F1: SpellListener desactivado por defecto (era prueba del palo)
        if (getConfig().getBoolean("legacy-skills.spell-listener", false)) {
            pm.registerEvents(new SpellListener(this), this);
            getLogger().info("[EldenCraft] SpellListener (legacy) ACTIVADO por config.");
        }

        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new ExperienceListener(this), this);
        pm.registerEvents(new MissionProgressListener(this), this);
        pm.registerEvents(new MissionGUI(this), this);
        pm.registerEvents(new ShopGUI(this), this);
        pm.registerEvents(new BossListener(this), this);
        pm.registerEvents(new BlockSafetyListener(), this);
        pm.registerEvents(new MobDifficultyListener(this), this);
        pm.registerEvents(new SkillsGUI(this), this);
        pm.registerEvents(new NewClassesListener(this), this);
        pm.registerEvents(new FlyListener(this), this);
        // listeners de armadura
        pm.registerEvents(new ArmorListener(this), this);
        pm.registerEvents(new AnvilListener(this), this);
        pm.registerEvents(new MobDeathListener(this), this);
        // F3: Concentración
        pm.registerEvents(concentrationManager, this);
        // esto tambien es listener xd
        pm.registerEvents(starterRecipes, this);
        // F3B-1: Lobby y Guis de personajes
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.character.LobbyListener(this), this);
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.character.gui.CharacterSelectorGUI.SelectorListener(this), this);
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.character.gui.CharacterActionsGUI.ActionsListener(this), this);
        pm.registerEvents(wizardListener, this);
        // F3B-2 / C3: GUIs de campañas y panel DM
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.campaign.gui.CampaignSelectorGUI.CampaignGUIListener(this), this);
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.campaign.gui.CampaignEditorGUI.CampaignEditorListener(this), this);
        pm.registerEvents(new com.tuusuario.mmo.eldencraft.campaign.gui.DMCampaignPanelGUI.DMCampaignPanelListener(this), this);

        // aca van todos los comandos, el bind es un helper q hicimos abajo
        bind("mmo",        new MMOCommand(this));
        bind("shop",       new ShopCommand(this));
        bind("missions",   new MissionCommand(this));
        bind("balance",    new BalanceCommand(this));
        bind("eldencraft", new AdminCommand(this));
        bind("boss",       new BossCommand(this));
        bind("skills",     new SkillsCommand(this));
        bind("party",      new PartyCommand(this));
        bind("rpgarmor",   new RpgArmorCommand(this));

        // F2: Nuevos comandos
        bind("ficha",      new FichaCommand(this));
        bind("roll",       new RollCommand(this));
        bind("prueba",     new PruebaCommand(this));
        bind("descanso",   new DescansoCommand(this));

        // F3: Hechizos
        bind("lanzar",     new LanzarCommand(this));

        // F3B-1: Multi-personaje y Lobby
        bind("personajes", new PersonajesCommand(this));
        bind("menu",       new PersonajesCommand(this));
        bind("personaje",  new PersonajeCommand(this));
        bind("lobby",      new LobbyCommand(this));

        // F3B-2: Campañas
        bind("campana",    new CampanaCommand(this));

        // F3B-3: Panel DM
        bind("dm",         new DMCommand(this));

        // autocompletado inteligente para los comandos principales
        var tab = new com.tuusuario.mmo.eldencraft.commands.EldenCraftTabCompleter(this);
        bindTab("mmo",        tab);
        bindTab("eldencraft", tab);
        bindTab("party",      tab);
        bindTab("boss",       tab);
        bindTab("rpgarmor",   tab);
        bindTab("ficha",      tab);
        bindTab("prueba",     tab);
        bindTab("descanso",   tab);
        bindTab("lanzar",     tab);
        bindTab("personaje",  tab);
        bindTab("campana",    tab);
        bindTab("dm",         tab);

        // Registrar PlaceholderAPI si está presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.tuusuario.mmo.eldencraft.util.EldenCraftPlaceholders(this).register();
            getLogger().info("¡PlaceholderAPI detectado! Marcadores de EldenCraft registrados.");
        }

        // si algun admin hace /reload, cargamos a los q ya estaban conectados
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerManager.loadPlayer(p.getUniqueId());
        }

        // el hud se actualiza cada segundo (20 ticks = 1 seg)
        Bukkit.getScheduler().runTaskTimer(this, this::tickHud, 0L, 20L);

        // esto hace la regen de las armaduras y esas cosas
        armorStatManager.startPassiveTask();

        // guardado automatico cada 5 min por si se cae el server (asíncrono para rendimiento)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> playerManager.saveAll(), 6000L, 6000L);

        // F3B/C1: Purga de personajes borrados tras 7 días al iniciar y cada 24 h (1.728.000 ticks)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (characterManager != null) {
                characterManager.purgeExpiredDeletedCharacters();
            }
        }, 60L, 1728000L);

        consoleBanner();
    }

    @Override
    public void onDisable() {
        if (flyManager != null)       flyManager.clearAll();
        if (bossManager != null)      bossManager.clearAllBossBars();
        if (armorStatManager != null) armorStatManager.shutdown();
        if (playerManager != null)    playerManager.saveAll();
        if (dataManager != null)      dataManager.close();
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED
                + "[EldenCraft] Plugin desactivado. Datos guardados.");
    }

    public void reloadAll() {
        reloadConfig();
        // F1: Recargar mensajes
        MessageUtil.reload();
        missionManager = new MissionManager(this);
        shopManager    = new ShopManager(this);
        if (bossManager  != null) bossManager.loadBosses();
        if (armorConfig  != null) armorConfig.reload();
        // F1: Reaplicar reglas de mundo
        applyWorldRules();
        // F3: Recargar hechizos
        SpellRegistry.getInstance().clear();
        new SpellLoader(this).loadAll();
        getLogger().info("[EldenCraft] Recarga completa.");
    }

    // ── F1: Reglas de mundo configurables ────────────────────────────────────

    /**
     * Aplica las reglas de mundo configuradas en config.yml bajo "world-rules".
     */
    private void applyWorldRules() {
        List<String> worlds = getConfig().getStringList("world-rules.apply-to-worlds");
        if (worlds.isEmpty()) return;

        boolean naturalRegen  = getConfig().getBoolean("world-rules.natural-regeneration", false);
        boolean mobSpawning   = getConfig().getBoolean("world-rules.mob-spawning", false);
        boolean keepInventory = getConfig().getBoolean("world-rules.keep-inventory", true);

        for (String worldName : worlds) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("[WorldRules] Mundo no encontrado: " + worldName);
                continue;
            }
            world.setGameRule(GameRule.NATURAL_REGENERATION, naturalRegen);
            world.setGameRule(GameRule.DO_MOB_SPAWNING,      mobSpawning);
            world.setGameRule(GameRule.KEEP_INVENTORY,       keepInventory);
            getLogger().info("[WorldRules] Reglas aplicadas en " + worldName
                    + " (naturalRegen=" + naturalRegen
                    + ", mobSpawning=" + mobSpawning
                    + ", keepInventory=" + keepInventory + ")");
        }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void bind(String cmd, org.bukkit.command.CommandExecutor exec) {
        var c = getCommand(cmd);
        if (c != null) c.setExecutor(exec);
        else getLogger().warning("[EldenCraft] Comando no encontrado en plugin.yml: /" + cmd);
    }

    private void bindTab(String cmd, org.bukkit.command.TabCompleter tab) {
        var c = getCommand(cmd);
        if (c != null) c.setTabCompleter(tab);
    }

    private void tickHud() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerManager.getPlayerData(player.getUniqueId());
            if (data == null) continue;

            // Omitimos el HUD si el jugador está volando activamente
            if (flyManager != null && flyManager.isFlying(player)) {
                continue;
            }

            // regenera mana o energia dependiendo de la clase
            double regen = data.getIntelligence() / 4.0;

            // pasiva del mago, le regenera un poco mas de mana
            com.tuusuario.mmo.eldencraft.models.ClassType pClass = data.getPlayerClass();
            if (pClass == com.tuusuario.mmo.eldencraft.models.ClassType.MAGE) {
                regen *= 1.5;
            }

            // Optimización: solo consultamos el bioma si el jugador es explorador o druida
            String bName = "";
            if (pClass == com.tuusuario.mmo.eldencraft.models.ClassType.RANGER
             || pClass == com.tuusuario.mmo.eldencraft.models.ClassType.DRUID) {
                bName = player.getLocation().getBlock().getBiome().name();
            }

            // pasiva del explorador: si esta en bosque o jungla le va mejor
            if (pClass == com.tuusuario.mmo.eldencraft.models.ClassType.RANGER) {
                if (bName.contains("FOREST") || bName.contains("JUNGLE") || bName.contains("PLAINS")) {
                    regen *= 1.5;
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false));
                }
            }

            // pasiva druida: segun el bioma le da distintos buffs, re copado
            if (pClass == com.tuusuario.mmo.eldencraft.models.ClassType.DRUID) {
                if (bName.contains("ICE") || bName.contains("SNOW") || bName.contains("FROZEN")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false));
                } else if (bName.contains("DESERT") || bName.contains("BADLANDS")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false));
                } else if (bName.contains("OCEAN") || bName.contains("RIVER")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false));
                } else if (bName.contains("FOREST") || bName.contains("JUNGLE")) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, true, false));
                }
            }

            data.setCurrentResource(Math.min(data.getMaxResource(), data.getCurrentResource() + regen));

            // la fatiga va bajando de a poco
            if (data.getFatigue() > 0) {
                data.setFatigue(Math.max(0, data.getFatigue() - 2));
            }
            if (data.getFatigue() >= 100) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 40, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                player.damage(0.1);
            }

            if (abilityListener != null && abilityListener.isMenuOpen(player.getUniqueId())) {
                continue;
            }

            String tipoRecurso = "Mana";
            ChatColor colorRecurso = ChatColor.AQUA;
            if (data.getPlayerClass() != null) {
                String c = data.getPlayerClass().name();
                if (c.equals("WARRIOR") || c.equals("BARBARIAN")
                 || c.equals("ROGUE")   || c.equals("RANGER")
                 || c.equals("MONK")) {
                    tipoRecurso = "Energia";
                    colorRecurso = ChatColor.GOLD;
                }
            }
            int expReq = experienceManager.getRequiredExp(data.getLevel());

            // F1: Mostrar PG D&D si está activado
            String hpInfo = "";
            if (getConfig().getBoolean("hp.dnd-hp-enabled", true) && data.getDndHpMax() > 0) {
                hpInfo = ChatColor.RED + "❤" + data.getDndHpCurrent() + "/" + data.getDndHpMax() + " ";
            }

            String mensaje = hpInfo
                    + ChatColor.GREEN + "Niv: " + data.getLevel() + " "
                    + colorRecurso + tipoRecurso + ": " + (int) data.getCurrentResource() + " "
                    + ChatColor.WHITE + "EXP: " + data.getXp() + "/" + expReq + " "
                    + ChatColor.YELLOW + "Fat: " + data.getFatigue() + "% "
                    + ChatColor.GOLD + "\u2666 " + data.getGold();
            player.sendActionBar(mensaje);
        }
    }

    private void consoleBanner() {
        var con = Bukkit.getConsoleSender();
        con.sendMessage(ChatColor.GOLD + "========================================");
        con.sendMessage(ChatColor.YELLOW + "EldenCraft MMO: v3.1 (LOTE 1 — D&D 5e SRD)");
        con.sendMessage(ChatColor.YELLOW + "  - 13 Clases: Guerrero, Mago, Explorador, Paladín,");
        con.sendMessage(ChatColor.YELLOW + "               Ladrón, Bardo, Bárbaro, Hechicero,");
        con.sendMessage(ChatColor.YELLOW + "               Brujo, Monje, Druida, Clérigo, Pistolero");
        con.sendMessage(ChatColor.YELLOW + "  - 13 Razas, 91 Habilidades únicas (7 por clase)");
        con.sendMessage(ChatColor.YELLOW + "  - Motor D&D 5e SRD: dados, atributos, hechizos");
        con.sendMessage(ChatColor.YELLOW + "  - Armaduras RPG + Misiones + Economía + Party");
        con.sendMessage(ChatColor.YELLOW + "Autor: NaNO3");
        con.sendMessage(ChatColor.GOLD + "========================================");
    }

    // getters
    public PlayerManager     getPlayerManager()     { return playerManager; }
    public SkillManager      getSkillManager()      { return skillManager; }
    public ExperienceManager getExperienceManager() { return experienceManager; }
    public EconomyManager    getEconomyManager()    { return economyManager; }
    public DataManager       getDataManager()       { return dataManager; }
    public MissionManager    getMissionManager()    { return missionManager; }
    public ShopManager       getShopManager()       { return shopManager; }
    public RewardManager     getRewardManager()     { return rewardManager; }
    public BossManager       getBossManager()       { return bossManager; }
    public PartyManager      getPartyManager()      { return partyManager; }
    public FlyManager        getFlyManager()        { return flyManager; }
    public com.tuusuario.mmo.eldencraft.managers.CastManager getCastManager() { return castManager; }
    public AbilityListener   getAbilityListener()   { return abilityListener; }
    public StarterRecipes    getStarterRecipes()    { return starterRecipes; }
    public RestManager       getRestManager()       { return restManager; }
    public SpellRegistry     getSpellRegistry()     { return spellRegistry; }
    public ConcentrationManager getConcentrationManager() { return concentrationManager; }
    public com.tuusuario.mmo.eldencraft.character.CharacterManager getCharacterManager() { return characterManager; }
    public com.tuusuario.mmo.eldencraft.character.LobbyManager getLobbyManager() { return lobbyManager; }
    public com.tuusuario.mmo.eldencraft.character.gui.CharacterCreationWizard.WizardListener getWizardListener() { return wizardListener; }
    public com.tuusuario.mmo.eldencraft.campaign.CampaignManager getCampaignManager() { return campaignManager; }

    // getters del modulo de armaduras
    public ArmorConfig    armorConfig()    { return armorConfig; }
    public ClassManager   classManager()   { return armorClassManager; }
    public StatManager    statManager()    { return armorStatManager; }
    public ArmorManager   armorManager()   { return armorManager; }
    public UpgradeManager upgradeManager() { return armorUpgradeManager; }
    public DropManager    dropManager()    { return armorDropManager; }
}
