package com.tuusuario.mmo.eldencraft.character.gui;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.character.CharacterProfile;
import com.tuusuario.mmo.eldencraft.character.LevelUpManager;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.RaceType;
import com.tuusuario.mmo.eldencraft.rules.AttributeCalculator;
import com.tuusuario.mmo.eldencraft.rules.SkillDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellDefinition;
import com.tuusuario.mmo.eldencraft.spells.SpellRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.security.SecureRandom;
import java.util.*;

/**
 * Asistente paso a paso para la creación completa de un personaje D&D 5e (SPEC 15.3 / C2).
 */
public class CharacterCreationWizard {

    private final EldenCraft plugin;
    private final Player player;
    private final int targetSlot;
    private final SecureRandom random = new SecureRandom();

    private RaceType selectedRace;
    private ClassType selectedClass;
    private String selectedSubclass = "Básica";
    private String selectedBackground = "Aventurero";
    private int[] selectedStats;
    private final Set<SkillDefinition> selectedProficiencies = new HashSet<>();
    private String selectedLanguage = "Común";
    private final List<String> selectedSpells = new ArrayList<>();
    private String startingEquipment = "PACK"; // PACK u ORO
    private String selectedAlignment = "Neutral";

    public CharacterCreationWizard(EldenCraft plugin, Player player, int targetSlot) {
        this.plugin = plugin;
        this.player = player;
        this.targetSlot = targetSlot;
    }

    public void start() {
        if (plugin.getWizardListener() != null) {
            plugin.getWizardListener().registerWizard(player.getUniqueId(), this);
        }
        openRaceSelection();
    }

    // ── Paso 1: Selección de Raza ─────────────────────────────────────────────

    public void openRaceSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(1), 18, ChatColor.DARK_GRAY + "Paso 1/10: " + ChatColor.BLUE + "Elige tu Raza");

        RaceType[] races = RaceType.values();
        Material[] icons = {
                Material.PLAYER_HEAD, Material.BOW, Material.IRON_PICKAXE, Material.FEATHER,
                Material.DRAGON_BREATH, Material.REDSTONE, Material.EXPERIENCE_BOTTLE,
                Material.IRON_AXE, Material.BLAZE_POWDER, Material.RABBIT_FOOT,
                Material.LEATHER_BOOTS, Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_SWORD
        };

        for (int i = 0; i < races.length && i < 18; i++) {
            RaceType r = races[i];
            Material mat = i < icons.length ? icons[i] : Material.PAPER;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + r.displayName);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Bonos: FUE +" + r.str + " DES +" + r.dex + " CON +" + r.con + " INT +" + r.intel,
                        ChatColor.YELLOW + "Click para seleccionar esta raza."
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    // ── Paso 2: Selección de Clase ────────────────────────────────────────────

    public void openClassSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(2), 18, ChatColor.DARK_GRAY + "Paso 2/10: " + ChatColor.GREEN + "Elige tu Clase");

        ClassType[] classes = ClassType.values();
        Material[] icons = {
                Material.IRON_SWORD, Material.BLAZE_ROD, Material.BOW, Material.GOLDEN_CHESTPLATE,
                Material.SHEARS, Material.NOTE_BLOCK, Material.NETHERITE_AXE, Material.POTION,
                Material.WITHER_SKELETON_SKULL, Material.STICK, Material.OAK_SAPLING, Material.GOLDEN_APPLE, Material.CROSSBOW
        };

        for (int i = 0; i < classes.length && i < 18; i++) {
            ClassType c = classes[i];
            Material mat = i < icons.length ? icons[i] : Material.BOOK;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + c.displayName);
                String tipo = c.isCaster() ? "Lanzador de Conjuros" : "Marcial";
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Tipo: " + ChatColor.YELLOW + tipo,
                        ChatColor.GRAY + "Dado de Golpe: d" + c.hitDie + " | Recurso: " + (c.isCaster() ? "Espacios D&D" : c.resourceType),
                        ChatColor.YELLOW + "Click para seleccionar esta clase."
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    // ── Paso 3: Selección de Subclase ─────────────────────────────────────────

    public void openSubclassSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(3), 9, ChatColor.DARK_GRAY + "Paso 3/10: " + ChatColor.DARK_AQUA + "Subclase (" + selectedClass.displayName + ")");
        List<String> subs = getSubclassesFor(selectedClass);

        for (int i = 0; i < subs.size() && i < 7; i++) {
            String sc = subs.get(i);
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + sc);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Especialización para " + selectedClass.displayName,
                        ChatColor.YELLOW + "Click para elegir esta subclase."
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i + 1, item);
        }

        player.openInventory(inv);
    }

    private List<String> getSubclassesFor(ClassType cls) {
        if (cls == null) return List.of("Básica");
        return switch (cls) {
            case WARRIOR -> List.of("Campeón", "Maestro de Batalla", "Caballero Arcano");
            case MAGE -> List.of("Escuela de Evocación", "Escuela de Abjuración", "Escuela de Nigromancia");
            case RANGER -> List.of("Cazador", "Señor de las Bestias", "Acechador Penumbroso");
            case PALADIN -> List.of("Juramento de Devoción", "Juramento de Venganza", "Juramento de los Antiguos");
            case ROGUE -> List.of("Ladrón", "Asesino", "Embaucador Arcano");
            case BARD -> List.of("Colegio del Saber", "Colegio del Valor", "Colegio de las Espadas");
            case BARBARIAN -> List.of("Senda del Berserker", "Senda del Tótem", "Senda del Celo");
            case SORCERER -> List.of("Linaje Dracónico", "Magia Salvaje", "Tormenta Arcana");
            case WARLOCK -> List.of("Patrón Infernal", "Gran Antiguo", "Señor Feérico");
            case MONK -> List.of("Camino de la Mano Abierta", "Camino de las Sombras", "Camino de los 4 Elementos");
            case DRUID -> List.of("Círculo de la Tierra", "Círculo de la Luna", "Círculo del Pastor");
            case CLERIC -> List.of("Dominio de la Vida", "Dominio de la Guerra", "Dominio de la Luz");
            case GUNSLINGER -> List.of("Francotirador", "Pistolero Rápido", "Artillero Táctico");
        };
    }

    // ── Paso 4: Selección de Trasfondo ────────────────────────────────────────

    public void openBackgroundSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(4), 9, ChatColor.DARK_GRAY + "Paso 4/10: " + ChatColor.LIGHT_PURPLE + "Elige tu Trasfondo");
        String[] bgs = {"Aventurero", "Acólito", "Criminal", "Noble", "Sabio", "Soldado"};
        Material[] icons = {Material.COMPASS, Material.GOLDEN_APPLE, Material.IRON_BARS, Material.GOLD_INGOT, Material.BOOK, Material.SHIELD};

        for (int i = 0; i < bgs.length; i++) {
            ItemStack item = new ItemStack(icons[i]);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + bgs[i]);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Historia previa y motivaciones del personaje.",
                        ChatColor.YELLOW + "Click para seleccionar."
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i + 1, item);
        }

        player.openInventory(inv);
    }

    // ── Paso 5: Asignación de Atributos (Método fijado por campaña) ───────────

    public void openAttributeSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(5), 9, ChatColor.DARK_GRAY + "Paso 5/10: " + ChatColor.RED + "Generación de Atributos");

        // Opción 1: Matriz Estándar Recomendada (Standard Array)
        int[] classOpt = LevelUpManager.getStandardArrayForClass(selectedClass);
        ItemStack item1 = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta m1 = item1.getItemMeta();
        if (m1 != null) {
            m1.setDisplayName(ChatColor.GOLD + "Matriz Estándar (15, 14, 13, 12, 10, 8)");
            m1.setLore(Arrays.asList(
                    ChatColor.GRAY + "FUE: " + classOpt[0] + " | DES: " + classOpt[1] + " | CON: " + classOpt[2],
                    ChatColor.GRAY + "INT: " + classOpt[3] + " | SAB: " + classOpt[4] + " | CAR: " + classOpt[5],
                    "",
                    ChatColor.YELLOW + "Click para aplicar esta distribución equilibrada."
            ));
            item1.setItemMeta(m1);
        }
        inv.setItem(1, item1);

        // Opción 2: Tirada 4d6 quitando el menor
        ItemStack item2 = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta m2 = item2.getItemMeta();
        if (m2 != null) {
            m2.setDisplayName(ChatColor.AQUA + "Tirada 4d6 (descartando el menor)");
            m2.setLore(Arrays.asList(
                    ChatColor.GRAY + "Tira 4 dados de 6 caras para cada atributo,",
                    ChatColor.GRAY + "descartando el dado con el valor más bajo.",
                    "",
                    ChatColor.YELLOW + "Click para generar tiradas aleatorias D&D."
            ));
            item2.setItemMeta(m2);
        }
        inv.setItem(4, item2);

        // Opción 3: Compra de 27 Puntos (Point Buy)
        ItemStack item3 = new ItemStack(Material.EMERALD);
        ItemMeta m3 = item3.getItemMeta();
        if (m3 != null) {
            m3.setDisplayName(ChatColor.GREEN + "Compra de 27 Puntos (Point Buy)");
            m3.setLore(Arrays.asList(
                    ChatColor.GRAY + "Distribución de 27 puntos según reglas SRD:",
                    ChatColor.GRAY + "Especializado: 15, 14, 13, 12, 10, 8",
                    ChatColor.GRAY + "Equilibrado: 14, 14, 13, 12, 10, 10",
                    "",
                    ChatColor.YELLOW + "Click para aplicar compra de puntos optimizada."
            ));
            item3.setItemMeta(m3);
        }
        inv.setItem(7, item3);

        player.openInventory(inv);
    }

    private int roll4d6DropLowest() {
        int[] rolls = new int[4];
        for (int i = 0; i < 4; i++) {
            rolls[i] = random.nextInt(6) + 1;
        }
        Arrays.sort(rolls);
        return rolls[1] + rolls[2] + rolls[3];
    }

    // ── Paso 6: Selección de Competencias con Contador ────────────────────────

    public void openSkillsSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(6), 27, ChatColor.DARK_GRAY + "Paso 6/10: " + ChatColor.BLUE + "Elige 2 Competencias");

        SkillDefinition[] allSkills = SkillDefinition.values();
        for (int i = 0; i < allSkills.length && i < 18; i++) {
            SkillDefinition skill = allSkills[i];
            boolean selected = selectedProficiencies.contains(skill);
            ItemStack item = new ItemStack(selected ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((selected ? ChatColor.GREEN : ChatColor.WHITE) + skill.nombre);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Atributo: " + skill.attribute.name(),
                        selected ? ChatColor.GREEN + "✔ SELECCIONADA" : ChatColor.DARK_GRAY + "Click para alternar"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        // Info contador
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta mInfo = info.getItemMeta();
        if (mInfo != null) {
            mInfo.setDisplayName(ChatColor.GOLD + "Habilidades Seleccionadas: " + selectedProficiencies.size() + "/2");
            mInfo.setLore(List.of(ChatColor.GRAY + "Elige al menos 2 competencias de habilidad."));
            info.setItemMeta(mInfo);
        }
        inv.setItem(22, info);

        // Botón Continuar
        ItemStack next = new ItemStack(selectedProficiencies.size() >= 2 ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta mNext = next.getItemMeta();
        if (mNext != null) {
            mNext.setDisplayName(selectedProficiencies.size() >= 2 ? ChatColor.GREEN + "Continuar a Idiomas »" : ChatColor.RED + "Elige al menos 2 habilidades");
            next.setItemMeta(mNext);
        }
        inv.setItem(26, next);

        player.openInventory(inv);
    }

    // ── Paso 7: Idiomas ───────────────────────────────────────────────────────

    public void openLanguagesSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(7), 9, ChatColor.DARK_GRAY + "Paso 7/10: " + ChatColor.AQUA + "Elige un Idioma");
        String[] langs = {"Élfico", "Enano", "Dracónico", "Orco", "Mediano", "Celestial", "Infernal"};

        for (int i = 0; i < langs.length; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + langs[i]);
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Idioma adicional (Común ya incluido).",
                        ChatColor.YELLOW + "Click para seleccionar."
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(i + 1, item);
        }

        player.openInventory(inv);
    }

    // ── Paso 8: Hechizos y Trucos Iniciales (si es lanzador) ─────────────────

    public void openSpellsSelection() {
        if (!selectedClass.isCaster()) {
            openEquipmentSelection();
            return;
        }

        Inventory inv = Bukkit.createInventory(new WizardHolder(8), 27, ChatColor.DARK_GRAY + "Paso 8/10: " + ChatColor.LIGHT_PURPLE + "Hechizos Iniciales");
        List<SpellDefinition> spells = SpellRegistry.getInstance().getForClass(selectedClass);

        int slot = 0;
        for (SpellDefinition sp : spells) {
            if (slot >= 18) break;
            boolean chosen = selectedSpells.contains(sp.getId());
            ItemStack item = new ItemStack(chosen ? Material.ENCHANTED_BOOK : Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String tag = sp.getNivel() == 0 ? "Truco" : ("Nivel " + sp.getNivel());
                meta.setDisplayName((chosen ? ChatColor.GREEN : ChatColor.GOLD) + sp.getNombre() + " (" + tag + ")");
                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Escuela: " + sp.getEscuela(),
                        chosen ? ChatColor.GREEN + "✔ SELECCIONADO" : ChatColor.YELLOW + "Click para alternar"
                ));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        ItemStack next = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta mNext = next.getItemMeta();
        if (mNext != null) {
            mNext.setDisplayName(ChatColor.GREEN + "Continuar a Equipo Inicial »");
            next.setItemMeta(mNext);
        }
        inv.setItem(26, next);

        player.openInventory(inv);
    }

    // ── Paso 9: Equipo Inicial (Paquete u Oro) ────────────────────────────────

    public void openEquipmentSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(9), 9, ChatColor.DARK_GRAY + "Paso 9/10: " + ChatColor.GOLD + "Equipo Inicial");

        // Paquete de aventurero
        ItemStack pack = new ItemStack(Material.CHEST);
        ItemMeta mPack = pack.getItemMeta();
        if (mPack != null) {
            mPack.setDisplayName(ChatColor.GREEN + "Paquete de Aventurero");
            mPack.setLore(Arrays.asList(
                    ChatColor.GRAY + "Incluye arma inicial para tu clase,",
                    ChatColor.GRAY + "armadura básica, antorchas y raciones.",
                    "",
                    ChatColor.YELLOW + "Click para equiparte con el paquete."
            ));
            pack.setItemMeta(mPack);
        }
        inv.setItem(2, pack);

        // Oro inicial
        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta mGold = gold.getItemMeta();
        if (mGold != null) {
            mGold.setDisplayName(ChatColor.GOLD + "Oro Inicial (+100 PO)");
            mGold.setLore(Arrays.asList(
                    ChatColor.GRAY + "Renuncia al paquete a cambio de 100",
                    ChatColor.GRAY + "piezas de oro extra para comprar en la tienda.",
                    "",
                    ChatColor.YELLOW + "Click para elegir el dinero inicial."
            ));
            gold.setItemMeta(mGold);
        }
        inv.setItem(6, gold);

        player.openInventory(inv);
    }

    // ── Paso 10: Alineamiento ─────────────────────────────────────────────────

    public void openAlignmentSelection() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(10), 9, ChatColor.DARK_GRAY + "Paso 10/10: " + ChatColor.YELLOW + "Alineamiento");
        String[] alignments = {"Legal Bueno", "Neutral Bueno", "Caótico Bueno", "Legal Neutral", "Neutral", "Caótico Neutral"};

        for (int i = 0; i < alignments.length; i++) {
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + alignments[i]);
                meta.setLore(List.of(ChatColor.YELLOW + "Click para elegir y ver el resumen."));
                item.setItemMeta(meta);
            }
            inv.setItem(i + 1, item);
        }

        player.openInventory(inv);
    }

    // ── Paso 11: Pantalla de Resumen con Confirmar / Volver ───────────────────

    public void openSummaryScreen() {
        Inventory inv = Bukkit.createInventory(new WizardHolder(11), 27, ChatColor.DARK_GRAY + "✦ " + ChatColor.DARK_GREEN + "Resumen del Personaje");

        // Ficha General
        ItemStack general = new ItemStack(Material.BOOK);
        ItemMeta mg = general.getItemMeta();
        if (mg != null) {
            mg.setDisplayName(ChatColor.GOLD + "Identidad y Origen");
            mg.setLore(Arrays.asList(
                    ChatColor.WHITE + "Raza: " + ChatColor.GREEN + (selectedRace != null ? selectedRace.displayName : "N/A"),
                    ChatColor.WHITE + "Clase: " + ChatColor.AQUA + (selectedClass != null ? selectedClass.displayName : "N/A"),
                    ChatColor.WHITE + "Subclase: " + ChatColor.YELLOW + selectedSubclass,
                    ChatColor.WHITE + "Trasfondo: " + ChatColor.LIGHT_PURPLE + selectedBackground,
                    ChatColor.WHITE + "Alineamiento: " + ChatColor.GRAY + selectedAlignment
            ));
            general.setItemMeta(mg);
        }
        inv.setItem(10, general);

        // Atributos D&D
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta ms = statsItem.getItemMeta();
        if (ms != null) {
            ms.setDisplayName(ChatColor.GOLD + "Atributos D&D 5e");
            int[] st = selectedStats != null ? selectedStats : new int[]{10, 10, 10, 10, 10, 10};
            ms.setLore(Arrays.asList(
                    ChatColor.GRAY + "FUE: " + st[0] + " | DES: " + st[1] + " | CON: " + st[2],
                    ChatColor.GRAY + "INT: " + st[3] + " | SAB: " + st[4] + " | CAR: " + st[5]
            ));
            statsItem.setItemMeta(ms);
        }
        inv.setItem(12, statsItem);

        // Habilidades e Idiomas
        ItemStack skillsItem = new ItemStack(Material.FEATHER);
        ItemMeta msk = skillsItem.getItemMeta();
        if (msk != null) {
            msk.setDisplayName(ChatColor.GOLD + "Competencias e Idiomas");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Idiomas: Común, " + selectedLanguage);
            lore.add(ChatColor.GRAY + "Competencias:");
            for (SkillDefinition sd : selectedProficiencies) {
                lore.add(ChatColor.YELLOW + " • " + sd.nombre);
            }
            msk.setLore(lore);
            skillsItem.setItemMeta(msk);
        }
        inv.setItem(14, skillsItem);

        // Equipo y Magia
        ItemStack eqItem = new ItemStack(Material.CHEST);
        ItemMeta me = eqItem.getItemMeta();
        if (me != null) {
            me.setDisplayName(ChatColor.GOLD + "Equipo y Recursos");
            me.setLore(Arrays.asList(
                    ChatColor.WHITE + "Equipo Inicial: " + (startingEquipment.equals("GOLD") ? "100 Piezas de Oro" : "Paquete de Aventurero"),
                    ChatColor.WHITE + "Modo de Recursos: " + (selectedClass.isCaster() ? "Espacios de Conjuro (SLOTS)" : selectedClass.resourceType),
                    ChatColor.WHITE + "Hechizos Elegidos: " + selectedSpells.size()
            ));
            eqItem.setItemMeta(me);
        }
        inv.setItem(16, eqItem);

        // Botón Confirmar
        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta mc = confirm.getItemMeta();
        if (mc != null) {
            mc.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "✔ CONFIRMAR Y NOMBRAR");
            mc.setLore(List.of(ChatColor.GRAY + "Escribe el nombre de tu personaje para finalizar."));
            confirm.setItemMeta(mc);
        }
        inv.setItem(21, confirm);

        // Botón Volver
        ItemStack back = new ItemStack(Material.RED_WOOL);
        ItemMeta mb = back.getItemMeta();
        if (mb != null) {
            mb.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "« VOLVER A MODIFICAR");
            mb.setLore(List.of(ChatColor.GRAY + "Reinicia el asistente para cambiar opciones."));
            back.setItemMeta(mb);
        }
        inv.setItem(23, back);

        player.openInventory(inv);
    }

    // ── Prompt de Nombre y Finalización ──────────────────────────────────────

    public void promptName() {
        player.closeInventory();
        player.sendMessage(ChatColor.GOLD + "================================================");
        player.sendMessage(ChatColor.YELLOW + "✦ ASISTENTE DE CREACIÓN DE PERSONAJE ✦");
        player.sendMessage(ChatColor.WHITE + "Raza: " + ChatColor.GREEN + selectedRace.displayName
                + ChatColor.WHITE + " | Clase: " + ChatColor.AQUA + selectedClass.displayName + " (" + selectedSubclass + ")");
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "Por favor, escribe el nombre de tu personaje en el chat:");
        player.sendMessage(ChatColor.GRAY + "(Entre 3 y 16 caracteres. Escribe 'cancelar' para salir)");
        player.sendMessage(ChatColor.GOLD + "================================================");

        var listener = new ChatNamePromptListener();
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    private class ChatNamePromptListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent e) {
            if (!e.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
            e.setCancelled(true);

            String text = e.getMessage().trim();
            HandlerList.unregisterAll(this);

            if (text.equalsIgnoreCase("cancelar")) {
                player.sendMessage(ChatColor.RED + "Creación de personaje cancelada.");
                return;
            }

            if (text.length() < 3 || text.length() > 16 || !text.matches("^[a-zA-Z0-9_áéíóúÁÉÍÓÚñÑ ]+$")) {
                player.sendMessage(ChatColor.RED + "Nombre inválido. Debe tener entre 3 y 16 caracteres alfanuméricos.");
                Bukkit.getScheduler().runTask(plugin, () -> promptName());
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> finishCreation(text));
        }
    }

    private void finishCreation(String name) {
        CharacterProfile profile = plugin.getCharacterManager().createCharacter(
                player.getUniqueId(),
                targetSlot,
                name,
                selectedRace,
                selectedClass,
                selectedSubclass,
                selectedStats,
                selectedProficiencies,
                selectedAlignment,
                selectedBackground
        );

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Error al registrar el personaje.");
            return;
        }

        // Oro adicional si eligió la opción de dinero
        if ("GOLD".equalsIgnoreCase(startingEquipment)) {
            profile.setGold(profile.getGold() + 100);
            plugin.getDataManager().saveCharacterProfile(profile);
        }

        // Guardar hechizos iniciales seleccionados
        for (String spellId : selectedSpells) {
            plugin.getDataManager().addSpellKnown(profile.getCharacterId(), player.getUniqueId(), spellId);
            plugin.getDataManager().addSpellPrepared(profile.getCharacterId(), player.getUniqueId(), spellId);
        }

        player.sendMessage(ChatColor.GREEN + "¡Personaje " + ChatColor.GOLD + name + ChatColor.GREEN + " creado con éxito en el Slot " + targetSlot + "!");
        plugin.getCharacterManager().selectCharacter(player, profile.getCharacterId());
    }

    // ── Holder y Listener del Wizard ─────────────────────────────────────────

    public static class WizardHolder implements InventoryHolder {
        private final int step;

        public WizardHolder(int step) {
            this.step = step;
        }

        public int getStep() { return step; }

        @Override
        public Inventory getInventory() { return null; }
    }

    public static class WizardListener implements Listener {
        private final EldenCraft plugin;
        private final Map<UUID, CharacterCreationWizard> activeWizards = new HashMap<>();

        public WizardListener(EldenCraft plugin) {
            this.plugin = plugin;
        }

        public void registerWizard(UUID uuid, CharacterCreationWizard wizard) {
            activeWizards.put(uuid, wizard);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (e.getInventory().getHolder() instanceof WizardHolder holder) {
                e.setCancelled(true);
                CharacterCreationWizard wizard = activeWizards.get(player.getUniqueId());
                if (wizard == null) {
                    player.closeInventory();
                    return;
                }

                int rawSlot = e.getRawSlot();
                switch (holder.getStep()) {
                    case 1 -> { // Raza
                        RaceType[] races = RaceType.values();
                        if (rawSlot >= 0 && rawSlot < races.length) {
                            wizard.selectedRace = races[rawSlot];
                            wizard.openClassSelection();
                        }
                    }
                    case 2 -> { // Clase
                        ClassType[] classes = ClassType.values();
                        if (rawSlot >= 0 && rawSlot < classes.length) {
                            wizard.selectedClass = classes[rawSlot];
                            wizard.openSubclassSelection();
                        }
                    }
                    case 3 -> { // Subclase
                        List<String> subs = wizard.getSubclassesFor(wizard.selectedClass);
                        int idx = rawSlot - 1;
                        if (idx >= 0 && idx < subs.size()) {
                            wizard.selectedSubclass = subs.get(idx);
                            wizard.openBackgroundSelection();
                        }
                    }
                    case 4 -> { // Trasfondo
                        String[] bgs = {"Aventurero", "Acólito", "Criminal", "Noble", "Sabio", "Soldado"};
                        int idx = rawSlot - 1;
                        if (idx >= 0 && idx < bgs.length) {
                            wizard.selectedBackground = bgs[idx];
                            wizard.openAttributeSelection();
                        }
                    }
                    case 5 -> { // Atributos
                        if (rawSlot == 1) { // Matriz estándar
                            wizard.selectedStats = LevelUpManager.getStandardArrayForClass(wizard.selectedClass);
                            wizard.openSkillsSelection();
                        } else if (rawSlot == 4) { // 4d6 drop lowest
                            wizard.selectedStats = new int[]{
                                    wizard.roll4d6DropLowest(), wizard.roll4d6DropLowest(),
                                    wizard.roll4d6DropLowest(), wizard.roll4d6DropLowest(),
                                    wizard.roll4d6DropLowest(), wizard.roll4d6DropLowest()
                            };
                            player.sendMessage(ChatColor.AQUA + "✦ Tirada 4d6 generada: FUE " + wizard.selectedStats[0]
                                    + " DES " + wizard.selectedStats[1] + " CON " + wizard.selectedStats[2]
                                    + " INT " + wizard.selectedStats[3] + " SAB " + wizard.selectedStats[4] + " CAR " + wizard.selectedStats[5]);
                            wizard.openSkillsSelection();
                        } else if (rawSlot == 7) { // Compra de 27 puntos
                            wizard.selectedStats = new int[]{15, 14, 13, 12, 10, 8};
                            wizard.openSkillsSelection();
                        }
                    }
                    case 6 -> { // Competencias
                        if (rawSlot >= 0 && rawSlot < 18) {
                            SkillDefinition[] all = SkillDefinition.values();
                            if (rawSlot < all.length) {
                                SkillDefinition s = all[rawSlot];
                                if (wizard.selectedProficiencies.contains(s)) {
                                    wizard.selectedProficiencies.remove(s);
                                } else {
                                    wizard.selectedProficiencies.add(s);
                                }
                                wizard.openSkillsSelection();
                            }
                        } else if (rawSlot == 26) {
                            if (wizard.selectedProficiencies.size() >= 2) {
                                wizard.openLanguagesSelection();
                            } else {
                                player.sendMessage(ChatColor.RED + "Por favor selecciona al menos 2 habilidades.");
                            }
                        }
                    }
                    case 7 -> { // Idiomas
                        String[] langs = {"Élfico", "Enano", "Dracónico", "Orco", "Mediano", "Celestial", "Infernal"};
                        int idx = rawSlot - 1;
                        if (idx >= 0 && idx < langs.length) {
                            wizard.selectedLanguage = langs[idx];
                            wizard.openSpellsSelection();
                        }
                    }
                    case 8 -> { // Hechizos (si es lanzador)
                        List<SpellDefinition> spells = SpellRegistry.getInstance().getForClass(wizard.selectedClass);
                        if (rawSlot >= 0 && rawSlot < spells.size()) {
                            String spId = spells.get(rawSlot).getId();
                            if (wizard.selectedSpells.contains(spId)) {
                                wizard.selectedSpells.remove(spId);
                            } else {
                                wizard.selectedSpells.add(spId);
                            }
                            wizard.openSpellsSelection();
                        } else if (rawSlot == 26) {
                            wizard.openEquipmentSelection();
                        }
                    }
                    case 9 -> { // Equipo
                        if (rawSlot == 2) {
                            wizard.startingEquipment = "PACK";
                            wizard.openAlignmentSelection();
                        } else if (rawSlot == 6) {
                            wizard.startingEquipment = "GOLD";
                            wizard.openAlignmentSelection();
                        }
                    }
                    case 10 -> { // Alineamiento
                        String[] alignments = {"Legal Bueno", "Neutral Bueno", "Caótico Bueno", "Legal Neutral", "Neutral", "Caótico Neutral"};
                        int idx = rawSlot - 1;
                        if (idx >= 0 && idx < alignments.length) {
                            wizard.selectedAlignment = alignments[idx];
                            wizard.openSummaryScreen();
                        }
                    }
                    case 11 -> { // Resumen
                        if (rawSlot == 21) { // Confirmar
                            activeWizards.remove(player.getUniqueId());
                            wizard.promptName();
                        } else if (rawSlot == 23) { // Volver
                            wizard.openRaceSelection();
                        }
                    }
                }
            }
        }
    }
}
