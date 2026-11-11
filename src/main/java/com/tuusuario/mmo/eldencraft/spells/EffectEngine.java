package com.tuusuario.mmo.eldencraft.spells;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.rules.Attribute;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.rules.DiceRoller;
import com.tuusuario.mmo.eldencraft.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * Motor de resolución y ejecución de efectos tipados de hechizos (SPEC 5.2).
 */
public class EffectEngine {

    private final EldenCraft plugin;
    private final DiceRoller diceRoller = new DiceRoller();

    public EffectEngine(EldenCraft plugin) {
        this.plugin = plugin;
    }

    /**
     * Ejecuta los efectos de un hechizo para un lanzador y objetivo.
     *
     * @param caster    Jugador que lanza el hechizo
     * @param casterData Datos del personaje
     * @param spell     Definición del hechizo
     * @param castLevel Nivel de espacio con el que se lanzó
     * @param target    Objetivo vivo (puede ser null o el propio lanzador)
     */
    public void execute(Player caster, PlayerData casterData, SpellDefinition spell, int castLevel, LivingEntity target) {
        LivingEntity finalTarget = target != null ? target : caster;

        // Calcular bono de ataque y CD de conjuro del lanzador
        int profBonus = AttributeCalculator.proficiencyBonus(casterData.getLevel());
        int castMod = getCastingModifier(casterData);
        int saveDC = AttributeCalculator.spellSaveDC(profBonus, castMod);
        int attackBonus = AttributeCalculator.spellAttackBonus(profBonus, castMod);

        for (EffectDefinition effect : spell.getEfectos()) {
            executeEffect(caster, casterData, spell, effect, castLevel, finalTarget, saveDC, attackBonus);
        }
    }

    private void executeEffect(Player caster, PlayerData casterData, SpellDefinition spell,
                               EffectDefinition effect, int castLevel, LivingEntity target,
                               int saveDC, int attackBonus) {
        String tipo = effect.getTipo();

        switch (tipo) {
            case "daño" -> applyDamage(caster, spell, effect, castLevel, target, saveDC, false);
            case "ataque_conjuro" -> applySpellAttack(caster, spell, effect, castLevel, target, attackBonus);
            case "curacion" -> applyHealing(caster, casterData, spell, effect, castLevel, target);
            case "condicion" -> applyCondition(caster, spell, effect, target);
            case "salvacion" -> applyDamage(caster, spell, effect, castLevel, target, saveDC, true);
            default -> {
                // Utilidad / Especial / Buff
                applyUtility(caster, spell, effect, target);
            }
        }
    }

    private void applyDamage(Player caster, SpellDefinition spell, EffectDefinition effect,
                             int castLevel, LivingEntity target, int saveDC, boolean requiresSave) {
        String diceExpr = resolveDiceExpression(effect, spell.getNivel(), castLevel);
        DiceRoller.RollResult roll = diceRoller.roll(diceExpr);
        int totalDamage = roll.total;

        if (requiresSave && effect.getSalvacion() != null) {
            // Objetivo realiza salvación
            boolean passedSave = simulateSavingThrow(target, effect.getSalvacion(), saveDC);
            if (passedSave) {
                if (effect.isSalvacionMitad()) {
                    totalDamage = Math.max(1, totalDamage / 2);
                    caster.sendMessage(ChatColor.GRAY + target.getName() + " superó la salvación (daño a la mitad: " + totalDamage + ").");
                } else {
                    totalDamage = 0;
                    caster.sendMessage(ChatColor.GRAY + target.getName() + " superó la salvación (sin efecto).");
                }
            }
        }

        if (totalDamage > 0) {
            if (target instanceof Player targetPlayer) {
                PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
                if (targetData != null && plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) {
                    int curHp = targetData.getDndHpCurrent();
                    targetData.setDndHpCurrent(Math.max(0, curHp - totalDamage));
                    MessageUtil.send(targetPlayer, "hp.damaged", Map.of("cantidad", String.valueOf(totalDamage)));
                    if (targetData.getDndHpCurrent() <= 0) {
                        MessageUtil.send(targetPlayer, "hp.zero");
                    }
                } else {
                    targetPlayer.damage(totalDamage);
                }
            } else {
                target.damage(totalDamage, caster);
            }

            caster.sendMessage(ChatColor.RED + "¡Impacto de " + spell.getNombre() + "! "
                    + ChatColor.WHITE + totalDamage + " de daño (" + effect.getTipoDano() + ").");
        }
    }

    private void applySpellAttack(Player caster, SpellDefinition spell, EffectDefinition effect,
                                  int castLevel, LivingEntity target, int attackBonus) {
        DiceRoller.RollResult attackRoll = diceRoller.roll("1d20");
        int totalAttack = attackRoll.total + attackBonus;

        // CA objetivo (default 10 o armadura)
        int targetAC = 10;
        if (target instanceof Player targetPlayer) {
            PlayerData td = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
            if (td != null) {
                targetAC = 10 + AttributeCalculator.modifier(td.getEffectiveDex());
            }
        }

        boolean hit = attackRoll.isCritical || (!attackRoll.isFumble && totalAttack >= targetAC);
        if (hit) {
            String diceExpr = resolveDiceExpression(effect, spell.getNivel(), castLevel);
            if (attackRoll.isCritical) {
                // Crítico: duplica dados
                diceExpr = doubleDice(diceExpr);
                caster.sendMessage(ChatColor.GOLD + "¡CRÍTICO en ataque de conjuro!");
            }
            DiceRoller.RollResult damageRoll = diceRoller.roll(diceExpr);
            int damage = damageRoll.total;

            if (target instanceof Player targetPlayer) {
                PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
                if (targetData != null && plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) {
                    targetData.setDndHpCurrent(Math.max(0, targetData.getDndHpCurrent() - damage));
                    MessageUtil.send(targetPlayer, "hp.damaged", Map.of("cantidad", String.valueOf(damage)));
                } else {
                    targetPlayer.damage(damage);
                }
            } else {
                target.damage(damage, caster);
            }

            caster.sendMessage(ChatColor.GOLD + "Ataque de conjuro (" + attackRoll.total + "+" + attackBonus
                    + "=" + totalAttack + " vs CA " + targetAC + "): IMPACTO por " + damage + " de daño.");
        } else {
            caster.sendMessage(ChatColor.GRAY + "Ataque de conjuro (" + attackRoll.total + "+" + attackBonus
                    + "=" + totalAttack + " vs CA " + targetAC + "): FALLO.");
        }
    }

    private void applyHealing(Player caster, PlayerData casterData, SpellDefinition spell,
                              EffectDefinition effect, int castLevel, LivingEntity target) {
        String diceExpr = resolveDiceExpression(effect, spell.getNivel(), castLevel);
        int castMod = getCastingModifier(casterData);
        DiceRoller.RollResult roll = diceRoller.roll(diceExpr);
        int healed = Math.max(1, roll.total + castMod);

        if (target instanceof Player targetPlayer) {
            PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
            if (targetData != null && plugin.getConfig().getBoolean("hp.dnd-hp-enabled", true)) {
                int maxHp = targetData.getDndHpMax();
                int currentHp = targetData.getDndHpCurrent();
                int finalHp = Math.min(maxHp, currentHp + healed);
                int actualHealed = finalHp - currentHp;
                targetData.setDndHpCurrent(finalHp);
                MessageUtil.send(targetPlayer, "hp.healed", Map.of("cantidad", String.valueOf(actualHealed)));
            } else {
                double max = targetPlayer.getMaxHealth();
                targetPlayer.setHealth(Math.min(max, targetPlayer.getHealth() + healed));
            }
        } else {
            double max = target.getMaxHealth();
            target.setHealth(Math.min(max, target.getHealth() + healed));
        }

        caster.sendMessage(ChatColor.GREEN + "¡Curación de " + spell.getNombre() + "! +" + healed + " PG.");
    }

    private void applyCondition(Player caster, SpellDefinition spell, EffectDefinition effect, LivingEntity target) {
        String cond = effect.getCondicion() != null ? effect.getCondicion().toLowerCase() : "envenenado";
        switch (cond) {
            case "envenenado" -> target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            case "cegado" -> target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
            case "invisibilidad", "invisible" -> target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
            case "sueño", "inconsciente" -> target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
            default -> target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 100, 0));
        }
        caster.sendMessage(ChatColor.AQUA + "Condición aplicada a " + target.getName() + ": " + cond + ".");
    }

    private void applyUtility(Player caster, SpellDefinition spell, EffectDefinition effect, LivingEntity target) {
        String spellId = spell.getId().toLowerCase();
        if (spellId.contains("luz")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
            caster.sendMessage(ChatColor.YELLOW + "Has imbuido con luz a " + target.getName() + ".");
        } else if (spellId.contains("escudo")) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1));
            caster.sendMessage(ChatColor.BLUE + "¡Escudo mágico desplegado!");
        } else {
            caster.sendMessage(ChatColor.LIGHT_PURPLE + "Efecto de " + spell.getNombre() + " activado.");
        }
    }

    private boolean simulateSavingThrow(LivingEntity target, String attrName, int dc) {
        int bonus = 0;
        if (target instanceof Player targetPlayer) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(targetPlayer.getUniqueId());
            if (data != null) {
                Attribute attr = Attribute.fromString(attrName);
                if (attr != null) {
                    bonus = switch (attr) {
                        case STR -> AttributeCalculator.modifier(data.getEffectiveStr());
                        case DEX -> AttributeCalculator.modifier(data.getEffectiveDex());
                        case CON -> AttributeCalculator.modifier(data.getEffectiveCon());
                        case INT -> AttributeCalculator.modifier(data.getEffectiveInt());
                        case WIS -> AttributeCalculator.modifier(data.getEffectiveWis());
                        case CHA -> AttributeCalculator.modifier(data.getEffectiveCha());
                    };
                }
            }
        }
        DiceRoller.RollResult roll = diceRoller.roll("1d20");
        return (roll.total + bonus) >= dc;
    }

    private String resolveDiceExpression(EffectDefinition effect, int baseLevel, int castLevel) {
        String base = effect.getDados() != null ? effect.getDados() : "1d6";
        if (castLevel <= baseLevel || effect.getMejoraNivel() == null) {
            return base;
        }
        // Si hay upcasting (ej: castLevel > baseLevel)
        int extraLevels = castLevel - baseLevel;
        // Si la mejora es simple tipo "+1d6 por nivel"
        return base + "+" + extraLevels + "d6";
    }

    private String doubleDice(String expr) {
        return expr.replaceAll("(\\d+)d(\\d+)", "$1*2d$2"); // simplificado
    }

    private int getCastingModifier(PlayerData data) {
        if (data.getPlayerClass() == null) return 0;
        return switch (data.getPlayerClass()) {
            case MAGE -> AttributeCalculator.modifier(data.getEffectiveInt());
            case CLERIC, DRUID, RANGER -> AttributeCalculator.modifier(data.getEffectiveWis());
            case BARD, SORCERER, WARLOCK, PALADIN -> AttributeCalculator.modifier(data.getEffectiveCha());
            default -> AttributeCalculator.modifier(data.getEffectiveInt());
        };
    }
}
