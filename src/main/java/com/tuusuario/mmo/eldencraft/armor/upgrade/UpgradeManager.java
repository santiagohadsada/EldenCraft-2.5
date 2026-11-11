package com.tuusuario.mmo.eldencraft.armor.upgrade;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorRarity;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorSlot;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorStat;
import com.tuusuario.mmo.eldencraft.armor.armor.ArmorType;
import com.tuusuario.mmo.eldencraft.armor.armor.RpgArmor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

// Mejora de armaduras en el yunque.
//
// Reglas:
//  - acepta tanto piezas RPG como armaduras vanilla normales: si pones
//    una vanilla, se promociona a RPG +1 (rareza COMUN, sin stats extra)
//  - el material BASE de la pieza no cambia nunca
//  - el ingrediente del slot derecho puede ser cuero, lingote de hierro,
//    oro, diamante, lingote de netherite o malla; tambien vale meter una
//    pieza completa del material que sea
//  - el refuerzo se guarda y aporta stats segun la diferencia de tier
//    (ver RpgArmor.reinforcementBonusPerLevel)
//  - max +5 niveles
public final class UpgradeManager {

    private final EldenCraft plugin;

    public UpgradeManager(EldenCraft plugin) {
        this.plugin = plugin;
    }
// devuelve 
    // no es un material valido como refuerzo.
    public ArmorType detectReinforcement(ItemStack ingredient) {
        if (ingredient == null || ingredient.getType() == Material.AIR) return null;
        ArmorType viaMaterial = ArmorType.fromMaterial(ingredient.getType());
        if (viaMaterial != null) return viaMaterial;
        return ArmorType.fromIngredient(ingredient.getType());
    }

    public boolean canUpgrade(ItemStack base, ItemStack ingredient) {
        if (base == null || base.getType() == Material.AIR) return false;
        // que sea armadura (RPG o vanilla)
        if (ArmorSlot.fromMaterial(base.getType()) == null) return false;
        if (detectReinforcement(ingredient) == null) return false;

        int currentLevel = currentUpgradeLevel(base);
        return currentLevel < plugin.armorConfig().maxUpgradeLevel();
    }

    private int currentUpgradeLevel(ItemStack base) {
        if (RpgArmor.isRpgArmor(base)) {
            RpgArmor a = RpgArmor.read(base);
            return a == null ? 0 : a.upgradeLevel();
        }
        return 0; // vanilla parte de +0
    }

    public ItemStack upgrade(ItemStack base, ItemStack ingredient) {
        ArmorType reinf = detectReinforcement(ingredient);
        if (reinf == null) return null;

        ArmorSlot slot = ArmorSlot.fromMaterial(base.getType());
        if (slot == null) return null;

        ArmorType type = ArmorType.fromMaterial(base.getType());
        if (type == null) return null;

        int max = plugin.armorConfig().maxUpgradeLevel();

        RpgArmor old = RpgArmor.isRpgArmor(base) ? RpgArmor.read(base) : null;
        int currentLevel = old == null ? 0 : old.upgradeLevel();
        int next = Math.min(max, currentLevel + 1);

        List<ArmorStat> stats = old == null ? new ArrayList<>() : new ArrayList<>(old.stats());
        String customName  = old == null ? null : old.customName();
        ArmorRarity rarity = old == null ? ArmorRarity.COMUN : old.rarity();

        RpgArmor upgraded = new RpgArmor(type, slot, rarity, next, stats, customName, reinf);

        ItemStack copy = base.clone();
        upgraded.apply(copy);
        return copy;
    }

    // Coste progresivo. Ademas penalizo (cobro mas) si la pieza esta
    // sobreforjada, para que no sea trivial sobrepotenciar cuero+netherite.
    public int experienceCost(ItemStack base, ItemStack ingredient) {
        ArmorType type = ArmorType.fromMaterial(base.getType());
        if (type == null) return 1;

        int currentLevel = currentUpgradeLevel(base);
        int baseCost = plugin.armorConfig().upgradeBaseCost();
        int stepCost = plugin.armorConfig().upgradeStepCost();
        int cost = baseCost + (currentLevel + 1) * stepCost;

        ArmorType reinf = detectReinforcement(ingredient);
        if (reinf != null) {
            int diff = reinf.tier() - type.tier();
            if (diff >= 3) {
                cost *= 2;            // sobrepotenciacion
            } else if (diff <= -2) {
                cost += 2;            // refuerzo de tier muy inferior
            }
        }
        return Math.max(1, cost);
    }
}
