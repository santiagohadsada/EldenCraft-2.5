package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.skills.Skill;
import com.tuusuario.mmo.eldencraft.skills.sorcerer.Sobrecarga;
import com.tuusuario.mmo.eldencraft.util.SorcererState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbilityListener implements Listener {

    private final EldenCraft plugin;
    private final Set<UUID> menuAbierto = new HashSet<>();

    // cooldowns: uuid -> (clave "CLASE_SLOT" -> timestamp del ultimo uso en ms)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // cooldowns en segundos por slot, el 0 no se usa
    private static final int[] COOLDOWN_SEGUNDOS = {0, 8, 10, 12, 15, 20, 25, 120};

    public AbilityListener(EldenCraft plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (menuAbierto.isEmpty()) return;
            for (UUID id : menuAbierto) {
                Player player = Bukkit.getPlayer(id);
                if (player != null && player.isOnline()) {
                    mostrarHabilidades(player);
                }
            }
        }, 5L, 10L);
    }

    private final Map<UUID, Integer> previousSlots = new HashMap<>();

    public boolean isMenuOpen(UUID uuid) {
        return menuAbierto.contains(uuid);
    }

    @EventHandler
    public void onPressF(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (menuAbierto.contains(uuid)) {
            menuAbierto.remove(uuid);
            if (previousSlots.containsKey(uuid)) {
                player.getInventory().setHeldItemSlot(previousSlots.remove(uuid));
            }
            player.sendActionBar(ChatColor.RED + "✖ Menú de Habilidades Cerrado");
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.5f);
        } else {
            menuAbierto.add(uuid);
            previousSlots.put(uuid, player.getInventory().getHeldItemSlot());
            player.getInventory().setHeldItemSlot(8);
            mostrarHabilidades(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
        }
    }

    @EventHandler
    public void onWardenBeam(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("warden_form")) return;

        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR ||
            event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            
            if (player.isSneaking()) {
                com.tuusuario.mmo.eldencraft.skills.druid.AvatarPrimigenio.shootBeam(player);
            }
        }
    }

    @EventHandler
    public void onNumberPress(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!menuAbierto.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        int slot = event.getNewSlot() + 1;

        if (slot >= 1 && slot <= 7) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data == null || data.getPlayerClass() == null) return;

            Skill skill = plugin.getSkillManager().getSkill(data.getPlayerClass(), slot);

            if (skill != null) {
                // 1. chequeamos q tenga el nivel necesario
                if (data.getLevel() < skill.getLevelRequired()) {
                    player.sendMessage(ChatColor.RED + "¡Necesitas nivel " + skill.getLevelRequired()
                            + " para usar " + skill.getName() + "!");
                    return;
                }

                // 2. chequeamos cooldown
                String cdKey = data.getPlayerClass().name() + "_" + slot;
                long ahora = System.currentTimeMillis();
                Map<String, Long> misCD = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
                long ultimoUso = misCD.getOrDefault(cdKey, 0L);
                int cdSegundos = (slot < COOLDOWN_SEGUNDOS.length) ? COOLDOWN_SEGUNDOS[slot] : 10;
                if (skill.getCooldown() > 0) {
                    cdSegundos = skill.getCooldown();
                }

                if (ahora - ultimoUso < cdSegundos * 1000L) {
                    long restante = (cdSegundos * 1000L - (ahora - ultimoUso)) / 1000 + 1;
                    player.sendMessage(ChatColor.RED + "⏳ " + skill.getName()
                            + " en recarga: " + restante + "s");
                    return;
                }

                // pasiva del mago: gasta 20% menos de mana
                double cost = skill.getCost();
                if (data.getPlayerClass() == ClassType.MAGE) {
                    cost = cost * 0.8;
                }

                // 3. chequeamos q tenga mana/energia
                //    pasiva del brujo: si no tiene mana usa vida
                if (data.getCurrentResource() < cost) {
                    if (data.getPlayerClass() == ClassType.WARLOCK && cost > 0) {
                        double missingMana = cost - data.getCurrentResource();
                        if (player.getHealth() > missingMana + 2.0) {
                            player.damage(missingMana);
                            player.sendActionBar(ChatColor.DARK_RED + "⚔ Pacto Oscuro: -"
                                    + (int) missingMana + " HP");
                            data.setCurrentResource(0); // gasta todo el mana que le quedaba
                        } else {
                            player.sendMessage(ChatColor.RED + "¡No tienes vida suficiente para el Pacto Oscuro!");
                            return;
                        }
                    } else {
                        String tipo = esMagico(data) ? "Maná" : "Energía";
                        player.sendMessage(ChatColor.RED + "¡No tienes suficiente " + tipo + "!");
                        return;
                    }
                } else {
                    data.setCurrentResource(data.getCurrentResource() - cost);
                }

                // 4. ejecutamos la habilidad
                data.setFatigue(data.getFatigue() + (int)(cost / 2));
                
                int skillCd = skill.getCooldown() > 0 ? skill.getCooldown() : ((slot < COOLDOWN_SEGUNDOS.length) ? COOLDOWN_SEGUNDOS[slot] : 10);
                misCD.put(cdKey, ahora);

                // sobrecarga del hechicero: no se duplica a si misma
                boolean sobrecargar = (data.getPlayerClass() == ClassType.SORCERER)
                        && SorcererState.sobrecargaActiva.contains(player.getUniqueId())
                        && !(skill instanceof Sobrecarga);

                if (sobrecargar) {
                    SorcererState.sobrecargaActiva.remove(player.getUniqueId());
                }

                if (esMagico(data) && skill.getCastType() != Skill.CastType.INSTANT) {
                    plugin.getCastManager().startCasting(player, skill, sobrecargar);
                    menuAbierto.remove(player.getUniqueId());
                    return;
                }

                skill.execute(player, data);

                if (sobrecargar) {
                    skill.execute(player, data);   // se ejecuta 2 veces
                    player.sendActionBar(ChatColor.LIGHT_PURPLE + "✦ ¡Sobrecarga! "
                            + skill.getName() + " x2");
                }

                // reaccion arcana: despues de 5 cargas explota en area
                if (data.getPlayerClass() == ClassType.SORCERER
                        && SorcererState.arcanaReady.remove(player.getUniqueId())) {
                    double arcDmg = 10.0 + data.getIntelligence() * 0.6;
                    for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                        if (e instanceof LivingEntity && !plugin.getPartyManager().isAlly(player, e)) {
                            ((LivingEntity) e).damage(arcDmg, player);
                        }
                    }
                    player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,
                            player.getLocation().add(0, 1, 0), 4, 1.5, 0.5, 1.5, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.8f);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ ¡Reacción Arcana! Onda de área liberada.");
                }

                if (!sobrecargar) {
                    player.sendActionBar(ChatColor.GREEN + "✔ " + skill.getName()
                            + ChatColor.GRAY + " (CD: " + cdSegundos + "s)");
                }
                menuAbierto.remove(player.getUniqueId());

            } else {
                player.sendMessage(ChatColor.GRAY + "Aún no hay una habilidad programada en este espacio.");
            }
        }
    }

    private void mostrarHabilidades(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getPlayerClass() == null) return;

        String clase = data.getPlayerClass().name();
        int nivel = data.getLevel();
        int[] hitos = {1, 3, 6, 9, 12, 16, 20};

        Map<String, Long> misCD = cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
        long ahora = System.currentTimeMillis();

        StringBuilder menu = new StringBuilder(ChatColor.GOLD + "" + ChatColor.BOLD
                + data.getPlayerClass().displayName + " ");

        for (int i = 0; i < hitos.length; i++) {
            int slotNum = i + 1;
            String cdKey = clase + "_" + slotNum;
            int cdSeg = (slotNum < COOLDOWN_SEGUNDOS.length) ? COOLDOWN_SEGUNDOS[slotNum] : 10;
            
            Skill skillObj = plugin.getSkillManager().getSkill(data.getPlayerClass(), slotNum);
            if (skillObj != null && skillObj.getCooldown() > 0) {
                cdSeg = skillObj.getCooldown();
            }
            
            boolean enCD = (ahora - misCD.getOrDefault(cdKey, 0L)) < cdSeg * 1000L;

            if (nivel < hitos[i]) {
                menu.append(ChatColor.DARK_GRAY).append(slotNum).append(" ");
            } else if (enCD) {
                menu.append(ChatColor.RED).append(slotNum).append(" ");
            } else {
                menu.append(ChatColor.GREEN).append(slotNum).append(" ");
            }
        }

        // indicadores de pasivas del hechicero
        if (data.getPlayerClass() == ClassType.SORCERER) {
            int cargas = SorcererState.arcaneCharges.getOrDefault(player.getUniqueId(), 0);
            menu.append(ChatColor.LIGHT_PURPLE).append("| Arcana: ").append(cargas).append("/5 ");
            if (SorcererState.sobrecargaActiva.contains(player.getUniqueId()))
                menu.append(ChatColor.YELLOW).append("[x2] ");
        }

        menu.append(ChatColor.WHITE).append("| [F] Salir");
        player.sendActionBar(menu.toString());
    }

    private boolean esMagico(PlayerData data) {
        String clase = data.getPlayerClass().name();
        return clase.equals("MAGE") || clase.equals("PALADIN") || clase.equals("BARD")
            || clase.equals("SORCERER") || clase.equals("WARLOCK")
            || clase.equals("DRUID")   || clase.equals("CLERIC");
        // el pistolero usa energia, asi q retorna false aca
    }
}
