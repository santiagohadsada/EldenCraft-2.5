package com.tuusuario.mmo.eldencraft.listeners;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.missions.Mission;
import com.tuusuario.mmo.eldencraft.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Menu de misiones con paginacion y filtros.
public class MissionGUI implements Listener {

    public static final String TITLE_BASE = "§8➤ §lMisiones MMO";
    private static final int PAGE_SIZE = 45;

    private final EldenCraft plugin;

    // Estado de UI por jugador (pagina y filtro).
    private final Map<UUID, GUIState> states = new HashMap<>();

    public MissionGUI(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        states.put(player.getUniqueId(), new GUIState());
        render(player);
    }

    private void render(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        GUIState st = states.computeIfAbsent(player.getUniqueId(), k -> new GUIState());

        // Filtrar segun el estado seleccionado
        List<Mission> filtered = new ArrayList<>();
        for (Mission m : plugin.getMissionManager().getAllMissions().values()) {
            boolean completed = data.getCompletedMissions().contains(m.getId());
            boolean active = data.getActiveMissions().containsKey(m.getId());
            switch (st.filter) {
                case ACTIVAS      -> { if (active)    filtered.add(m); }
                case COMPLETADAS  -> { if (completed) filtered.add(m); }
                case DISPONIBLES  -> { if (!active && !completed && data.getLevel() >= m.getMinLevel()) filtered.add(m); }
                case TODAS        -> filtered.add(m);
            }
        }

        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) PAGE_SIZE));
        if (st.page >= totalPages) st.page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54,
                TITLE_BASE + " §7[" + st.filter.label + " " + (st.page + 1) + "/" + totalPages + "]");

        int from = st.page * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filtered.size());
        int slot = 0;
        for (int i = from; i < to; i++) {
            inv.setItem(slot++, missionItem(filtered.get(i), data));
        }

        // fila inferior (45..53)
        // 45: pagina anterior
        inv.setItem(45, simple(Material.ARROW, "§e« Pagina anterior",
                "§7Click para retroceder"));
        // 46-49: filtros
        inv.setItem(46, filterBtn(Material.PAPER,        Filter.TODAS,       st.filter));
        inv.setItem(47, filterBtn(Material.WRITABLE_BOOK,Filter.ACTIVAS,     st.filter));
        inv.setItem(48, filterBtn(Material.LIME_DYE,     Filter.COMPLETADAS, st.filter));
        inv.setItem(49, filterBtn(Material.MAP,          Filter.DISPONIBLES, st.filter));
        // 50-52: info jugador
        inv.setItem(51, simple(Material.PLAYER_HEAD, "§6§l" + player.getName(),
                "§7Nivel: §f" + data.getLevel() + "/20",
                "§7Oro: §6" + data.getGold(),
                "§7Activas: §f" + data.getActiveMissions().size(),
                "§7Completadas: §a" + data.getCompletedMissions().size()));
        // 53: pagina siguiente
        inv.setItem(53, simple(Material.ARROW, "§ePagina siguiente »",
                "§7Click para avanzar"));

        // Relleno
        ItemStack pane = simple(Material.GRAY_STAINED_GLASS_PANE, " ");
        if (inv.getItem(50) == null) inv.setItem(50, pane);
        if (inv.getItem(52) == null) inv.setItem(52, pane);

        player.openInventory(inv);
    }

    private ItemStack missionItem(Mission m, PlayerData data) {
        boolean completed = data.getCompletedMissions().contains(m.getId());
        boolean active = data.getActiveMissions().containsKey(m.getId());
        int prog = active ? data.getActiveMissions().get(m.getId()).getProgress() : 0;

        Material icon = Material.PAPER;
        if (completed) icon = Material.LIME_DYE;
        else if (active) icon = Material.WRITABLE_BOOK;
        else if (data.getLevel() < m.getMinLevel()) icon = Material.GRAY_DYE;

        ItemStack stack = new ItemStack(icon);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(m.getDifficultyColor() + ChatColor.BOLD + m.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "ID: " + ChatColor.DARK_GRAY + m.getId());
            lore.add(ChatColor.GRAY + "Tipo: " + ChatColor.WHITE + m.getType().name());
            lore.add(ChatColor.GRAY + "Objetivo: " + ChatColor.WHITE + m.getAmount() + "x " + m.getTarget());
            lore.add(ChatColor.GRAY + "Dificultad: " + m.getDifficultyColor() + m.getDifficulty());
            lore.add(ChatColor.GRAY + "Nivel min: " + ChatColor.WHITE + m.getMinLevel());
            lore.add("");
            lore.add(ChatColor.GREEN + "Recompensa:");
            lore.add("  " + ChatColor.AQUA + "+" + m.getExpReward() + " EXP");
            lore.add("  " + ChatColor.GOLD + "+" + m.getGoldReward() + " oro");
            lore.add("");
            if (completed) {
                lore.add(ChatColor.GREEN + "✔ COMPLETADA");
            } else if (active) {
                int pct = (int) Math.min(100, (prog * 100.0) / Math.max(1, m.getAmount()));
                lore.add(ChatColor.YELLOW + "Progreso: " + prog + "/" + m.getAmount()
                        + ChatColor.GRAY + " (" + pct + "%)");
                lore.add(progressBar(pct));
            } else if (data.getLevel() < m.getMinLevel()) {
                lore.add(ChatColor.RED + "✖ Nivel insuficiente");
            } else {
                lore.add(ChatColor.GREEN + "▶ Click para aceptar");
            }
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String progressBar(int pct) {
        int filled = pct / 5; // 20 segmentos
        StringBuilder sb = new StringBuilder(ChatColor.DARK_GRAY + "[");
        for (int i = 0; i < 20; i++) {
            if (i < filled) sb.append(ChatColor.GREEN).append("|");
            else sb.append(ChatColor.GRAY).append("|");
        }
        sb.append(ChatColor.DARK_GRAY).append("]");
        return sb.toString();
    }

    private ItemStack filterBtn(Material m, Filter f, Filter selected) {
        boolean on = (f == selected);
        return simple(m,
                (on ? "§a§l▶ " : "§7") + f.label,
                on ? "§7Filtro activo" : "§7Click para filtrar");
    }

    private ItemStack simple(Material m, String name, String... lore) {
        ItemStack s = new ItemStack(m);
        ItemMeta meta = s.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(List.of(lore));
            s.setItemMeta(meta);
        }
        return s;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String t = e.getView().getTitle();
        if (t == null || !t.startsWith(TITLE_BASE)) return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        GUIState st = states.computeIfAbsent(p.getUniqueId(), k -> new GUIState());
        int slot = e.getRawSlot();

        // Botones de la fila inferior
        if (slot == 45) { st.page = Math.max(0, st.page - 1); render(p); return; }
        if (slot == 53) { st.page = st.page + 1; render(p); return; }
        if (slot == 46) { st.filter = Filter.TODAS;       st.page = 0; render(p); return; }
        if (slot == 47) { st.filter = Filter.ACTIVAS;     st.page = 0; render(p); return; }
        if (slot == 48) { st.filter = Filter.COMPLETADAS; st.page = 0; render(p); return; }
        if (slot == 49) { st.filter = Filter.DISPONIBLES; st.page = 0; render(p); return; }
        if (slot >= 45) return; // resto de la fila inferior: ignorar

        // Click en mision -> aceptar
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        for (Mission m : plugin.getMissionManager().getAllMissions().values()) {
            if (m.getName().equalsIgnoreCase(name)) {
                PlayerData d = plugin.getPlayerManager().getPlayerData(p.getUniqueId());
                if (d == null) return;
                plugin.getMissionManager().acceptMission(p, d, m.getId());
                render(p);
                return;
            }
        }
    }

    private enum Filter {
        TODAS("Todas"),
        ACTIVAS("Activas"),
        COMPLETADAS("Completadas"),
        DISPONIBLES("Disponibles");

        final String label;
        Filter(String s) { this.label = s; }
    }

    private static class GUIState {
        int page = 0;
        Filter filter = Filter.TODAS;
    }
}
