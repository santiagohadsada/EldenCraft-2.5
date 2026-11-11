package com.tuusuario.mmo.eldencraft.rules;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona la concentración en hechizos D&D 5e.
 * Cuando un jugador recibe daño, realiza salvación de CON o pierde la concentración.
 */
public class ConcentrationManager implements Listener {

    private final EldenCraft plugin;
    private final CheckEngine checkEngine = new CheckEngine();
    // uuid → id del hechizo en concentración
    private final Map<UUID, String> concentrating = new HashMap<>();

    public ConcentrationManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public void startConcentration(UUID uuid, String spellId) {
        concentrating.put(uuid, spellId);
    }

    public void breakConcentration(UUID uuid) {
        concentrating.remove(uuid);
    }

    public boolean isConcentrating(UUID uuid) {
        return concentrating.containsKey(uuid);
    }

    public String getConcentrationSpell(UUID uuid) {
        return concentrating.get(uuid);
    }

    // ── Listener ─────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        if (!isConcentrating(uuid)) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(uuid);
        if (data == null) return;

        // CD = max(10, daño / 2)
        int damage = (int) Math.ceil(event.getFinalDamage());
        int dc = Math.max(10, damage / 2);

        // Salvación de CON; el jugador tiene competencia si es lanzador (simplificado: siempre proficiente para lanzadores)
        // # VERIFICAR: qué clases tienen prof. en salvaciones de CON según SRD
        CheckResult result = checkEngine.performSavingThrow(data, Attribute.CON, dc, false);

        if (!result.success) {
            String spell = concentrating.remove(uuid);
            // Limpiar en PlayerData también
            data.setConcentrationSpell(null);

            Map<String, String> ph = new HashMap<>();
            MessageUtil.send(player, "spell.concentration-broken");
            plugin.getLogger().fine("[Concentración] " + player.getName() + " perdió concentración en " + spell
                    + " (d20=" + result.roll + "+" + result.modifier + "=" + result.total + " vs CD " + dc + ")");
        }
    }
}
