package com.tuusuario.mmo.eldencraft.models;
// las razas del juego, cada una tiene stats distintos y un tamaño diferente
public enum RaceType {
    // Nombre, STR, DEX, CON, INT, WIS, CHA, vida base, escala (1.0 = tamaño normal)
    HUMAN("Humano", 10, 10, 10, 10, 10, 10, 20.0, 1.0),
    DWARF("Enano", 12, 8, 14, 10, 12, 8, 24.0, 0.78),
    ELF("Elfo", 8, 14, 10, 12, 10, 10, 16.0, 1.06),
    HOBBIT("Hobbit", 6, 10, 12, 8, 10, 8, 12.0, 0.67),
    ORC("Orco", 16, 10, 14, 8, 8, 6, 28.0, 1.11),
    GNOME("Gnomo", 8, 12, 10, 14, 10, 10, 16.0, 0.56),
    // estas las agregamos despues, las razas nuevas
    MEDIANO("Mediano", 8, 12, 11, 10, 10, 11, 18.0, 0.72),
    TIEFLING("Tiefling", 9, 10, 10, 11, 9, 14, 20.0, 1.03),
    GOLIATH("Goliat", 15, 8, 15, 8, 10, 6, 30.0, 1.28),
    FORGED("Forjado", 12, 8, 14, 12, 10, 6, 28.0, 1.11),
    UNDEAD("No-muerto", 10, 8, 12, 10, 8, 5, 24.0, 1.0),
    AASIMAR("Aasimar", 10, 10, 11, 10, 12, 14, 22.0, 1.06),
    // humanoide-pájaro del dnd: ágil, sabio, liviano. Puede planear.
    AARAKOCRA("Aarakocra", 8, 16, 10, 10, 12, 10, 18.0, 1.03);

    // atributos del enum, son final pq no deberian cambiar nunca
    public final String displayName;
    public final int str, dex, con, intel, wis, cha;
    public final double baseHealth;
    public final double scale;

    // constructor, nada loco aca
    RaceType(String displayName, int str, int dex, int con, int intel, int wis, int cha, double baseHealth, double scale) {
        this.displayName = displayName;
        this.str = str;
        this.dex = dex;
        this.con = con;
        this.intel = intel;
        this.wis = wis;
        this.cha = cha;
        this.baseHealth = baseHealth;
        this.scale = scale;
    }
}