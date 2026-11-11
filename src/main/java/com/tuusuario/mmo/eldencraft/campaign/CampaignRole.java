package com.tuusuario.mmo.eldencraft.campaign;

/**
 * Roles de participantes en una campaña D&D (SPEC 15.4 / F3B-2).
 */
public enum CampaignRole {
    DM_PRINCIPAL("DM Principal", true, true),
    CO_DM("Co-DM", true, false),
    JUGADOR("Jugador", false, false),
    ESPECTADOR("Espectador", false, false);

    public final String displayName;
    public final boolean isDm;
    public final boolean isOwner;

    CampaignRole(String displayName, boolean isDm, boolean isOwner) {
        this.displayName = displayName;
        this.isDm = isDm;
        this.isOwner = isOwner;
    }

    public static CampaignRole fromString(String str) {
        if (str == null) return JUGADOR;
        for (CampaignRole r : values()) {
            if (r.name().equalsIgnoreCase(str) || r.displayName.equalsIgnoreCase(str)) return r;
        }
        return JUGADOR;
    }
}
