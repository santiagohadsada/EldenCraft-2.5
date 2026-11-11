package com.tuusuario.mmo.eldencraft.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utilidad de mensajes: carga messages.yml y ofrece métodos para obtener y enviar mensajes.
 * Soporta reemplazos de {placeholder} y colores con &.
 */
public class MessageUtil {

    private static FileConfiguration messages;
    private static String prefix = "&6[EldenCraft]&r ";
    private static com.tuusuario.mmo.eldencraft.EldenCraft plugin;

    private MessageUtil() {}

    /**
     * Inicializa el sistema de mensajes. Llamar en onEnable() antes de usar.
     */
    public static void init(com.tuusuario.mmo.eldencraft.EldenCraft pl) {
        plugin = pl;
        reload();
    }

    /**
     * Recarga messages.yml desde disco.
     */
    public static void reload() {
        if (plugin == null) return;
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        // Merge con defaults del jar
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messages.setDefaults(defConfig);
        }

        prefix = color(messages.getString("prefix", "&6[EldenCraft]&r "));
    }

    /**
     * Obtiene un mensaje localizado con colores aplicados.
     */
    public static String get(String key) {
        if (messages == null) return key;
        String raw = messages.getString(key, "&c[MSG faltante: " + key + "]");
        return color(raw);
    }

    /**
     * Obtiene un mensaje con reemplazos de {placeholder}.
     */
    public static String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    /**
     * Envía un mensaje a un CommandSender (sin prefix).
     */
    public static void send(CommandSender sender, String key) {
        if (sender != null) sender.sendMessage(get(key));
    }

    /**
     * Envía un mensaje a un CommandSender con reemplazos.
     */
    public static void send(CommandSender sender, String key, Map<String, String> placeholders) {
        if (sender != null) sender.sendMessage(get(key, placeholders));
    }

    /**
     * Envía un mensaje prefijado a un CommandSender.
     */
    public static void sendPrefixed(CommandSender sender, String key) {
        if (sender != null) sender.sendMessage(prefix + get(key));
    }

    /**
     * Envía un mensaje prefijado con reemplazos.
     */
    public static void sendPrefixed(CommandSender sender, String key, Map<String, String> placeholders) {
        if (sender != null) sender.sendMessage(prefix + get(key, placeholders));
    }

    /**
     * Convierte códigos de color & en colores de chat.
     */
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getPrefix() { return prefix; }
}
