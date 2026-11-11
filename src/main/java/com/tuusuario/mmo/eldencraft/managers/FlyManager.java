package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlyManager {

    private final EldenCraft plugin;
    private final Map<UUID, FlyState> flyStates = new ConcurrentHashMap<>();

    public static class FlyState {
        public boolean active = false;
        public int ticksFlying = 0;
        public int cooldownTicks = 0;
        public int cancelFallTicks = 0;
        public BossBar bossBar = null;
    }

    public FlyManager(EldenCraft plugin) {
        this.plugin = plugin;
        // Tarea que corre cada tick para actualizar vuelo y cooldowns
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 1L);
    }

    public boolean isFlying(Player player) {
        FlyState state = flyStates.get(player.getUniqueId());
        return state != null && state.active;
    }

    public int getCooldown(Player player) {
        FlyState state = flyStates.get(player.getUniqueId());
        return state != null ? state.cooldownTicks : 0;
    }

    public FlyState getFlyState(UUID uuid) {
        return flyStates.get(uuid);
    }

    public void clearPlayer(UUID uuid) {
        FlyState state = flyStates.remove(uuid);
        if (state != null) {
            if (state.bossBar != null) {
                state.bossBar.removeAll();
                state.bossBar = null;
            }
            if (state.active) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    deactivateFlight(player, false);
                }
            }
        }
    }

    public void clearAll() {
        for (UUID uuid : flyStates.keySet()) {
            clearPlayer(uuid);
        }
        flyStates.clear();
    }

    public void sendFakeElytra(Player player, boolean forceReal) {
        org.bukkit.inventory.ItemStack item;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        RaceType race = (data != null) ? data.getRace() : null;
        FlyState state = getFlyState(player.getUniqueId());
        boolean isFlying = state != null && state.active;

        if (!forceReal && isFlying && (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA)) {
            item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.ELYTRA);
        } else {
            item = player.getInventory().getChestplate();
            if (item == null) {
                item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR);
            }
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendEquipmentChange(player, org.bukkit.inventory.EquipmentSlot.CHEST, item);
        }
    }

    public void syncFakeEquipmentForJoined(Player joinedPlayer) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            FlyState state = getFlyState(online.getUniqueId());
            if (state != null && state.active) {
                PlayerData data = plugin.getPlayerManager().getPlayerData(online.getUniqueId());
                if (data != null && data.getRace() != null) {
                    RaceType race = data.getRace();
                    if (race == RaceType.AASIMAR || race == RaceType.AARAKOCRA) {
                        joinedPlayer.sendEquipmentChange(online, org.bukkit.inventory.EquipmentSlot.CHEST, new org.bukkit.inventory.ItemStack(org.bukkit.Material.ELYTRA));
                    }
                }
            }
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Ignoramos a administradores o jugadores en creativo/espectador
            if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                continue;
            }

            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data == null || data.getRace() == null) continue;

            RaceType race = data.getRace();
            if (race != RaceType.AASIMAR && race != RaceType.AARAKOCRA) continue;

            UUID uuid = player.getUniqueId();
            FlyState state = flyStates.computeIfAbsent(uuid, k -> new FlyState());

            // Reducimos el contador de daño por caída
            if (state.cancelFallTicks > 0) {
                state.cancelFallTicks--;
            }

            if (state.active) {
                state.ticksFlying++;

                // Velocidad actual del jugador
                double speed = player.getVelocity().length();
                double speedMultiplier = 1.0;
                
                // Si va más rápido de lo normal (por cohetes o pociones), aumentamos el multiplicador agresivamente
                if (speed > 0.6) {
                    speedMultiplier = 1.0 + ((speed - 0.6) * 12.0);
                }

                // Consumo de recurso
                double maxRes = data.getMaxResource();
                double currentRes = data.getCurrentResource();
                double baseCost = (race == RaceType.AASIMAR) ? (3.0 / 20.0) : (1.5 / 20.0);
                double cost = baseCost * speedMultiplier;

                if (currentRes < cost) {
                    deactivateFlight(player, true);
                    player.sendMessage(ChatColor.RED + "Te has quedado sin recursos para seguir volando por usar demasiada velocidad.");
                    continue;
                }

                data.setCurrentResource(currentRes - cost);

                if (race == RaceType.AASIMAR && speedMultiplier > 1.0) {
                    int extraTicks = (int) Math.round(speedMultiplier - 1.0);
                    state.ticksFlying += extraTicks;
                }

                // Comportamientos específicos de vuelo
                if (state.bossBar == null) {
                    state.bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
                    state.bossBar.addPlayer(player);
                }

                if (race == RaceType.AASIMAR) {
                    // Límite de burst de 2 segundos (40 ticks)
                    if (state.ticksFlying >= 40) {
                        deactivateFlight(player, true);
                        player.sendMessage(ChatColor.YELLOW + "Tu vuelo celestial ha terminado.");
                        continue;
                    }

                    // Forzamos el modo de vuelo vanilla para Aasimar
                    if (!player.getAllowFlight()) player.setAllowFlight(true);
                    if (!player.isFlying()) player.setFlying(true);
                    if (player.isGliding()) player.setGliding(false);

                    // Si toca el suelo, finaliza el vuelo
                    if (state.ticksFlying > 10 && player.isOnGround()) {
                        deactivateFlight(player, false);
                        continue;
                    }

                    double secsLeft = (40.0 - state.ticksFlying) / 20.0;
                    double progress = Math.max(0.0, 1.0 - ((double)state.ticksFlying / 40.0));
                    state.bossBar.setTitle(ChatColor.GOLD + "✦ Vuelo Aasimar " + ChatColor.GRAY + String.format("(%.1fs)", secsLeft));
                    state.bossBar.setColor(BarColor.YELLOW);
                    state.bossBar.setProgress(progress);

                } else {
                    // Aarakocra: planeo continuo
                    if (!player.isGliding()) player.setGliding(true);
                    // Habilitamos allowFlight durante planeo para que funcione la desactivación por doble salto
                    if (!player.getAllowFlight()) player.setAllowFlight(true);
                    player.setFlying(false);

                    // Aleteo periódico para ganar altura (cada 1.5s / 30 ticks)
                    if (state.ticksFlying % 30 == 0) {
                        Vector dir = player.getLocation().getDirection().normalize().multiply(0.4);
                        dir.setY(0.35); // Impulso ascendente
                        player.setVelocity(dir);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.2f);
                    }

                    // Si toca el suelo, finaliza el planeo
                    if (state.ticksFlying > 10 && player.isOnGround()) {
                        deactivateFlight(player, false);
                        continue;
                    }

                    double progress = Math.max(0.0, currentRes / maxRes);
                    state.bossBar.setTitle(ChatColor.DARK_AQUA + "☁ Planeo Aarakocra " + ChatColor.GRAY + String.format("(%d/%d E)", (int)currentRes, (int)maxRes));
                    state.bossBar.setColor(BarColor.BLUE);
                    state.bossBar.setProgress(progress);
                }

            } else {
                // Gestión de Cooldowns
                if (state.cooldownTicks > 0) {
                    state.cooldownTicks--;
                    if (state.cooldownTicks == 0) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                        if (state.bossBar != null) {
                            state.bossBar.setTitle(ChatColor.GREEN + "✦ ¡Vuelo racial listo! ✦");
                            state.bossBar.setColor(BarColor.GREEN);
                            state.bossBar.setProgress(1.0);
                            
                            // Remover la bossbar después de 2 segundos si no la vuelve a usar
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                FlyState current = flyStates.get(player.getUniqueId());
                                if (current != null && current.bossBar != null && !current.active && current.cooldownTicks == 0) {
                                    current.bossBar.removeAll();
                                    current.bossBar = null;
                                }
                            }, 40L);
                        }
                    } else {
                        if (state.bossBar == null) {
                            state.bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
                            state.bossBar.addPlayer(player);
                        }
                        int maxCd = (race == RaceType.AASIMAR) ? 240 : 80;
                        double secsLeft = state.cooldownTicks / 20.0;
                        double progress = Math.max(0.0, (double)state.cooldownTicks / maxCd);
                        state.bossBar.setTitle(ChatColor.RED + "⏳ Cooldown Vuelo " + ChatColor.GRAY + String.format("(%.1fs)", secsLeft));
                        state.bossBar.setColor(BarColor.RED);
                        state.bossBar.setProgress(progress);
                    }
                }

                // Habilitamos allowFlight en tierra para que puedan iniciar el doble salto
                if (player.isOnGround() && state.cooldownTicks == 0 && data.getCurrentResource() >= data.getMaxResource() * 0.20) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                    }
                } else if (player.isOnGround()) {
                    if (player.getAllowFlight() && !player.isFlying()) {
                        player.setAllowFlight(false);
                    }
                }

                state.ticksFlying++;
            }

            // Sincronizar visualmente la fake elytra periódicamente para evitar fallos del cliente (tanto volando como parado)
            if (state.ticksFlying % 20 == 0) {
                sendFakeElytra(player, false);
            }
        }
    }

    public void tryActivateFlight(Player player, PlayerData data) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        RaceType race = data.getRace();
        if (race != RaceType.AASIMAR && race != RaceType.AARAKOCRA) return;

        FlyState state = flyStates.computeIfAbsent(player.getUniqueId(), k -> new FlyState());
        if (state.active) return;

        if (state.cooldownTicks > 0) {
            player.sendMessage(ChatColor.RED + "El vuelo racial está en enfriamiento.");
            return;
        }

        double minResource = data.getMaxResource() * 0.20;
        if (data.getCurrentResource() < minResource) {
            player.sendMessage(ChatColor.RED + "No tienes suficiente energía o maná para alzar el vuelo.");
            return;
        }

        if (player.isOnGround()) {
            return;
        }

        // Activación
        state.active = true;
        state.ticksFlying = 0;
        sendFakeElytra(player, false);

        if (race == RaceType.AASIMAR) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setFlySpeed(0.05f);
            player.setGliding(false);
            player.setVelocity(player.getVelocity().add(new Vector(0, 0.5, 0)));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        } else {
            // Aarakocra
            player.setAllowFlight(true); // Allow double jump to cancel flight
            player.setFlying(false);
            player.setGliding(true);
            Vector dir = player.getLocation().getDirection().normalize().multiply(0.4);
            dir.setY(0.3);
            player.setVelocity(dir);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.0f);
        }
    }

    public void deactivateFlight(Player player, boolean startCooldown) {
        FlyState state = flyStates.get(player.getUniqueId());
        if (state == null || !state.active) return;

        state.active = false;
        sendFakeElytra(player, false);
        
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        RaceType race = (data != null) ? data.getRace() : null;

        if (race == RaceType.AASIMAR) {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setFlySpeed(0.1f);
            if (startCooldown) {
                state.cooldownTicks = 240; // 12 segundos (240 ticks)
            }
        } else {
            player.setGliding(false);
            player.setAllowFlight(false);
            if (startCooldown) {
                state.cooldownTicks = 80; // 4 segundos (80 ticks)
            }
        }

        // Siempre otorgamos inmunidad de daño por caída tras volar
        state.cancelFallTicks = 100; // 5 segundos (100 ticks)

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_ELYTRA, 0.8f, 0.8f);
    }


}
