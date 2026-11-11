package com.tuusuario.mmo.eldencraft.missions;

// Definicion de una mision (plantilla, NO el progreso del jugador).
public class Mission {

    private final String id;
    private final String name;
    private final MissionType type;
    private final String target;     // EntityType o Material en MAYUSCULAS, o "ANY", o "ANY_HOSTILE"
    private final int amount;        // Cantidad a completar
    private final int expReward;
    private final int goldReward;
    private final String difficulty; // FACIL / NORMAL / DIFICIL / EPICA / LEGENDARIA
    private final int minLevel;      // Nivel minimo del jugador para aceptar

    public Mission(String id, String name, MissionType type, String target,
                   int amount, int expReward, int goldReward,
                   String difficulty, int minLevel) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.target = target.toUpperCase();
        this.amount = amount;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.difficulty = difficulty;
        this.minLevel = minLevel;
    }

    public String getId()           { return id; }
    public String getName()         { return name; }
    public MissionType getType()    { return type; }
    public String getTarget()       { return target; }
    public int getAmount()          { return amount; }
    public int getExpReward()       { return expReward; }
    public int getGoldReward()      { return goldReward; }
    public String getDifficulty()   { return difficulty; }
    public int getMinLevel()        { return minLevel; }

    // Color del nivel de dificultad para mostrarlo en GUI.
    public String getDifficultyColor() {
        return switch (difficulty.toUpperCase()) {
            case "FACIL"      -> "§a";
            case "NORMAL"     -> "§e";
            case "DIFICIL"    -> "§6";
            case "EPICA"      -> "§5";
            case "LEGENDARIA" -> "§c";
            default           -> "§7";
        };
    }
}
