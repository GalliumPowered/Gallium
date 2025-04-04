package org.galliumpowered.world.entity;

import net.kyori.adventure.text.Component;
import org.galliumpowered.Gamemode;
import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.command.CommandCaller;
import org.galliumpowered.pagination.PaginationListAudience;
import org.galliumpowered.permission.Group;
import org.galliumpowered.permission.PermissionHolder;
import org.galliumpowered.world.World;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public interface Player extends Entity, CommandCaller, PermissionHolder, PaginationListAudience {
    /**
     * Get the player's UUID
     * @return the uuid
     */
    UUID getUUID();

    /**
     * Whether the player has a permission
     * @param permission the permission
     * @return whether the player has the permission
     */
    default boolean hasPermission(String permission) {
        return Gallium.getPermissionManager().playerHasPermission(this, permission);
    }

    /**
     * Get the players permissions
     * @return {@link ArrayList} of permissions
     */
    default ArrayList<String> getPermissions() {
        return Gallium.getPermissionManager().getPlayerPermissions(this);
    }

    /**
     * Sends a message to the player
     * @param component A component
     */
    void sendMessage(Component component);

    /**
     * Disconnect the player
     * @param component The reason for disconnecting them
     */
    void disconnect(Component component);

    /**
     * Disconnect the player
     */
    void disconnect();

    /**
     * Get the player's prefix
     * @return the prefix
     */
    default String getPrefix() {
        String prefix = Gallium.getDatabase().getPlayerPrefix(this);
        return Objects.requireNonNullElse(prefix, Colors.WHITE);
    }

    /**
     * Get the player's group
     * @return the {@link Group}
     */
    default Optional<Group> getGroup() {
        return Gallium.getDatabase().getPlayerGroup(this);
    }

    /**
     * Set a player's group
     * @param group the {@link Group}
     * @throws SQLException
     */
    default void setGroup(Group group) throws SQLException {
        Gallium.getDatabase().setPlayerGroup(this, group);
    }

    /**
     * Add a permission to the player
     * @param permission the permission node
     * @throws SQLException
     */
    default void addPermission(String permission) throws SQLException {
        Gallium.getDatabase().insertPermission(permission, this);
    }

    /**
     * Remove a permission from the player
     * @param permission the permission node
     * @throws SQLException
     */
    default void removePermission(String permission) throws SQLException {
        Gallium.getDatabase().removePermission(permission, this);
    }

    /**
     * Ungroup the player
     * @throws SQLException
     */
    default void ungroup() throws SQLException {
        Gallium.getDatabase().setPlayerGroup(this, null);
    }

    /**
     * Set the player's prefix
     * @param prefix the prefix
     * @throws SQLException
     */
    default void setPrefix(String prefix) throws SQLException {
        Gallium.getDatabase().setPlayerPrefix(this, prefix);
    }

    /**
     * Teleport the player
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     */
    void teleport(double x, double y, double z);

    /**
     * Set the player's {@link Gamemode}
     * @param gamemode The {@link Gamemode}
     */
    void setGamemode(Gamemode gamemode);

    /**
     * Kill the player
     */
    void kill();

    /**
     * Gets the player's current {@link World}
     * @return Player world
     */
    World getWorld();

    /**
     * Get whether this player is online
     *
     * @return Player online status
     */
    boolean isOnline();

    /**
     * Get whether this player is a server operator
     *
     * @return Server operator status
     */
    boolean isOperator();
}
