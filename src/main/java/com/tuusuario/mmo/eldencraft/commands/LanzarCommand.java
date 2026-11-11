package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.spells.EffectEngine;
import com.tuusuario.mmo.eldencraft.spells.SpellDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellRegistry;
import com.tuusuario.mmo.eldencraft.spells.SpellValidator;
import com.tuusuario.mmo.eldencraft.spells.SpellVisuals;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Comando para lanzar un hechizo D&D 5e (/lanzar <id> [nivel]).
 */
public class LanzarCommand implements CommandExecutor {

    private final EldenCraft plugin;
    private final EffectEngine effectEngine;

    public LanzarCommand(EldenCraft plugin) {
        this.plugin = plugin;
        this.effectEngine = new EffectEngine(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, "player-only");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /lanzar <id_hechizo> [nivel_espacio]");
            player.sendMessage(ChatColor.GRAY + "Ejemplo: /lanzar curar_heridas 1");
            return true;
        }

        String spellId = args[0].toLowerCase();
        SpellDefinition spell = SpellRegistry.getInstance().get(spellId);
        if (spell == null) {
            MessageUtil.send(player, "spell.not-found", Map.of("id", spellId));
            return true;
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Error cargando tus datos de personaje.");
            return true;
        }

        // Determinar nivel de lanzamiento (por defecto el nivel base del hechizo)
        int castLevel = spell.getNivel();
        if (args.length > 1) {
            try {
                castLevel = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "spell.invalid-level");
                return true;
            }
        }

        // Validar restricciones de clase y espacios
        SpellValidator.ValidationResult val = SpellValidator.validateCast(data, spell, castLevel);
        if (!val.valid) {
            player.sendMessage(ChatColor.RED + val.reason);
            return true;
        }

        // Consumir espacio de conjuro si no es truco (nivel 0)
        if (spell.getNivel() > 0) {
            data.useSpellSlot(castLevel);
        }

        // Concentración
        if (spell.isConcentracion()) {
            if (data.getConcentrationSpell() != null) {
                player.sendMessage(ChatColor.GRAY + "Terminaste la concentración en tu hechizo anterior.");
            }
            data.setConcentrationSpell(spell.getId());
            plugin.getConcentrationManager().startConcentration(player.getUniqueId(), spell.getId());
        }

        // Ejecutar efectos (aislado por campaña para jugadores)
        LivingEntity target = getTargetEntity(player, 25);
        if (target instanceof Player targetPlayer) {
            boolean isAdmin = player.isOp() || player.hasPermission("eldencraft.admin");
            if (!isAdmin) {
                String pCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, player);
                String tCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, targetPlayer);
                if (!pCamp.equalsIgnoreCase(tCamp)) {
                    player.sendMessage(ChatColor.RED + "No puedes afectar con hechizos a jugadores de otra campaña.");
                    target = null;
                }
            }
        }
        effectEngine.execute(player, data, spell, castLevel, target);

        // Efectos visuales por escuela
        SpellVisuals.playSchoolEffect(player, target != null ? target.getLocation() : player.getLocation(), spell.getEscuela());

        MessageUtil.send(player, "spell.cast", Map.of("hechizo", spell.getNombre()));
        return true;
    }

    private LivingEntity getTargetEntity(Player player, double range) {
        try {
            var ray = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    range,
                    entity -> entity instanceof LivingEntity && !entity.equals(player)
            );
            if (ray != null && ray.getHitEntity() instanceof LivingEntity living) {
                return living;
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
