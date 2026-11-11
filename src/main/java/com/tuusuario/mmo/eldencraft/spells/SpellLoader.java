package com.tuusuario.mmo.eldencraft.spells;

import com.tuusuario.mmo.eldencraft.EldenCraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cargador de archivos YAML de hechizos (SPEC 5.1 / SPEC 3.2).
 * Carga desde plugins/EldenCraft/spells/ y desde recursos del jar.
 */
public class SpellLoader {

    private final EldenCraft plugin;

    public SpellLoader(EldenCraft plugin) {
        this.plugin = plugin;
    }

    /**
     * Carga todos los hechizos disponibles en el plugin.
     */
    public void loadAll() {
        File spellsDir = new File(plugin.getDataFolder(), "spells");
        if (!spellsDir.exists()) {
            spellsDir.mkdirs();
        }

        // Guardar archivos por defecto si no existen
        saveDefaultSpellFile("spells/nivel_0.yml");
        saveDefaultSpellFile("spells/nivel_1.yml");
        saveDefaultSpellFile("spells/nivel_3.yml");

        int loadedCount = 0;

        // Cargar desde carpeta física
        File[] files = spellsDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null && files.length > 0) {
            for (File f : files) {
                loadedCount += loadFile(f);
            }
        }

        // Carpeta custom
        File customDir = new File(spellsDir, "custom");
        if (!customDir.exists()) customDir.mkdirs();
        File[] customFiles = customDir.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (customFiles != null) {
            for (File f : customFiles) {
                loadedCount += loadFile(f);
            }
        }

        // Si la carpeta estaba vacía o no cargó nada, cargar directamente desde los recursos del jar
        if (SpellRegistry.getInstance().count() == 0) {
            loadedCount += loadResourceFile("spells/nivel_0.yml");
            loadedCount += loadResourceFile("spells/nivel_1.yml");
            loadedCount += loadResourceFile("spells/nivel_3.yml");
        }

        plugin.getLogger().info("[SpellLoader] " + SpellRegistry.getInstance().count() + " hechizos cargados en total.");
    }

    private int loadFile(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            return parseConfig(config, file.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("[SpellLoader] Error al cargar archivo " + file.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    private int loadResourceFile(String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return 0;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            return parseConfig(config, resourcePath);
        } catch (Exception e) {
            plugin.getLogger().warning("[SpellLoader] Error al leer recurso " + resourcePath + ": " + e.getMessage());
            return 0;
        }
    }

    private int parseConfig(YamlConfiguration config, String fileName) {
        int count = 0;
        for (String key : config.getKeys(false)) {
            ConfigurationSection sec = config.getConfigurationSection(key);
            if (sec == null) continue;

            try {
                SpellDefinition spell = parseSpell(key, sec, fileName);
                if (spell != null) {
                    SpellRegistry.getInstance().register(spell);
                    count++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[SpellLoader] [" + fileName + "] Error en hechizo '" + key + "': " + e.getMessage());
            }
        }
        return count;
    }

    private SpellDefinition parseSpell(String id, ConfigurationSection sec, String fileName) {
        // Validar campos obligatorios
        String nombre = sec.getString("nombre");
        if (nombre == null) {
            plugin.getLogger().warning("[SpellLoader] [" + fileName + "] Hechizo '" + id + "' omitido: falta 'nombre'.");
            return null;
        }

        SpellDefinition s = new SpellDefinition();
        s.setId(id.toLowerCase());
        s.setNombre(nombre);
        s.setNivel(sec.getInt("nivel", 0));
        s.setEscuela(sec.getString("escuela", "evocacion"));
        s.setClases(sec.getStringList("clases"));
        s.setTiempoLanzamiento(sec.getString("tiempo_lanzamiento", "accion"));
        s.setRitual(sec.getBoolean("ritual", false));
        s.setAlcance(sec.getString("alcance", "propio"));
        s.setComponentes(sec.getStringList("componentes"));
        s.setDuracion(sec.getString("duracion", "instantaneo"));
        s.setConcentracion(sec.getBoolean("concentracion", false));
        s.setObjetivo(sec.getString("objetivo", "criatura"));
        s.setMejoraNivel(sec.getString("mejora_nivel"));
        s.setMejoraPersonaje(sec.getString("mejora_personaje"));
        s.setCustom(sec.getBoolean("custom", false));
        s.setFuente(sec.getString("fuente", "SRD"));

        // Area si existe
        if (sec.isConfigurationSection("area")) {
            ConfigurationSection asec = sec.getConfigurationSection("area");
            AreaDefinition area = new AreaDefinition(asec.getString("forma", "esfera"), asec.getInt("tamano_pies", 20));
            s.setArea(area);
        }

        // Efectos
        List<Map<?, ?>> efectosRaw = sec.getMapList("efectos");
        List<EffectDefinition> efectos = new ArrayList<>();
        for (Map<?, ?> raw : efectosRaw) {
            EffectDefinition eff = new EffectDefinition();
            if (raw.containsKey("tipo")) eff.setTipo(String.valueOf(raw.get("tipo")));
            if (raw.containsKey("dados")) eff.setDados(String.valueOf(raw.get("dados")));
            if (raw.containsKey("tipo_dano")) eff.setTipoDano(String.valueOf(raw.get("tipo_dano")));
            if (raw.containsKey("salvacion_mitad")) eff.setSalvacionMitad(Boolean.parseBoolean(String.valueOf(raw.get("salvacion_mitad"))));
            if (raw.containsKey("salvacion")) eff.setSalvacion(String.valueOf(raw.get("salvacion")));
            if (raw.containsKey("condicion")) eff.setCondicion(String.valueOf(raw.get("condicion")));
            if (raw.containsKey("duracion")) eff.setDuracion(String.valueOf(raw.get("duracion")));
            if (raw.containsKey("mejora_nivel")) eff.setMejoraNivel(String.valueOf(raw.get("mejora_nivel")));
            if (raw.containsKey("mejora_personaje")) eff.setMejoraPersonaje(String.valueOf(raw.get("mejora_personaje")));
            efectos.add(eff);
        }
        s.setEfectos(efectos);

        return s;
    }

    private void saveDefaultSpellFile(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            try {
                plugin.saveResource(path, false);
            } catch (Exception ignored) {}
        }
    }
}
