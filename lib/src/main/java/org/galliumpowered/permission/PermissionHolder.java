package org.galliumpowered.permission;


import java.util.List;

public interface PermissionHolder {
    /**
     * The name of the permission owner
     * @return Permission owner name
     */
    String getName();

    /**
     * Check if this permission holder has a permission
     *
     * @param permissionNode Permission to check
     * @return If the holder has the permission
     */
    boolean hasPermission(String permissionNode);

    /**
     * Get a list of permissions this permission holder has
     *
     * @return Permission list
     */
    List<String> getPermissions();
}
