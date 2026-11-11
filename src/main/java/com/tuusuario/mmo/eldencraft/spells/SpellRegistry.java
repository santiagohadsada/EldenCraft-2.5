package com.tuusuario.mmo.eldencraft.spells;

import com.tuusuario.mmo.eldencraft.models.ClassType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro en memoria de todos los hechizos disponibles.
 */
public class SpellRegistry {

    private static SpellRegistry instance;

    private final Map<String, SpellDefinition> spells = new HashMap<>();

    public static synchronized SpellRegistry getInstance() {
        if (instance == null) {
            instance = new SpellRegistry();
        }
        return instance;
    }

    public void register(SpellDefinition spell) {
        if (spell != null && spell.getId() != null) {
            spells.put(spell.getId().toLowerCase(), spell);
        }
    }

    public SpellDefinition get(String id) {
        if (id == null) return null;
        return spells.get(id.toLowerCase());
    }

    public Collection<SpellDefinition> getAll() {
        return Collections.unmodifiableCollection(spells.values());
    }

    /**
     * Devuelve todos los hechizos accesibles para una clase.
     */
    public List<SpellDefinition> getForClass(ClassType cls) {
        if (cls == null) return Collections.emptyList();
        String key = cls.spellKey();
        List<SpellDefinition> result = new ArrayList<>();
        for (SpellDefinition spell : spells.values()) {
            if (spell.getClases().contains(key) || spell.getClases().contains(cls.name().toLowerCase())) {
                result.add(spell);
            }
        }
        return result;
    }

    public void clear() {
        spells.clear();
    }

    public int count() {
        return spells.size();
    }
}
