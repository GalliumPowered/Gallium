package org.galliumpowered.permission;

import org.galliumpowered.world.entity.Player;

public interface PermissionOwner {
    /**
     * Name of the permission owner
     * @return Permission owner name
     */
    String getName();

    /**
     * UUID of permission owner
     * @return Permission owner UUID
     */
    String getUUID();
}
