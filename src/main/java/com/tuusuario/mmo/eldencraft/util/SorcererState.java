package com.tuusuario.mmo.eldencraft.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Estado estático compartido del Hechicero.
 * arcaneCharges  — cargas de Reacción Arcana (pasiva, al recibir daño).
 * arcanaReady    — UUIDs con la Reacción Arcana lista (5 cargas acumuladas).
 * sobrecargaActiva — UUIDs con Sobrecarga activa (duplica siguiente habilidad).
 * safeWitherSkulls — UUIDs de WitherSkulls del Brujo que no deben romper bloques.
 */
public final class SorcererState {

    public static final Map<UUID, Integer> arcaneCharges    = new HashMap<>();
    public static final Set<UUID>          arcanaReady      = new HashSet<>();
    public static final Set<UUID>          sobrecargaActiva = new HashSet<>();
    public static final Set<UUID>          safeWitherSkulls = new HashSet<>();
    public static final Map<UUID, Long>    desfaseInmune    = new HashMap<>();

    private SorcererState() {}
}
