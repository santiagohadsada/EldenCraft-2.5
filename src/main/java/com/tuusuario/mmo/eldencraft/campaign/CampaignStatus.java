package com.tuusuario.mmo.eldencraft.campaign;

/**
 * Estado de una campaña (SPEC 15.4 / F3B-2).
 */
public enum CampaignStatus {
    BORRADOR("Borrador"),
    ABIERTA("Abierta"),
    SOLO_INVITADOS("Solo Invitados"),
    EN_PAUSA("En Pausa"),
    CERRADA("Cerrada"),
    ARCHIVADA("Archivada");

    public final String displayName;

    CampaignStatus(String displayName) {
        this.displayName = displayName;
    }

    public static CampaignStatus fromString(String str) {
        if (str == null) return ABIERTA;
        for (CampaignStatus s : values()) {
            if (s.name().equalsIgnoreCase(str) || s.displayName.equalsIgnoreCase(str)) return s;
        }
        return ABIERTA;
    }
}
