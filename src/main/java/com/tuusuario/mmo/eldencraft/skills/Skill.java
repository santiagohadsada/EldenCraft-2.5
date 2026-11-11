package com.tuusuario.mmo.eldencraft.skills;

import com.tuusuario.mmo.eldencraft.models.PlayerData;
import com.tuusuario.mmo.eldencraft.util.SkillDescriptions;
import org.bukkit.entity.Player;

public interface Skill {
    String getName();            // como se llama la habilidad
    double getCost();            // cuanto mana o energia gasta
    int getLevelRequired();      // nivel minimo pa usarla (1, 3, 6, 9, 12, 16, 20)

    public enum CastType { INSTANT, CHARGEABLE, MUST_CHARGE }

    default CastType getCastType() { return CastType.INSTANT; }
    default int getMaxChargeTicks() { return 60; } // 3 segundos por defecto

    void execute(Player player, PlayerData data); // lo q hace cuando la usas

    default void execute(Player player, PlayerData data, double chargePercent) {
        execute(player, data); // Fallback por defecto para habilidades no actualizadas
    }

    // cooldown en segundos, si retorna 0 usa el default q esta en AbilityListener
    default int getCooldown() { return 0; }

    // descripcion pa el menu de /skills, la saca de SkillDescriptions
    default String getDescription() {
        return SkillDescriptions.get(this);
    }
}
