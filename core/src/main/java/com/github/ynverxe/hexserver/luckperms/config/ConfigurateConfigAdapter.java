package com.github.ynverxe.hexserver.luckperms.config;

import com.github.ynverxe.configuratehelper.handler.FastConfiguration;
import com.github.ynverxe.configuratehelper.handler.content.ContentChannel;
import com.github.ynverxe.hexserver.HexServer;
import io.leangen.geantyref.TypeToken;
import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter;
import me.lucko.luckperms.common.plugin.LuckPermsPlugin;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigurateConfigAdapter implements ConfigurationAdapter {

  private final LuckPermsPlugin plugin;
  private final FastConfiguration configuration;

  public ConfigurateConfigAdapter(LuckPermsPlugin plugin, Path path) {
    this.plugin = plugin;
    try {
      this.configuration = HexServer.instance().configurationFactory()
          .toBuilder()
          .configurationLoaderFactory(HoconConfigurationLoader::builder)
          .build()
          .create(null, path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public LuckPermsPlugin getPlugin() {
    return plugin;
  }

  @Override
  public void reload() {
    this.configuration.load();
  }

  @Override
  public String getString(String s, String s1) {
    return Objects.requireNonNullElse(configuration.node().node(resolve(s)).getString(), s1 != null ? s1 : "");
  }

  @Override
  public int getInteger(String s, int i) {
    return Objects.requireNonNullElse(configuration.node().node(resolve(s)).getInt(), i);
  }

  @Override
  public boolean getBoolean(String s, boolean b) {
    return Objects.requireNonNullElse(configuration.node().node(resolve(s)).getBoolean(), b);
  }

  @Override
  public List<String> getStringList(String s, List<String> list) {
    try {
      return Objects.requireNonNullElse(configuration.node().node(resolve(s)).getList(String.class), list);
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<String, String> getStringMap(String s, Map<String, String> map) {
    try {
      return Objects.requireNonNullElse(configuration.node().node(resolve(s)).get(new TypeToken<Map<String, String>>() {}), map);
    } catch (SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  private String[] resolve(String path) {
    return path.split(".//");
  }
}