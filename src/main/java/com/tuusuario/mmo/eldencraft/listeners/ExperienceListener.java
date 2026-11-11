package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.managers.ExperienceManager;
import com.tuusuario.mmo.eldencraft.managers.MissionManager;
import com.tuusuario.mmo.eldencraft.managers.PartyManager;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Sistema de experiencia con party:
 * - Sin party: el matador recibe el 100% de EXP base.
 * - Con party: TODOS los miembros (incluido el matador) dentro del radio reciben
 *   el 90% de la EXP base. Es un ligero penalizador de compartir, pero compensa
 *   con la ventaja de jugar en equipo.
 * - No se aplica el bug anterior de +5 fijo: ahora usa el % correcto de la EXP real.
 */
public class ExperienceListener implements Listener {

    // Porcentaje de EXP que reciben todos los miembros de party (incluido el matador)
    private static final double PARTY_XP_MULTIPLIER = 0.7;

    private final EldenCraft plugin;

    public ExperienceListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null) return;
        if (entity instanceof Player) return;

        Player killer = entity.getKiller();
        PlayerData killerData = plugin.getPlayerManager().getPlayerData(killer.getUniqueId());
        if (killerData == null) return;

        ExperienceManager expManager = plugin.getExperienceManager();
        int expBase = expManager.getExpFromMob(entity.getType());
        if (expBase <= 0) return;

        // calculamos cuanto oro dropea
        boolean esHostil = MissionManager.isHostileTarget(entity.getType().name());
        int gold = 0;
        if (esHostil) {
            gold = (int) Math.ceil(expBase * plugin.getEconomyManager().getHostileDropMultiplier());
        }

        PartyManager pm = plugin.getPartyManager();
        PartyManager.Party party = pm.getParty(killer.getUniqueId());

        if (party == null || party.members.size() <= 1) {
            // sin party: le damos toda la exp y oro
            killerData.setXp(killerData.getXp() + expBase);
            killer.sendMessage(ChatColor.YELLOW + "+" + expBase + " EXP "
                    + ChatColor.DARK_GRAY + "(" + entity.getType().name() + ")");
            if (gold > 0) {
                plugin.getEconomyManager().addGold(killerData, gold);
                killer.sendMessage(ChatColor.GOLD + "+" + gold + " oro");
            }
            expManager.checkLevelUp(killer, killerData);
        } else {
            // con party: todos reciben el 70% de exp (penalizador por jugar en grupo)
            int expParty = (int) Math.max(1, Math.round(expBase * PARTY_XP_MULTIPLIER));
            int goldParty = (int) Math.max(0, Math.round(gold * PARTY_XP_MULTIPLIER));

            double radiusSq = PartyManager.XP_SHARE_RADIUS_FROM_ENEMY
                    * PartyManager.XP_SHARE_RADIUS_FROM_ENEMY;
            Location enemyLoc = entity.getLocation();

            // exp al q mato (con el descuento de party)
            killerData.setXp(killerData.getXp() + expParty);
            killer.sendMessage(ChatColor.YELLOW + "+" + expParty + " EXP "
                    + ChatColor.DARK_GRAY + "(party, " + entity.getType().name() + ")");
            if (goldParty > 0) {
                plugin.getEconomyManager().addGold(killerData, goldParty);
                killer.sendMessage(ChatColor.GOLD + "+" + goldParty + " oro (party)");
            }
            expManager.checkLevelUp(killer, killerData);

            // Dar XP a los compañeros en rango
            for (Player mate : pm.getOnlineMembers(party)) {
                if (mate.getUniqueId().equals(killer.getUniqueId())) continue;
                if (!mate.getWorld().equals(enemyLoc.getWorld())) continue;
                if (mate.getLocation().distanceSquared(enemyLoc) > radiusSq) continue;

                PlayerData mateData = plugin.getPlayerManager().getPlayerData(mate.getUniqueId());
                if (mateData == null) continue;

                mateData.setXp(mateData.getXp() + expParty);
                mate.sendMessage(ChatColor.LIGHT_PURPLE + "+" + expParty + " EXP"
                        + ChatColor.DARK_GRAY + " (party de " + killer.getName() + ")");
                if (goldParty > 0) {
                    plugin.getEconomyManager().addGold(mateData, goldParty);
                    mate.sendMessage(ChatColor.GOLD + "+" + goldParty + " oro "
                            + ChatColor.DARK_GRAY + "(party)");
                }
                expManager.checkLevelUp(mate, mateData);
            }
        }
    }
}
