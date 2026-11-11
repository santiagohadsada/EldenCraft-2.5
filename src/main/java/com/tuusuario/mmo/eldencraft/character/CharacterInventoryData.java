package com.tuusuario.mmo.eldencraft.character;

/**
 * Contenedor de datos de inventario y estado espacial de un personaje guardado.
 */
public class CharacterInventoryData {
    public final String inventoryData;
    public final String armorData;
    public final String offhandData;
    public final String enderChestData;
    public final String world;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;
    public final String gameMode;

    public CharacterInventoryData(String inventoryData, String armorData, String offhandData,
                                  String enderChestData, String world, double x, double y, double z,
                                  float yaw, float pitch, String gameMode) {
        this.inventoryData = inventoryData;
        this.armorData = armorData;
        this.offhandData = offhandData;
        this.enderChestData = enderChestData;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.gameMode = gameMode != null ? gameMode : "SURVIVAL";
    }
}
