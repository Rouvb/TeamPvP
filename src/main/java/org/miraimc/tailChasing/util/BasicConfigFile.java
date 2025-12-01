package org.miraimc.tailChasing.util;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class BasicConfigFile {

    private final JavaPlugin plugin;
    private final String fileName;
    private final boolean autoUpdate;
    private YamlConfiguration configuration;
    private final File file;

    public BasicConfigFile(JavaPlugin plugin, String fileName, boolean autoUpdate) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
        this.autoUpdate = autoUpdate;
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!this.file.exists()) {
            plugin.saveResource(fileName, false);
        }
        load();
    }

    public boolean getBoolean(String path) {
        return configuration.getBoolean(path, false);
    }

    public double getDouble(String path) {
        return configuration.getDouble(path, 0.0D);
    }

    public int getInt(String path) {
        return configuration.getInt(path, 0);
    }

    public String getRawString(String path) {
        return configuration.getString(path, "");
    }

    public List<String> getRawStringList(String path) {
        return configuration.getStringList(path);
    }

    public String getString(String path) {
        String value = configuration.getString(path);
        return value != null ? TextUtil.translate(value) : "";
    }

    public List<String> getStringList(String path) {
        List<String> list = configuration.getStringList(path);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(TextUtil::translate)
                .collect(Collectors.toList());
    }

    public List<?> getList(String path) {
        return configuration.getList(path, Collections.emptyList());
    }

    public void set(String path, Object value) {
        configuration.set(path, value);
        save();
    }

    public void addToList(String path, String value) {
        List<String> list = configuration.getStringList(path);
        list.add(value);
        configuration.set(path, list);
        save();
    }

    public void removeFromList(String path, int index) {
        List<String> list = configuration.getStringList(path);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (index < 0 || index >= list.size()) {
            return;
        }

        list.remove(index);
        configuration.set(path, list);
        save();
    }

    public void remove(String path) {
        configuration.set(path, null);
    }

    public void load() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);

        try (InputStream defConfigStream = plugin.getResource(fileName)) {
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                configuration.setDefaults(defConfig);

                if (autoUpdate) {
                    configuration.options().copyDefaults(true);
                    save();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load default config for " + fileName);
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config file: " + fileName);
            e.printStackTrace();
        }
    }

    public void reload() {
        load();
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return configuration.getConfigurationSection(path);
    }
}
