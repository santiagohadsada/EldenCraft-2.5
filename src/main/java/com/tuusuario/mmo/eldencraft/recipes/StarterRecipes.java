package com.tuusuario.mmo.eldencraft.recipes;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorType;
import com.tuusuario.mmo.eldencraft.models.ClassType;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Recetas del equipo inicial. Cada clase tiene 4 piezas (casco/peto/grebas/botas)
// hechas con su material caracteristico + un "nucleo" simbolico que la marca como
// receta de clase. Solo el jugador con la clase correcta puede craftearla:
// el resto ve la matrix vacia.
public final class StarterRecipes implements Listener {

    // Material base por clase
    private static final Map<ClassType, ArmorType> MAT = new EnumMap<>(ClassType.class);
    // Nucleo (centro de la receta) por clase
    private static final Map<ClassType, Material> CORE = new EnumMap<>(ClassType.class);

    static {
        // Tanques / cuerpo a cuerpo: hierro
        MAT.put(ClassType.WARRIOR,    ArmorType.IRON);
        MAT.put(ClassType.BARBARIAN,  ArmorType.IRON);
        // Sagrado: oro
        MAT.put(ClassType.PALADIN,    ArmorType.GOLD);
        // Magos / soporte: cuero
        MAT.put(ClassType.MAGE,       ArmorType.LEATHER);
        MAT.put(ClassType.BARD,       ArmorType.LEATHER);
        // Sigilo y caza: malla
        MAT.put(ClassType.ROGUE,      ArmorType.CHAINMAIL);
        MAT.put(ClassType.RANGER,     ArmorType.CHAINMAIL);

        CORE.put(ClassType.WARRIOR,   Material.NETHERITE_SCRAP);
        CORE.put(ClassType.BARBARIAN, Material.BONE);
        CORE.put(ClassType.PALADIN,   Material.GLOWSTONE_DUST);
        CORE.put(ClassType.MAGE,      Material.BLAZE_POWDER);
        CORE.put(ClassType.BARD,      Material.NOTE_BLOCK);
        CORE.put(ClassType.ROGUE,     Material.STRING);
        CORE.put(ClassType.RANGER,    Material.ARROW);
    }

    private final EldenCraft plugin;
    private final Map<NamespacedKey, ClassType> recipeClass = new HashMap<>();

    public StarterRecipes(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void giveStarterBook(Player player) {
        if (player == null) return;
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        org.bukkit.inventory.meta.BookMeta meta = (org.bukkit.inventory.meta.BookMeta) book.getItemMeta();
        if (meta != null) {
            meta.setTitle(ChatColor.GOLD + "Guía del Aventurero");
            meta.setAuthor("EldenCraft");
            meta.addPage(ChatColor.DARK_BLUE + "=== ELDENCRAFT ===\n\n"
                    + ChatColor.BLACK + "¡Bienvenido a tu aventura!\n\n"
                    + "Usa " + ChatColor.DARK_GREEN + "/personajes" + ChatColor.BLACK + " para gestionar tus personajes.\n\n"
                    + "Usa " + ChatColor.DARK_GREEN + "/ficha" + ChatColor.BLACK + " para ver tu hoja de personaje.\n\n"
                    + "Usa " + ChatColor.DARK_GREEN + "/campana" + ChatColor.BLACK + " para ver tus campañas.");
            book.setItemMeta(meta);
            player.getInventory().addItem(book);
        }
    }

    public int registeredCount() { return recipeClass.size(); }

    public void register() {
        int total = 0;
        for (ClassType clazz : ClassType.values()) {
            ArmorType type = MAT.get(clazz);
            Material core = CORE.get(clazz);
            if (type == null || core == null) continue;

            Material base = baseIngredient(type);
            for (ArmorSlot slot : ArmorSlot.values()) {
                NamespacedKey key = new NamespacedKey(
                        plugin,
                        "starter_" + clazz.name().toLowerCase() + "_" + slot.name().toLowerCase()
                );

                ItemStack out = plugin.armorManager()
                        .generate(type, slot, ArmorRarity.COMUN, 0);
                if (out == null) continue;

                // Renombrar para que se note que es el set inicial
                ItemMeta meta = out.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.WHITE + "Equipo Inicial - "
                            + clazz.displayName + " - " + slotEs(slot));
                    out.setItemMeta(meta);
                }

                ShapedRecipe recipe = new ShapedRecipe(key, out);
                shapeFor(recipe, slot);
                recipe.setIngredient('X', base);
                recipe.setIngredient('C', core);

                try {
                    Bukkit.removeRecipe(key); // por si quedaba de un /reload
                    Bukkit.addRecipe(recipe);
                    recipeClass.put(key, clazz);
                    total++;
                } catch (IllegalStateException ignored) {
                    // ya existia, lo dejo asi
                }
            }
        }
        plugin.getLogger().info("Recetas iniciales registradas: " + total
                + "  (" + ClassType.values().length + " clases x 4 piezas)");
    }

    private void shapeFor(ShapedRecipe r, ArmorSlot slot) {
        // X = material base, C = nucleo de clase, espacio = vacio.
        // No copiamos las shapes vanilla tal cual a proposito: anadimos C al
        // centro para evitar choques con las recetas normales del juego.
        switch (slot) {
            case HELMET     -> r.shape("XXX", "XCX");
            case CHESTPLATE -> r.shape("XCX", "XXX", "XXX");
            case LEGGINGS   -> r.shape("XCX", "X X", "X X");
            case BOOTS      -> r.shape("XCX", "X X");
        }
    }

    private Material baseIngredient(ArmorType type) {
        return switch (type) {
            case LEATHER   -> Material.LEATHER;
            case CHAINMAIL -> Material.IRON_NUGGET; // malla "improvisada"
            case IRON      -> Material.IRON_INGOT;
            case GOLD      -> Material.GOLD_INGOT;
            case DIAMOND   -> Material.DIAMOND;
            case NETHERITE -> Material.NETHERITE_INGOT;
        };
    }

    private String slotEs(ArmorSlot s) {
        return switch (s) {
            case HELMET     -> "Casco";
            case CHESTPLATE -> "Peto";
            case LEGGINGS   -> "Grebas";
            case BOOTS      -> "Botas";
        };
    }

    // Cuando alguien intenta sacar el item, comprobamos su clase y cancelamos
    // si no coincide.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent e) {
        ClassType req = requiredClass(e.getRecipe());
        if (req == null) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (data == null) {
            // datos aun no cargados, lo dejamos pasar para no bloquear sin razon
            return;
        }
        if (data.getPlayerClass() != req) {
            e.setCancelled(true);
            p.sendMessage(ChatColor.RED + "Esta receta es exclusiva de la clase "
                    + ChatColor.YELLOW + req.displayName + ChatColor.RED + ".");
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    // Mientras el jugador previsualiza la matrix, si no es de la clase correcta
    // vaciamos el resultado: asi sabe en el momento que no le sirve.
    // Si la clase SI coincide, regeneramos la pieza con stats nuevos para que
    // cada crafteo tenga rolls distintos (no el mismo item compartido).
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(PrepareItemCraftEvent e) {
        Recipe r = e.getRecipe();
        ClassType req = requiredClass(r);
        if (req == null) return;

        Player p = resolvePlayer(e);
        if (p == null) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
        if (data == null) return;       // sin datos => no bloqueamos

        if (data.getPlayerClass() != req) {
            e.getInventory().setResult(null);
            return;
        }

        // Coincide: regeneramos un item fresco con rolls aleatorios
        ArmorType type = MAT.get(req);
        ArmorSlot slot = slotOf(((Keyed) r).getKey());
        if (type == null || slot == null) return;

        ItemStack fresh = plugin.armorManager().generate(type, slot, ArmorRarity.COMUN, 0);
        if (fresh == null) return;
        ItemMeta meta = fresh.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "Equipo Inicial - "
                    + req.displayName + " - " + slotEs(slot));
            fresh.setItemMeta(meta);
        }
        e.getInventory().setResult(fresh);
    }

    private ClassType requiredClass(Recipe r) {
        if (!(r instanceof Keyed keyed)) return null;
        return recipeClass.get(keyed.getKey());
    }

    private Player resolvePlayer(PrepareItemCraftEvent e) {
        try {
            HumanEntity viewer = e.getView().getPlayer();
            if (viewer instanceof Player p) return p;
        } catch (Throwable ignored) { /* en algunas versiones puede explotar */ }
        for (HumanEntity h : e.getViewers()) {
            if (h instanceof Player p) return p;
        }
        return null;
    }

    private ArmorSlot slotOf(NamespacedKey key) {
        if (key == null) return null;
        String k = key.getKey();
        if (k.endsWith("_helmet"))     return ArmorSlot.HELMET;
        if (k.endsWith("_chestplate")) return ArmorSlot.CHESTPLATE;
        if (k.endsWith("_leggings"))   return ArmorSlot.LEGGINGS;
        if (k.endsWith("_boots"))      return ArmorSlot.BOOTS;
        return null;
    }
}
