package com.tuusuario.mmo.eldencraft.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// aca guardamos los estados de los buffs/debuffs de las clases en memoria
// (barbaro berserk, guerrero parry, paladin totem, ladron critico, etc)
public final class BalanceState {

    private BalanceState() {}

    // barbaro en furia extrema: recibe x2 de daño
    public static final Set<UUID> barbarianBerserk =
            Collections.synchronizedSet(new HashSet<>());

    // guerrero en postura de combate: lifesteal por golpe
    public static final Set<UUID> warriorLifesteal =
            Collections.synchronizedSet(new HashSet<>());

    // guerrero en contraataque: refleja 50% del daño
    public static final Set<UUID> warriorParry =
            Collections.synchronizedSet(new HashSet<>());

    // paladin con totem activo: guarda cuando se vence
    public static final Map<UUID, Long> paladinTotem =
            Collections.synchronizedMap(new HashMap<>());

    // ladron: multiplicador del proximo golpe (2.0 = critico, 4.0 = doble critico)
    public static final Map<UUID, Double> rogueCritNext =
            Collections.synchronizedMap(new HashMap<>());

    public static boolean isBerserk(UUID id) { return barbarianBerserk.contains(id); }
    public static boolean hasLifesteal(UUID id) { return warriorLifesteal.contains(id); }
    public static boolean isParrying(UUID id) { return warriorParry.contains(id); }

    public static boolean hasActiveTotem(UUID id) {
        Long exp = paladinTotem.get(id);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) {
            paladinTotem.remove(id);
            return false;
        }
        return true;
    }

    // barbaro: guarda cuando fue el ultimo golpe pa la pasiva de combate
    public static final Map<UUID, Long> barbarianCombat =
            Collections.synchronizedMap(new HashMap<>());

    // druida: espinas activas, guarda cuando se vencen
    public static final Map<UUID, Long> druidThorns =
            Collections.synchronizedMap(new HashMap<>());

    // pistolero: modo duelo, bloquea curacion del objetivo
    public static final Map<UUID, Long> gunslingerDuelMode =
            Collections.synchronizedMap(new HashMap<>());
}
