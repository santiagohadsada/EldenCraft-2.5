package com.tuusuario.mmo.eldencraft.managers;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

// sistema de parties, pa jugar en grupo
public class PartyManager {

    public static class Party {
        public final UUID leader;
        public final List<UUID> members = new ArrayList<>();

        public Party(UUID leader) {
            this.leader = leader;
            this.members.add(leader);
        }
    }

    private final EldenCraft plugin;

    // mapa: uuid de cualquier miembro -> la party en la q esta
    private final Map<UUID, Party> playerToParty = new HashMap<>();

    // invitaciones pendientes: uuid del q invitaron -> uuid del lider q invito
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    // distancia maxima pa compartir exp (ya no se usa pero lo dejamos por las dudas)
    public static final double XP_SHARE_RADIUS = 50.0;

    // distancia maxima entre el mob muerto y los miembros pa q reciban exp
    public static final double XP_SHARE_RADIUS_FROM_ENEMY = 25.0;

    // porcentaje de exp q reciben los otros miembros (el q mata siempre recibe 100%)
    public static final double SHARED_XP_RATIO = 0.6;

    public PartyManager(EldenCraft plugin) {
        this.plugin = plugin;
    }

    public Party getParty(UUID uuid) { return playerToParty.get(uuid); }

    public boolean isInParty(UUID uuid) { return playerToParty.containsKey(uuid); }

    // crea una party nueva con el jugador como lider
    public boolean createParty(Player leader) {
        if (isInParty(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Ya estas en una party. Usa /party leave primero.");
            return false;
        }
        Party p = new Party(leader.getUniqueId());
        playerToParty.put(leader.getUniqueId(), p);
        leader.sendMessage(ChatColor.GREEN + "✔ Party creada. Eres el lider. Usa "
                + ChatColor.YELLOW + "/party invite <jugador>" + ChatColor.GREEN + " para invitar.");
        return true;
    }

    // manda una invitacion a otro jugador
    public boolean invite(Player leader, Player target) {
        Party p = getParty(leader.getUniqueId());
        if (p == null || !p.leader.equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Solo el lider de la party puede invitar.");
            return false;
        }
        if (target.getUniqueId().equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "No puedes invitarte a ti mismo.");
            return false;
        }
        if (isInParty(target.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + target.getName() + " ya esta en una party.");
            return false;
        }
        String leaderCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, leader);
        String targetCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, target);
        if (!leaderCamp.equalsIgnoreCase(targetCamp)) {
            leader.sendMessage(ChatColor.RED + "Solo puedes invitar a miembros de tu misma campaña.");
            return false;
        }
        pendingInvites.put(target.getUniqueId(), leader.getUniqueId());
        leader.sendMessage(ChatColor.GREEN + "✔ Invitacion enviada a " + ChatColor.YELLOW + target.getName());
        target.sendMessage(ChatColor.AQUA + "✦ " + leader.getName()
                + " te ha invitado a su party. Acepta con " + ChatColor.YELLOW + "/party accept");
        return true;
    }

    // el jugador acepta la invitacion
    public boolean accept(Player invitee) {
        UUID leaderId = pendingInvites.remove(invitee.getUniqueId());
        if (leaderId == null) {
            invitee.sendMessage(ChatColor.RED + "No tienes invitaciones pendientes.");
            return false;
        }
        Party p = getParty(leaderId);
        if (p == null) {
            invitee.sendMessage(ChatColor.RED + "La party ya no existe.");
            return false;
        }
        if (isInParty(invitee.getUniqueId())) {
            invitee.sendMessage(ChatColor.RED + "Ya estas en una party.");
            return false;
        }
        Player leader = Bukkit.getPlayer(leaderId);
        if (leader != null) {
            String leaderCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, leader);
            String inviteeCamp = com.tuusuario.mmo.eldencraft.campaign.CampaignContext.getCampaignId(plugin, invitee);
            if (!leaderCamp.equalsIgnoreCase(inviteeCamp)) {
                invitee.sendMessage(ChatColor.RED + "El líder de la party pertenece a otra campaña.");
                return false;
            }
        }
        p.members.add(invitee.getUniqueId());
        playerToParty.put(invitee.getUniqueId(), p);
        broadcast(p, ChatColor.GREEN + "✔ " + invitee.getName() + " se ha unido a la party.");
        return true;
    }

    // salirse de la party
    public boolean leave(Player player) {
        Party p = getParty(player.getUniqueId());
        if (p == null) {
            player.sendMessage(ChatColor.RED + "No estas en ninguna party.");
            return false;
        }
        p.members.remove(player.getUniqueId());
        playerToParty.remove(player.getUniqueId());

        if (p.members.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "Has dejado la party (disuelta porque estaba vacia).");
            return true;
        }

        if (p.leader.equals(player.getUniqueId())) {
            // le pasamos el liderazgo al primer miembro q quede
            UUID newLeader = p.members.get(0);
            Party newParty = new Party(newLeader);
            // reutilizamos los miembros
            newParty.members.clear();
            newParty.members.addAll(p.members);
            for (UUID m : p.members) playerToParty.put(m, newParty);
            broadcast(newParty, ChatColor.YELLOW + "★ " + player.getName()
                    + " salio. Nuevo lider: " + Bukkit.getOfflinePlayer(newLeader).getName());
            player.sendMessage(ChatColor.GRAY + "Has dejado la party.");
            return true;
        }

        broadcast(p, ChatColor.GRAY + "✦ " + player.getName() + " ha dejado la party.");
        player.sendMessage(ChatColor.GRAY + "Has dejado la party.");
        return true;
    }

    // el lider echa a alguien
    public boolean kick(Player leader, Player target) {
        Party p = getParty(leader.getUniqueId());
        if (p == null || !p.leader.equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Solo el lider puede expulsar.");
            return false;
        }
        if (!p.members.contains(target.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + target.getName() + " no esta en tu party.");
            return false;
        }
        if (target.getUniqueId().equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "No puedes expulsarte a ti mismo. Usa /party disband.");
            return false;
        }
        p.members.remove(target.getUniqueId());
        playerToParty.remove(target.getUniqueId());
        target.sendMessage(ChatColor.RED + "✖ Has sido expulsado de la party por " + leader.getName() + ".");
        broadcast(p, ChatColor.YELLOW + "✦ " + target.getName() + " fue expulsado de la party.");
        return true;
    }

    // el lider disuelve todo el grupo
    public boolean disband(Player leader) {
        Party p = getParty(leader.getUniqueId());
        if (p == null || !p.leader.equals(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Solo el lider puede disolver la party.");
            return false;
        }
        broadcast(p, ChatColor.RED + "✖ La party ha sido disuelta.");
        for (UUID id : new ArrayList<>(p.members)) {
            playerToParty.remove(id);
        }
        return true;
    }

    // cuando alguien se desconecta limpiamos todo
    public void handleQuit(UUID uuid) {
        pendingInvites.remove(uuid);
        // limpiamos las invitaciones q haya mandado este jugador
        pendingInvites.entrySet().removeIf(e -> e.getValue().equals(uuid));

        Party p = getParty(uuid);
        if (p == null) return;

        p.members.remove(uuid);
        playerToParty.remove(uuid);

        if (p.members.isEmpty()) return;

        if (p.leader.equals(uuid)) {
            UUID newLeader = p.members.get(0);
            Party newParty = new Party(newLeader);
            newParty.members.clear();
            newParty.members.addAll(p.members);
            for (UUID m : p.members) playerToParty.put(m, newParty);
            broadcast(newParty, ChatColor.YELLOW + "★ El lider se desconecto. Nuevo lider: "
                    + Bukkit.getOfflinePlayer(newLeader).getName());
        }
    }

    // lista de miembros q estan online
    public List<Player> getOnlineMembers(Party p) {
        List<Player> list = new ArrayList<>();
        for (UUID id : p.members) {
            Player pl = Bukkit.getPlayer(id);
            if (pl != null && pl.isOnline()) list.add(pl);
        }
        return list;
    }

    // manda un mensaje a todos los de la party q esten conectados
    public void broadcast(Party p, String msg) {
        for (Player pl : getOnlineMembers(p)) pl.sendMessage(msg);
    }

    public boolean isSameParty(Player p1, Player p2) {
        if (p1.equals(p2)) return true;
        Party pa = getParty(p1.getUniqueId());
        Party pb = getParty(p2.getUniqueId());
        return pa != null && pb != null && pa == pb;
    }

    public boolean isAlly(Player caster, org.bukkit.entity.Entity target) {
        if (target instanceof Player) {
            return isSameParty(caster, (Player) target);
        }
        return false;
    }

    public boolean canHeal(Player caster, Player target) {
        if (caster.equals(target)) return true;
        Party p = getParty(caster.getUniqueId());
        if (p != null) {
            Party p2 = getParty(target.getUniqueId());
            return p == p2;
        }
        return true;
    }

    public EldenCraft getPlugin() { return plugin; }
}
