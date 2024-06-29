package org.galliumpowered.permission;

import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Group {
    String name;
    ArrayList<String> permissions;
    String prefix;

    public Group(String name, ArrayList<String> permissions, String prefix) {
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
    public ArrayList<String> getPermissions() {
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
        Gallium.getDatabase().insertPermission(permission, new PermissionOwner(this));
    }

    /**
     * Removes a permission from the group
     * @param permission The permission to remove
     * @throws SQLException
     */
    public void removePermission(String permission) throws SQLException {
        Gallium.getDatabase().removePermission(permission, new PermissionOwner(this));
    }

    /**
     * Whether the group has a specific permission
     * @param permission The permission to check
     * @return True if the permission is present, otherwise false
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Sets the group's prefix. Supports colour coding with &
     * @param prefix The prefix to set it to
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
