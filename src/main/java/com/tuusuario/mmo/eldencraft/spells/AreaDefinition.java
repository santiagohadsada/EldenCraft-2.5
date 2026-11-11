package com.tuusuario.mmo.eldencraft.spells;

/**
 * Define el área geométrica de un hechizo D&D 5e (esfera, cubo, cono, línea, cilindro).
 */
public class AreaDefinition {

    private String forma; // "esfera", "cubo", "cono", "linea", "cilindro"
    private int tamanoPies;

    public AreaDefinition() {}

    public AreaDefinition(String forma, int tamanoPies) {
        this.forma = forma;
        this.tamanoPies = tamanoPies;
    }

    public String getForma() { return forma != null ? forma.toLowerCase() : "esfera"; }
    public void setForma(String forma) { this.forma = forma; }

    public int getTamanoPies() { return tamanoPies; }
    public void setTamanoPies(int tamanoPies) { this.tamanoPies = tamanoPies; }

    /** Convierte el tamaño en pies a bloques de Minecraft (5 pies = 1 bloque). */
    public double getTamanoBloques(double feetPerBlock) {
        return tamanoPies / (feetPerBlock > 0 ? feetPerBlock : 5.0);
    }
}
