package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.missions.Mission;
import com.tuusuario.mmo.eldencraft.missions.MissionProgress;
import com.tuusuario.mmo.eldencraft.missions.MissionType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

// carga las misiones del missions.yml y maneja cuando las aceptas, progresas y completas
public class MissionManager {

    private final EldenCraft plugin;
    private final Map<String, Mission> missions = new LinkedHashMap<>();

    public MissionManager(EldenCraft plugin) {
        this.plugin = plugin;
        loadMissions();
    }

    // lee el archivo missions.yml y lo parsea
    private void loadMissions() {
        File file = new File(plugin.getDataFolder(), "missions.yml");
        if (!file.exists()) {
            plugin.saveResource("missions.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = cfg.getConfigurationSection("missions");
        if (sec == null) {
            plugin.getLogger().warning("[EldenCraft] missions.yml no tiene seccion 'missions'.");
            return;
        }
        for (String id : sec.getKeys(false)) {
            ConfigurationSection m = sec.getConfigurationSection(id);
            if (m == null) continue;
            try {
                Mission mission = new Mission(
                        id,
                        m.getString("name", id),
                        MissionType.valueOf(m.getString("type", "KILL").toUpperCase()),
                        m.getString("target", "ANY"),
                        m.getInt("amount", 1),
                        m.getInt("exp", 100),
                        m.getInt("gold", 50),
                        m.getString("difficulty", "NORMAL"),
                        m.getInt("minLevel", 1)
                );
                missions.put(id, mission);
            } catch (Exception e) {
                plugin.getLogger().warning("[EldenCraft] Error cargando mision '" + id + "': " + e.getMessage());
            }
        }
        plugin.getLogger().info("[EldenCraft] Cargadas " + missions.size() + " misiones.");
    }

    public Map<String, Mission> getAllMissions() {
        return missions;
    }

    public Mission getMission(String id) {
        return missions.get(id);
    }

    // acepta una mision pa el jugador, chequea q no la tenga ya
    public boolean acceptMission(Player player, PlayerData data, String missionId) {
        Mission mission = missions.get(missionId);
        if (mission == null) return false;

        if (data.getCompletedMissions().contains(missionId)) {
            player.sendMessage(ChatColor.RED + "Ya completaste esta mision.");
            return false;
        }
        if (data.getActiveMissions().containsKey(missionId)) {
            player.sendMessage(ChatColor.YELLOW + "Ya tienes esta mision activa.");
            return false;
        }
        if (data.getLevel() < mission.getMinLevel()) {
            player.sendMessage(ChatColor.RED + "Necesitas nivel " + mission.getMinLevel() + " para esta mision.");
            return false;
        }

        data.getActiveMissions().put(missionId, new MissionProgress(missionId, 0));
        player.sendMessage(ChatColor.GREEN + "✦ Mision aceptada: " + ChatColor.GOLD + mission.getName());
        player.sendMessage(ChatColor.GRAY + "  Objetivo: " + ChatColor.WHITE + mission.getAmount()
                + " " + mission.getTarget() + " (" + mission.getType().name() + ")");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
        return true;
    }

    // suma progreso a todas las misiones activas q matcheen con el tipo y target
    public void addProgress(Player player, MissionType type, String target, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getActiveMissions().isEmpty()) return;

        for (MissionProgress prog : data.getActiveMissions().values()) {
            Mission m = missions.get(prog.getMissionId());
            if (m == null || m.getType() != type) continue;

            // vemos si el target coincide
            String mTarget = m.getTarget();
            boolean match = mTarget.equals("ANY") || mTarget.equals(target);
            if (!match && mTarget.equals("ANY_HOSTILE") && type == MissionType.KILL) {
                // si es ANY_HOSTILE chequeamos si es un mob hostil
                match = isHostileTarget(target);
            }
            if (!match) continue;

            int before = prog.getProgress();
            prog.addProgress(amount);
            int now = Math.min(prog.getProgress(), m.getAmount());
            prog.setProgress(now);

            // avisamos cada 5 unidades pa no llenar el chat
            if (before < m.getAmount() && now >= m.getAmount()) {
                completeMission(player, data, m);
            } else if (before / 5 != now / 5) {
                // mostramos el progreso cada 5 pa no spamear
                player.sendActionBar(ChatColor.AQUA + "📜 " + m.getName()
                        + " §7(" + now + "/" + m.getAmount() + ")");
            }
        }
    }

    // marca como completada y da las recompensas
    private void completeMission(Player player, PlayerData data, Mission mission) {
        data.getActiveMissions().remove(mission.getId());
        data.getCompletedMissions().add(mission.getId());

        // le damos la exp y el oro
        int expMul = (int) (mission.getExpReward()
                * plugin.getConfig().getDouble("experience.mission-exp-multiplier", 1.0));
        data.setXp(data.getXp() + expMul);
        plugin.getEconomyManager().addGold(data, mission.getGoldReward());

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "✦✦✦ MISION COMPLETADA ✦✦✦");
        player.sendMessage(ChatColor.YELLOW + mission.getName());
        player.sendMessage(ChatColor.GREEN + " +" + expMul + " EXP   " + ChatColor.GOLD + "+"
                + mission.getGoldReward() + " oro");
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // vemos si subio de nivel
        plugin.getExperienceManager().checkLevelUp(player, data);
    }

    // lista de mobs hostiles pa verificar el target ANY_HOSTILE
    public static boolean isHostileTarget(String type) {
        switch (type) {
            case "ZOMBIE": case "SKELETON": case "CREEPER": case "SPIDER": case "ENDERMAN":
            case "WITCH": case "WITHER_SKELETON": case "SLIME": case "PILLAGER":
            case "VINDICATOR": case "RAVAGER": case "EVOKER": case "ILLUSIONER":
            case "PIGLIN": case "PIGLIN_BRUTE": case "ZOGLIN": case "HOGLIN":
            case "BLAZE": case "GHAST": case "MAGMA_CUBE": case "GUARDIAN":
            case "ELDER_GUARDIAN": case "PHANTOM": case "DROWNED": case "HUSK":
            case "STRAY": case "SHULKER": case "VEX": case "ENDERMITE":
            case "SILVERFISH": case "CAVE_SPIDER": case "WITHER":
            case "ENDER_DRAGON": case "WARDEN": case "BREEZE":
                return true;
            default:
                return false;
        }
    }
}
