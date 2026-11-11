package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.managers.PartyManager;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.BalanceState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.entity.EntityTargetEvent;

// aca va todo lo del combate xd
public class CombatListener implements Listener {

    private final EldenCraft plugin;

    public CombatListener(EldenCraft plugin) {
        this.plugin = plugin;
    }

    // cuando el jugador le pega a algo o sus invocaciones
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        // 1) si son de la misma party no se hacen daño entre ellos
        Player victimPlayer = null;
        if (event.getEntity() instanceof Player) {
            victimPlayer = (Player) event.getEntity();
        } else {
            victimPlayer = getSummoner(event.getEntity());
        }

        Player attackerPlayer = getPlayerSource(event);
        if (attackerPlayer == null) {
            attackerPlayer = getSummoner(event.getDamager());
        }

        if (attackerPlayer != null && victimPlayer != null) {
            if (attackerPlayer.equals(victimPlayer)) {
                event.setCancelled(true);
                return;
            }
            PartyManager pm = plugin.getPartyManager();
            PartyManager.Party pa = pm.getParty(attackerPlayer.getUniqueId());
            PartyManager.Party pv = pm.getParty(victimPlayer.getUniqueId());
            if (pa != null && pv != null && pa == pv) {
                event.setCancelled(true);
                attackerPlayer.sendActionBar(ChatColor.GREEN
                        + "✦ Fuego amigo desactivado.");
                return;
            }
        }

        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // 2) daño extra por fuerza
        double damageBase = event.getDamage();
        double extra = damageBase * (data.getStrength() / 10.0);

        // PASIVAS OFENSIVAS DE CADA CLASE
        com.tuusuario.mmo.eldencraft.models.ClassType cType = data.getPlayerClass();
        if (cType != null) {
            // guerrero: mientras menos vida mas daño
            if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.WARRIOR && event.getDamager() instanceof Player) {
                double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double missingPct = 1.0 - (player.getHealth() / maxHp);
                extra += damageBase * (0.25 * Math.max(0, missingPct));
            }
            // paladin: daño doble a no-muertos
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.PALADIN && com.tuusuario.mmo.eldencraft.util.UndeadCheck.isUndead(event.getEntity())) {
                extra += damageBase * 1.0;
            }
            // ladron: critico si ataca por la espalda
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.ROGUE && event.getDamager() instanceof Player) {
                org.bukkit.util.Vector dirV = event.getEntity().getLocation().getDirection();
                org.bukkit.util.Vector dirA = player.getLocation().getDirection();
                if (dirV.dot(dirA) > 0.5) { // esta atacando por atras
                    extra += damageBase * 0.5;
                    BalanceState.rogueCritNext.put(player.getUniqueId(), 2.0); // Forzar crítico visual
                }
            }
            // monje: pasiva del puño
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.MONK && event.getDamager() instanceof Player) {
                org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand != null && hand.getType().name().contains("SWORD")) {
                    event.setDamage(0);
                    player.sendMessage(ChatColor.RED + "Tus votos monásticos te prohíben usar espadas.");
                    return;
                } else if (hand != null && hand.getType().name().contains("AXE")) {
                    damageBase = 1.0;
                    extra = 0;
                } else if (hand == null || hand.getType() == org.bukkit.Material.AIR) {
                    if (event.getDamager() instanceof Player && event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                        // Escala hasta +10 de daño a nivel 20 (1 base + 10 extra = 11 total -> Netherite Filo 5)
                        extra += (data.getLevel() / 20.0) * 10.0;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, true, false));
                    }
                }
            }
            // barbaro: se pone re loco en combate
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.BARBARIAN) {
                BalanceState.barbarianCombat.put(player.getUniqueId(), System.currentTimeMillis());
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 0, true, false));
                extra += damageBase * 0.2;
            }
            // brujo: daño extra con las calaveras de wither
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.WARLOCK && event.getDamager() instanceof org.bukkit.entity.WitherSkull) {
                extra += 8.0 + (data.getIntelligence() * 0.4);
            }
            // hechicero: daño extra con las bolitas arcanas
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.SORCERER && event.getDamager() instanceof org.bukkit.entity.Snowball snowball) {
                if (snowball.hasMetadata("arcane_mote")) {
                    double intel = snowball.getMetadata("arcane_mote").get(0).asDouble();
                    extra += 10.0 + (intel * 0.4);
                }
            }
        }

        // 3) critico del ladron (consume el flag q se puso antes)
        Double critMult = BalanceState.rogueCritNext.remove(player.getUniqueId());
        if (critMult != null && critMult > 1.0) {
            extra += damageBase * (critMult - 1.0);
            player.getWorld().spawnParticle(Particle.CRIT,
                    event.getEntity().getLocation().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.05);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.4f);
            String etiqueta = critMult >= 4.0 ? "§4§l¡DOBLE CRÍTICO!" : "§e§l¡CRÍTICO!";
            player.sendActionBar(etiqueta);
        }

        // 4) si tiene la marca del pistolero hace 30% mas de daño
        if (event.getEntity().hasMetadata("gunslinger_mark")) {
            long expiry = event.getEntity().getMetadata("gunslinger_mark").get(0).asLong();
            if (System.currentTimeMillis() < expiry) {
                extra += damageBase * 0.3; // +30% de daño
                event.getEntity().getWorld().spawnParticle(Particle.ANGRY_VILLAGER, event.getEntity().getLocation().add(0, 1, 0), 5);
            } else {
                event.getEntity().removeMetadata("gunslinger_mark", plugin);
            }
        }

        event.setDamage(damageBase + extra);

        // 4) lifesteal del guerrero en postura de combate, +1 hp por golpe
        if (BalanceState.hasLifesteal(player.getUniqueId())) {
            double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(maxHp, player.getHealth() + 1.0));
            player.getWorld().spawnParticle(Particle.HEART,
                    player.getLocation().add(0, 1.6, 0), 1);
        }
    }

    // daño q recibe el jugador: barbaro berserk x2, guerrero refleja, etc
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamageReceived(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // hechicero: si esta en desfase no le hace daño nada
        if (com.tuusuario.mmo.eldencraft.util.SorcererState.desfaseInmune.containsKey(victim.getUniqueId())) {
            long expiry = com.tuusuario.mmo.eldencraft.util.SorcererState.desfaseInmune.get(victim.getUniqueId());
            if (System.currentTimeMillis() < expiry) {
                event.setCancelled(true);
                return;
            } else {
                com.tuusuario.mmo.eldencraft.util.SorcererState.desfaseInmune.remove(victim.getUniqueId());
            }
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(victim.getUniqueId());
        if (data != null && data.getPlayerClass() != null) {
            com.tuusuario.mmo.eldencraft.models.ClassType cType = data.getPlayerClass();
            // paladin: los no-muertos le hacen 50% menos
            if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.PALADIN && com.tuusuario.mmo.eldencraft.util.UndeadCheck.isUndead(event.getDamager())) {
                event.setDamage(event.getDamage() * 0.5);
            }
            // druida: devuelve 30% del daño con las espinas
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.DRUID) {
                if (com.tuusuario.mmo.eldencraft.util.BalanceState.druidThorns.containsKey(victim.getUniqueId())) {
                    if (System.currentTimeMillis() < com.tuusuario.mmo.eldencraft.util.BalanceState.druidThorns.get(victim.getUniqueId())) {
                        if (event.getDamager() instanceof org.bukkit.entity.LivingEntity) {
                            org.bukkit.entity.LivingEntity attacker = (org.bukkit.entity.LivingEntity) event.getDamager();
                            attacker.damage(event.getDamage() * 0.3, victim);
                            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.1);
                        }
                    } else {
                        com.tuusuario.mmo.eldencraft.util.BalanceState.druidThorns.remove(victim.getUniqueId());
                    }
                }
            }
            // hechicero: acumula cargas arcanas cuando le pegan
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.SORCERER) {
                int cargas = com.tuusuario.mmo.eldencraft.util.SorcererState.arcaneCharges.getOrDefault(victim.getUniqueId(), 0);
                if (cargas < 5) {
                    cargas++;
                    com.tuusuario.mmo.eldencraft.util.SorcererState.arcaneCharges.put(victim.getUniqueId(), cargas);
                    if (cargas == 5) {
                        com.tuusuario.mmo.eldencraft.util.SorcererState.arcanaReady.add(victim.getUniqueId());
                        victim.sendMessage("§d✦ ¡Reacción Arcana lista! Tu próxima habilidad explotará.");
                        victim.playSound(victim.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 2f);
                    }
                }
            }
            // barbaro: recibir daño tambien lo pone en modo combate
            else if (cType == com.tuusuario.mmo.eldencraft.models.ClassType.BARBARIAN) {
                BalanceState.barbarianCombat.put(victim.getUniqueId(), System.currentTimeMillis());
                victim.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 0, true, false));
            }
        }

        // barbaro en berserk recibe el doble de daño
        if (BalanceState.isBerserk(victim.getUniqueId())) {
            event.setDamage(event.getDamage() * 2.0);
        }

        // pistolero: sangrado y bala explosiva
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.hasMetadata("explosive_bullet")) {
                proj.getWorld().createExplosion(proj.getLocation(), 2.0f, false, false, event.getEntity());
            }
            if (proj.getShooter() instanceof Player) {
                Player shooter = (Player) proj.getShooter();
                PlayerData sData = plugin.getPlayerManager().getPlayerData(shooter.getUniqueId());
                if (sData != null && sData.getPlayerClass() == com.tuusuario.mmo.eldencraft.models.ClassType.GUNSLINGER) {
                    if (Math.random() < 0.2) { // 20% de chance de sangrado
                        com.tuusuario.mmo.eldencraft.util.BleedTask.applyBleed(victim, shooter, 5, 2.0, plugin);
                        shooter.sendMessage(ChatColor.DARK_RED + "¡Has causado sangrado!");
                    }
                }
            }
        }

        // guerrero contraataque: refleja 50% del daño al atacante
        if (BalanceState.isParrying(victim.getUniqueId())) {
            LivingEntity reflectTarget = null;

            // caso 1: le pego directo un mob o jugador
            if (event.getDamager() instanceof LivingEntity && event.getDamager() != victim) {
                reflectTarget = (LivingEntity) event.getDamager();
            }
            // caso 2: le tiro un proyectil (flecha, bola de fuego, etc)
            else if (event.getDamager() instanceof Projectile) {
                Projectile proj = (Projectile) event.getDamager();
                ProjectileSource shooter = proj.getShooter();
                if (shooter instanceof LivingEntity && shooter != victim) {
                    reflectTarget = (LivingEntity) shooter;
                }
            }

            if (reflectTarget != null) {
                double reflect = event.getDamage() * 0.5;
                if (reflect > 0.5) {
                    reflectTarget.damage(reflect, victim);
                    reflectTarget.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                            reflectTarget.getLocation().add(0, 1, 0), 3);
                    reflectTarget.getWorld().playSound(reflectTarget.getLocation(),
                            Sound.ITEM_SHIELD_BLOCK, 1f, 1.5f);

                    // robo de vida: se cura el 25% del daño reflejado
                    double heal = reflect * 0.25;
                    double maxHp = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    victim.setHealth(Math.min(maxHp, victim.getHealth() + heal));
                    victim.getWorld().spawnParticle(Particle.HEART,
                            victim.getLocation().add(0, 1.6, 0), 2, 0.3, 0.3, 0.3, 0);
                }
            }
        }
    }

    // totem divino del paladin: si alguien se va a morir y hay un paladin con totem cerca, lo salva
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLethalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        if (victim.getHealth() - event.getFinalDamage() > 0) return;

        for (Player paladin : Bukkit.getOnlinePlayers()) {
            if (!paladin.getWorld().equals(victim.getWorld())) continue;
            if (paladin.getLocation().distance(victim.getLocation()) > 30.0) continue;
            if (!BalanceState.hasActiveTotem(paladin.getUniqueId())) continue;

            event.setCancelled(true);
            double maxHp = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            victim.setHealth(Math.max(1.0, maxHp * 0.5));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
            victim.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    victim.getLocation().add(0, 1, 0), 80, 0.6, 1.2, 0.6, 0.4);
            victim.getWorld().playSound(victim.getLocation(),
                    Sound.ITEM_TOTEM_USE, 1f, 1f);

            BalanceState.paladinTotem.remove(paladin.getUniqueId());
            paladin.sendMessage(ChatColor.GOLD + "✦ Tu §e§lTótem Divino§r§6 ha salvado a "
                    + ChatColor.YELLOW + victim.getName() + ChatColor.GOLD + ".");
            if (!paladin.equals(victim)) {
                victim.sendMessage(ChatColor.GOLD + "✦ ¡El §e§lTótem Divino§r§6 de "
                        + paladin.getName() + " te ha salvado de la muerte!");
            }
            return;
        }
    }

    // modo duelo del pistolero: bloquea la curacion
    @EventHandler
    public void onHeal(org.bukkit.event.entity.EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            Long expiry = BalanceState.gunslingerDuelMode.get(p.getUniqueId());
            if (expiry != null) {
                if (System.currentTimeMillis() < expiry) {
                    event.setCancelled(true);
                } else {
                    BalanceState.gunslingerDuelMode.remove(p.getUniqueId());
                }
            }
        }
    }

    // esto saca el jugador q hizo el daño, sea pq le pego directo
    // o pq lanzo un proyectil o algo asi
    private Player getPlayerSource(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                return (Player) proj.getShooter();
            }
        }
        return null;
    }

    private Player getSummoner(org.bukkit.entity.Entity entity) {
        if (entity instanceof org.bukkit.entity.Tameable tameable) {
            if (tameable.getOwner() instanceof Player p) return p;
        }
        if (entity.hasMetadata("summoner_uuid")) {
            String uuidStr = entity.getMetadata("summoner_uuid").get(0).asString();
            return Bukkit.getPlayer(java.util.UUID.fromString(uuidStr));
        }
        return null;
    }
}
