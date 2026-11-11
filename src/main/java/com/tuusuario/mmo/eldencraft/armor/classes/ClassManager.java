package com.tuusuario.mmo.eldencraft.armor.classes;

import org.bukkit.entity.Player;

/**
 * Contrato minimo para integrar con un sistema de clases existente.
 *
 * <p>El plugin SOLO consulta el nombre de la clase del jugador.
 * En este proyecto la implementacion oficial es
 * {@link EldenCraftClassManagerAdapter}, que delega en el PlayerManager
 * existente de EldenCraft.
 */
public interface ClassManager {

    /**
     * Devuelve el nombre de la clase del jugador en MAYUSCULAS.
     * Ejemplos: "MAGO", "GUERRERO", "PALADIN".
     * Devuelve null si el jugador aun no tiene clase asignada.
     */
    String getClassName(Player player);

    default boolean hasClass(Player player) {
        String name = getClassName(player);
        return name != null && !name.isBlank();
    }
}
