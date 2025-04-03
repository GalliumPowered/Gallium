package org.galliumpowered.permission;

import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Group implements PermissionHolder {
    private final String name;
    private final List<String> permissions;
    String prefix;

    public Group(String name, List<String> permissions, String prefix) {
        this.name = name;
        this.permissions = permissions;
        this.prefix = prefix;
    }

    /**
     * The name of the group
     * @return Group name
     */
    public String getName() {
        return name;
    }

    /**
     * The permissions the group holds
     * @return Permisisons of the group
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Prefix of the group
     * @return Group prefix
     */
    public String getPrefix() {
        return Objects.requireNonNullElse(prefix, Colors.WHITE);
    }

    /**
     * Adds a permission to the group
     * @param permission The permission to add
     * @throws SQLException
     */
    public void addPermission(String permission) throws SQLException {
        Gallium.getDatabase().insertPermission(permission, this);
    }

    /**
     * Removes a permission from the group
     * @param permission The permission to remove
     * @throws SQLException
     */
    public void removePermission(String permission) throws SQLException {
        Gallium.getDatabase().removePermission(permission, this);
    }

    /**
     * Whether the group has a specific permission
     * @param permission The permission to check
     * @return True if the permission is present, otherwise false
     */
    @Override
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Sets the group's prefix. Supports colour coding with {@literal &}
     * @param prefix The prefix to set it to
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
