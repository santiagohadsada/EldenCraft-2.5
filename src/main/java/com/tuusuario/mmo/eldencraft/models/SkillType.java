package com.tuusuario.mmo.eldencraft.models;

public enum SkillType {
    // === GUERRERO (usa energia) ===
    GOLPE_PODEROSO(1, 15), DEFENSA_SOLIDA(3, 20), CARGA_FURIOSA(6, 25), 
    ESCUDO_PROTECTOR(9, 30), FURIA_GUERRERA(12, 40), SALTO_COMBATE(16, 45), RABIA_CIEGA(20, 60),
    
    // === MAGO (usa mana) ===
    BOLA_FUEGO(1, 20), ESCUDO_MAGICO(3, 25), TELEPORT(6, 30), 
    LLUVIA_HIELO(9, 40), EXPLOSION_MAGICA(12, 55), CURACION_MAGICA(16, 60), TORMENTA_ELECTRICA(20, 80);

    private final int nivelRequerido;
    private final double costo;

    SkillType(int nivelRequerido, double costo) {
        this.nivelRequerido = nivelRequerido;
        this.costo = costo;
    }

    public int getNivelRequerido() { return nivelRequerido; }
    public double getCosto() { return costo; }
}