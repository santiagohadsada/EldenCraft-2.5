package com.tuusuario.mmo.eldencraft.spells;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la definición de un hechizo D&D 5e cargado desde YAML (SPEC 5.1).
 */
public class SpellDefinition {

    private String id;
    private String nombre;
    private int nivel;                  // 0-9 (0 = truco)
    private String escuela;             // evocacion, abjuracion, conjuracion, etc.
    private List<String> clases = new ArrayList<>(); // nombres de ClassType en minúsculas
    private String tiempoLanzamiento;   // accion, adicional, reaccion, 1_minuto, etc.
    private boolean ritual;
    private String alcance;             // "propio", "toque", "120 pies", etc.
    private List<String> componentes = new ArrayList<>(); // ["V", "S", "M:..."]
    private String duracion;            // "instantaneo", "1 minuto", etc.
    private boolean concentracion;
    private String objetivo;            // "yo", "criatura", "punto", "area"
    private AreaDefinition area;
    private List<EffectDefinition> efectos = new ArrayList<>();
    private String mejoraNivel;         // upcasting: ej: "+1d6 por nivel superior al 1"
    private String mejoraPersonaje;     // trucos: "+1d10 a nivel 5, 11 y 17"
    private boolean custom;
    private String fuente;              // "SRD", "custom"

    public SpellDefinition() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre != null ? nombre : id; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public String getEscuela() { return escuela != null ? escuela.toLowerCase() : "evocacion"; }
    public void setEscuela(String escuela) { this.escuela = escuela; }

    public List<String> getClases() { return clases; }
    public void setClases(List<String> clases) { this.clases = clases; }

    public String getTiempoLanzamiento() { return tiempoLanzamiento != null ? tiempoLanzamiento : "accion"; }
    public void setTiempoLanzamiento(String tiempoLanzamiento) { this.tiempoLanzamiento = tiempoLanzamiento; }

    public boolean isRitual() { return ritual; }
    public void setRitual(boolean ritual) { this.ritual = ritual; }

    public String getAlcance() { return alcance != null ? alcance : "propio"; }
    public void setAlcance(String alcance) { this.alcance = alcance; }

    public List<String> getComponentes() { return componentes; }
    public void setComponentes(List<String> componentes) { this.componentes = componentes; }

    public String getDuracion() { return duracion != null ? duracion : "instantaneo"; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public boolean isConcentracion() { return concentracion; }
    public void setConcentracion(boolean concentracion) { this.concentracion = concentracion; }

    public String getObjetivo() { return objetivo != null ? objetivo : "criatura"; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public AreaDefinition getArea() { return area; }
    public void setArea(AreaDefinition area) { this.area = area; }

    public List<EffectDefinition> getEfectos() { return efectos; }
    public void setEfectos(List<EffectDefinition> efectos) { this.efectos = efectos; }

    public String getMejoraNivel() { return mejoraNivel; }
    public void setMejoraNivel(String mejoraNivel) { this.mejoraNivel = mejoraNivel; }

    public String getMejoraPersonaje() { return mejoraPersonaje; }
    public void setMejoraPersonaje(String mejoraPersonaje) { this.mejoraPersonaje = mejoraPersonaje; }

    public boolean isCustom() { return custom; }
    public void setCustom(boolean custom) { this.custom = custom; }

    public String getFuente() { return fuente != null ? fuente : "SRD"; }
    public void setFuente(String fuente) { this.fuente = fuente; }
}
