package com.tuusuario.mmo.eldencraft.missions;

// Progreso de una mision aceptada por un jugador.
public class MissionProgress {
    private final String missionId;
    private int progress;

    public MissionProgress(String missionId, int progress) {
        this.missionId = missionId;
        this.progress = progress;
    }

    public String getMissionId() { return missionId; }

    public int getProgress() { return progress; }

    public void addProgress(int amount) {
        this.progress += amount;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
