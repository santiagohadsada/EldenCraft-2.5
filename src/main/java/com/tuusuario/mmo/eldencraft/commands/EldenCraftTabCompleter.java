package com.tuusuario.mmo.eldencraft.commands;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Autocompletado inteligente (Tab Completion) para los comandos de EldenCraft:
 *   /mmo, /eldencraft (/ec), /party, /boss, /rpgarmor, /prueba, /descanso, /lanzar, /ficha
 */
public class EldenCraftTabCompleter implements TabCompleter {

    private final EldenCraft plugin;

    private static final List<String> RACES   = Arrays.stream(RaceType.values())
            .map(r -> r.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

    private static final List<String> CLASSES = Arrays.stream(ClassType.values())
            .map(c -> c.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

    private static final List<String> MMO_SUBS = Arrays.asList("elegir");

    private static final List<String> EC_SUBS = Arrays.asList(
            "reload", "descanso", "setlevel", "givemoney", "resetdata", "spawnboss", "listbosses"
    );

    private static final List<String> PARTY_SUBS = Arrays.asList(
            "create", "invite", "accept", "leave", "kick", "disband", "list"
    );

    private static final List<String> BOSS_SUBS = Arrays.asList("list");

    private static final List<String> RPGARMOR_SUBS = Arrays.asList(
            "give", "reload", "info", "diag"
    );

    private static final List<String> SKILLS = Arrays.stream(SkillDefinition.values())
            .map(s -> s.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());

    private static final List<String> REST_SUBS = Arrays.asList("corto", "largo");

    public EldenCraftTabCompleter(EldenCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        return switch (cmd) {
            case "mmo"        -> tabMmo(args);
            case "eldencraft" -> tabEldencraft(sender, args);
            case "party"      -> tabParty(args);
            case "boss"       -> tabBoss(args);
            case "rpgarmor"   -> tabRpgArmor(args);
            case "prueba"     -> tabPrueba(sender, args);
            case "descanso"   -> tabDescanso(sender, args);
            case "lanzar"     -> tabLanzar(args);
            case "ficha"      -> tabFicha(sender, args);
            case "personaje"  -> tabPersonaje(args);
            case "campana"    -> tabCampana(args);
            case "dm"         -> tabDm(sender, args);
            default           -> List.of();
        };
    }

    private List<String> tabMmo(String[] args) {
        if (args.length == 1) return filter(MMO_SUBS, args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("elegir")) return filter(RACES, args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("elegir")) return filter(CLASSES, args[2]);
        return List.of();
    }

    private List<String> tabEldencraft(CommandSender sender, String[] args) {
        if (!sender.hasPermission("eldencraft.admin")) return List.of();
        if (args.length == 1) return filter(EC_SUBS, args[0]);
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            return switch (sub) {
                case "descanso" -> filter(REST_SUBS, args[1]);
                case "setlevel", "givemoney", "resetdata" -> onlinePlayers(args[1]);
                case "spawnboss" -> bossIds(args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3) {
            if (sub.equals("descanso")) {
                List<String> targets = new ArrayList<>(onlinePlayers(args[2]));
                targets.add("todos");
                return filter(targets, args[2]);
            }
            if (sub.equals("setlevel")) {
                return levelSuggestions(args[2]);
            }
        }
        return List.of();
    }

    private List<String> tabParty(String[] args) {
        if (args.length == 1) return filter(PARTY_SUBS, args[0]);
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && (sub.equals("invite") || sub.equals("kick"))) {
            return onlinePlayers(args[1]);
        }
        return List.of();
    }

    private List<String> tabBoss(String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(BOSS_SUBS);
            opts.addAll(plugin.getBossManager().getBosses().keySet());
            return filter(opts, args[0]);
        }
        return List.of();
    }

    private List<String> tabRpgArmor(String[] args) {
        if (args.length == 1) return filter(RPGARMOR_SUBS, args[0]);
        if (!args[0].equalsIgnoreCase("give")) return List.of();
        return switch (args.length) {
            case 2 -> filter(enumNames(com.tuusuario.mmo.eldencraft.armor.armor.ArmorType.values()), args[1]);
            case 3 -> filter(enumNames(com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot.values()), args[2]);
            case 4 -> filter(enumNames(com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity.values()), args[3]);
            case 5 -> {
                int max = plugin.armorConfig().maxUpgradeLevel();
                List<String> ups = new ArrayList<>();
                for (int i = 0; i <= max; i++) ups.add(String.valueOf(i));
                yield filter(ups, args[4]);
            }
            default -> List.of();
        };
    }

    private List<String> tabPrueba(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(SKILLS);
            if (sender.hasPermission("eldencraft.dm")) {
                opts.addAll(onlinePlayers(args[0]));
            }
            return filter(opts, args[0]);
        }
        if (args.length == 2) {
            return filter(SKILLS, args[1]);
        }
        return List.of();
    }

    private List<String> tabDescanso(CommandSender sender, String[] args) {
        if (args.length == 1) return filter(REST_SUBS, args[0]);
        if (args.length == 2 && sender.hasPermission("eldencraft.dm")) return onlinePlayers(args[1]);
        return List.of();
    }

    private List<String> tabLanzar(String[] args) {
        if (args.length == 1) {
            List<String> spellIds = SpellRegistry.getInstance().getAll().stream()
                    .map(SpellDefinition::getId)
                    .collect(Collectors.toList());
            return filter(spellIds, args[0]);
        }
        if (args.length == 2) {
            List<String> lvls = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
            return filter(lvls, args[1]);
        }
        return List.of();
    }

    private List<String> tabFicha(CommandSender sender, String[] args) {
        if (args.length == 1 && (sender.hasPermission("eldencraft.dm") || sender.hasPermission("eldencraft.admin"))) {
            return onlinePlayers(args[0]);
        }
        return List.of();
    }

    private List<String> tabPersonaje(String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("menu", "cambiar", "crear", "borrar", "recuperar", "duplicar", "exportar", "info"), args[0]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("crear")) {
            return filter(RACES, args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("crear")) {
            return filter(CLASSES, args[3]);
        }
        return List.of();
    }

    private List<String> tabCampana(String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("lista", "info", "unirse", "invitar", "crear", "abrir", "cerrar", "mundo", "rol", "expulsar"), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("invitar") || sub.equals("rol") || sub.equals("expulsar")) return onlinePlayers(args[1]);
            if (sub.equals("mundo")) return filter(Arrays.asList("cargar", "descargar", "tp"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("rol")) {
            return filter(Arrays.asList("DM_PRINCIPAL", "CO_DM", "JUGADOR", "ESPECTADOR"), args[2]);
        }
        return List.of();
    }

    private List<String> tabDm(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("fichas", "ficha", "aprobar", "xp", "oro", "sesion", "nota"), args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2) {
            if (sub.equals("ficha") || sub.equals("aprobar")) return onlinePlayers(args[1]);
            if (sub.equals("xp") || sub.equals("oro")) {
                List<String> list = new ArrayList<>(onlinePlayers(args[1]));
                list.add("all");
                return filter(list, args[1]);
            }
            if (sub.equals("sesion")) return filter(Arrays.asList("iniciar", "terminar"), args[1]);
            if (sub.equals("nota")) return filter(Arrays.asList("agregar", "listar"), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }

    private List<String> onlinePlayers(String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }

    private List<String> bossIds(String typed) {
        return filter(new ArrayList<>(plugin.getBossManager().getBosses().keySet()), typed);
    }

    private List<String> levelSuggestions(String typed) {
        List<String> lvls = new ArrayList<>();
        for (int i = 1; i <= 20; i++) lvls.add(String.valueOf(i));
        return filter(lvls, typed);
    }

    private <E extends Enum<E>> List<String> enumNames(E[] values) {
        List<String> names = new ArrayList<>(values.length);
        for (E v : values) names.add(v.name().toLowerCase(Locale.ROOT));
        return names;
    }
}
