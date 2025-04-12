package com.github.ynverxe.hexserver.luckperms;

import com.github.ynverxe.hexserver.HexServer;
import com.github.ynverxe.hexserver.extension.HexExtension;
import com.github.ynverxe.hexserver.luckperms.config.ConfigurateConfigAdapter;
import me.lucko.luckperms.minestom.CommandRegistry;
import me.lucko.luckperms.minestom.LuckPermsMinestom;
import net.luckperms.api.LuckPerms;
import net.minestom.server.ServerProcess;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Paths;

public class LuckPermsExtension extends HexExtension {

  private @Nullable LuckPerms luckPerms;

  public LuckPermsExtension() throws IOException {
  }

  @Override
  public void initialize() {
    try {
      initResourceIfMissing("luckperms.conf", "luckperms.conf");

      ServerProcess process = HexServer.instance().process();
      this.luckPerms = LuckPermsMinestom.builder(this.getDataDirectory().resolve("LuckPerms"))
          .commandRegistry(CommandRegistry.of(process.command()::register, process.command()::unregister))
          .configurationAdapter(lpMinestomPlugin -> new ConfigurateConfigAdapter(lpMinestomPlugin, Paths.get("LuckPerms/luckperms.conf")))
          .dependencyManager(true)
          .enable();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void terminate() {
    LuckPermsMinestom.disable();
  }

  public @Nullable LuckPerms luckPerms() {
    return luckPerms;
  }
}