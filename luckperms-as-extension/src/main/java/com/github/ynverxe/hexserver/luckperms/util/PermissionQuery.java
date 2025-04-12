package com.github.ynverxe.hexserver.luckperms.util;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.ServerSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PermissionQuery {
  
  private final @NotNull String permission;
  private final @NotNull Supplier<@Nullable QueryOptions> optionsSupplier;

  public PermissionQuery(@NotNull String permission, @NotNull Supplier<@Nullable QueryOptions> optionsSupplier) {
    this.permission = Objects.requireNonNull(permission);
    this.optionsSupplier = Objects.requireNonNull(optionsSupplier);
  }

  public PermissionQuery(@NotNull String permission) {
    this(permission, QueryOptions::defaultContextualOptions);
  }

  public @NotNull Supplier<@Nullable QueryOptions> optionsSupplier() {
    return optionsSupplier;
  }

  public @NotNull PermissionQuery optionsSupplier(@NotNull Supplier<@Nullable QueryOptions> optionsSupplier) {
    return new PermissionQuery(this.permission, optionsSupplier);
  }

  public @NotNull Tristate query(@NotNull CommandSender sender) {
    if (sender instanceof ConsoleSender || sender instanceof ServerSender) {
      return Tristate.TRUE;
    }

    if (!(sender instanceof Player player))
      return Tristate.UNDEFINED;

    return this.queryUser(player.getUuid());
  }

  public @NotNull Tristate queryGroup(@NotNull String name) {
    Group group = LuckPermsProvider.get()
        .getGroupManager()
        .getGroup(name);

    if (group == null)
      return Tristate.UNDEFINED;

    return this.query(group);
  }

  public @NotNull Tristate queryUser(@NotNull UUID userUuid) {
    User user = LuckPermsProvider.get()
        .getUserManager()
        .getUser(userUuid);

    if (user == null)
      return Tristate.UNDEFINED;

    return this.query(user);
  }

  public @NotNull Tristate query(@NotNull PermissionHolder holder) {
    QueryOptions options = this.optionsSupplier.get();

    if (options == null) {
      options = QueryOptions.nonContextual();
    }

    return holder.getCachedData().getPermissionData(options).checkPermission(permission);
  }
}