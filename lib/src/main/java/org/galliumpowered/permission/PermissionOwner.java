package org.galliumpowered.permission;

import org.galliumpowered.world.entity.Player;

import java.util.ArrayList;

public interface PermissionOwner {
    ArrayList<PermissionNode> permissions = new ArrayList<>();
    /**
     * Name of the permission owner
     * @return Permission owner name
     */
    String getName();

    /**
     * Get an {@link ArrayList} of permission nodes that this owner has
     * @return Permission nodes.
     */
    default ArrayList<PermissionNode> getPermissions() {
        return permissions;
    }
}
