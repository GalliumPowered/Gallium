package org.galliumpowered.permission;

import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Group implements PermissionOwner {
    String name;
    ArrayList<String> permissions;
    String prefix;

    public Group(String name, ArrayList<String> permissions, String prefix) {
        this.name = name;
        this.permissions = permissions;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getUUID() {
        return getName();
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public String getPrefix() {
        return Objects.requireNonNullElse(prefix, Colors.WHITE);
    }

    public void addPermission(String permission) throws SQLException {
        Gallium.getDatabase().insertPermission(permission, this);
    }

    public void removePermission(String permission) throws SQLException {
        Gallium.getDatabase().removePermission(permission, this);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
