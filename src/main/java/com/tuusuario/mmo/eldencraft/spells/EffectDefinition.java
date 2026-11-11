package com.tuusuario.mmo.eldencraft.spells;

/**
 * Define un efecto individual dentro de un hechizo (SPEC 5.2).
 */
public class EffectDefinition {

    private String tipo;            // daño | curacion | condicion | salvacion | ataque_conjuro | movimiento | buff | debuff | utilidad | especial
    private String dados;           // ej: "8d6", "1d8"
    private String tipoDano;        // fuego, frio, relampago, necrotico, etc.
    private boolean salvacionMitad; // si true, éxito en salvación = mitad de daño
    private String salvacion;       // "FUE", "DES", "CON", "INT", "SAB", "CAR"
    private String condicion;       // cegado, envenenado, etc.
    private String duracion;        // en rondas o "hasta_eliminado"
    private String mejoraNivel;     // ej: "+1d6 por nivel superior"
    private String mejoraPersonaje; // ej: "+1d10 a nivel 5, 11 y 17"

    public EffectDefinition() {}

    public String getTipo() { return tipo != null ? tipo.toLowerCase() : "daño"; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDados() { return dados; }
    public void setDados(String dados) { this.dados = dados; }

    public String getTipoDano() { return tipoDano != null ? tipoDano.toLowerCase() : "fuerza"; }
    public void setTipoDano(String tipoDano) { this.tipoDano = tipoDano; }

    public boolean isSalvacionMitad() { return salvacionMitad; }
    public void setSalvacionMitad(boolean salvacionMitad) { this.salvacionMitad = salvacionMitad; }

    public String getSalvacion() { return salvacion; }
    public void setSalvacion(String salvacion) { this.salvacion = salvacion; }

    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getMejoraNivel() { return mejoraNivel; }
    public void setMejoraNivel(String mejoraNivel) { this.mejoraNivel = mejoraNivel; }

    public String getMejoraPersonaje() { return mejoraPersonaje; }
    public void setMejoraPersonaje(String mejoraPersonaje) { this.mejoraPersonaje = mejoraPersonaje; }
}
