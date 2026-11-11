package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Muestra la ficha completa de personaje D&D 5e (/ficha [jugador]).
 */
public class FichaCommand implements CommandExecutor {

    private final EldenCraft plugin;

    public FichaCommand(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length > 0) {
            // Ver la ficha de otro jugador (requiere permiso de DM o Admin)
            if (!sender.hasPermission("eldencraft.dm") && !sender.hasPermission("eldencraft.admin") && !sender.isOp()) {
                MessageUtil.send(sender, "no-permission");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.send(sender, "ficha.player-not-found");
                return true;
            }

            // Aislamiento por campaña: Solo DM de la campaña del jugador o admin pueden ver fichas ajenas
            if (sender instanceof Player viewer) {
                boolean isAdmin = viewer.isOp() || viewer.hasPermission("eldencraft.admin");
                if (!isAdmin) {
                    boolean isDm = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.isDmForPlayer(plugin, viewer, target);
                    String vCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, viewer);
                    String tCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, target);
                    if (!isDm || !vCamp.equalsIgnoreCase(tCamp)) {
                        viewer.sendMessage(ChatColor.RED + "No puedes ver la ficha de un personaje de otra campaña.");
                        return true;
                    }
                }
            }
        } else {
            if (!(sender instanceof Player p)) {
                MessageUtil.send(sender, "player-only");
                return true;
            }
            target = p;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (data == null) {
            MessageUtil.send(sender, "ficha.player-not-found");
            return true;
        }

        showFicha(sender, target, data);
        return true;
    }

    private void showFicha(CommandSender viewer, Player player, PlayerData data) {
        String raza = data.getRace() != null ? data.getRace().displayName : "Sin elegir";
        String clase = data.getPlayerClass() != null ? data.getPlayerClass().displayName : "Sin elegir";
        int nivel = data.getLevel();
        int xp = data.getXp();
        int xpNext = plugin.getExperienceManager().getRequiredExp(nivel);

        int hpCur = data.getDndHpCurrent();
        int hpMax = data.getDndHpMax();
        if (hpMax == 0) {
            hpMax = (int) data.getMaxHealth();
            hpCur = hpMax;
        }

        boolean slotsMode = "SLOTS".equalsIgnoreCase(plugin.getConfig().getString("resource-mode", "SLOTS"));
        String tipoRecurso = "Maná";
        if (data.getPlayerClass() != null) {
            if (slotsMode && data.getPlayerClass().isCaster()) {
                tipoRecurso = "Espacios de Conjuro (SLOTS)";
            } else if ("ENERGIA".equalsIgnoreCase(data.getPlayerClass().resourceType)) {
                tipoRecurso = "Energía";
            }
        }
        int recCur = (int) data.getCurrentResource();
        int recMax = (int) data.getMaxResource();

        int str = data.getEffectiveStr();
        int dex = data.getEffectiveDex();
        int con = data.getEffectiveCon();
        int intel = data.getEffectiveInt();
        int wis = data.getEffectiveWis();
        int cha = data.getEffectiveCha();

        viewer.sendMessage(MessageUtil.get("ficha.header"));
        viewer.sendMessage(MessageUtil.get("ficha.title", Map.of("nombre", player.getName())));
        viewer.sendMessage(MessageUtil.get("ficha.race-class", Map.of("raza", raza, "clase", clase)));
        viewer.sendMessage(MessageUtil.get("ficha.level-xp", Map.of("nivel", String.valueOf(nivel), "xp", String.valueOf(xp), "xp_next", String.valueOf(xpNext))));
        viewer.sendMessage(MessageUtil.get("ficha.hp", Map.of("hp_actual", String.valueOf(hpCur), "hp_max", String.valueOf(hpMax))));
        viewer.sendMessage(MessageUtil.get("ficha.resource", Map.of("recurso", tipoRecurso, "actual", String.valueOf(recCur), "max", String.valueOf(recMax))));
        viewer.sendMessage(MessageUtil.get("ficha.fatigue", Map.of("fatiga", String.valueOf(data.getFatigue()))));
        viewer.sendMessage(MessageUtil.get("ficha.gold", Map.of("oro", String.valueOf(data.getGold()))));

        viewer.sendMessage(MessageUtil.get("ficha.attrs", Map.of(
                "str", String.valueOf(str), "str_mod", formatMod(AttributeCalculator.modifier(str)),
                "dex", String.valueOf(dex), "dex_mod", formatMod(AttributeCalculator.modifier(dex)),
                "con", String.valueOf(con), "con_mod", formatMod(AttributeCalculator.modifier(con))
        )));
        viewer.sendMessage(MessageUtil.get("ficha.attrs2", Map.of(
                "int", String.valueOf(intel), "int_mod", formatMod(AttributeCalculator.modifier(intel)),
                "wis", String.valueOf(wis), "wis_mod", formatMod(AttributeCalculator.modifier(wis)),
                "cha", String.valueOf(cha), "cha_mod", formatMod(AttributeCalculator.modifier(cha))
        )));

        // Si tiene espacios de conjuro, mostrarlos
        if (data.getPlayerClass() != null && com.tuusuario.mmo.eldencraft.spells.SpellSlotTable.isSpellcaster(data.getPlayerClass())) {
            StringBuilder slotsStr = new StringBuilder(ChatColor.DARK_AQUA + "Espacios: " + ChatColor.WHITE);
            int[] maxS = data.getSpellSlotsMax();
            int[] usedS = data.getSpellSlotsUsed();
            boolean hasAny = false;
            for (int i = 0; i < 9; i++) {
                if (maxS[i] > 0) {
                    hasAny = true;
                    int avail = maxS[i] - usedS[i];
                    slotsStr.append("N").append(i + 1).append(":").append(avail).append("/").append(maxS[i]).append(" ");
                }
            }
            if (hasAny) {
                viewer.sendMessage(slotsStr.toString().trim());
            }
        }

        viewer.sendMessage(MessageUtil.get("ficha.footer"));
    }

    private String formatMod(int mod) {
        return mod >= 0 ? "+" + mod : String.valueOf(mod);
    }
}
