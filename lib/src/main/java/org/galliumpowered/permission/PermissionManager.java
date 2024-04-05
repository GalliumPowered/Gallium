package org.galliumpowered.permission;

import org.galliumpowered.Gallium;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.exceptions.GalliumDatabaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class PermissionManager {
    private static final Logger log = LogManager.getLogger("Gallium/PluginManager");
    private ArrayList<PermissionNode> permissions;
    public PermissionManager() {
        this.permissions = new ArrayList<>();
    }

    /**
     * Check if a player has a permission
     * @param player the player
     * @param permission permission node
     * @return whether the player has the permission
     */
    public boolean playerHasPermission(Player player, String permission) {
        log.debug("Called playerHasPermission");
        if (Objects.equals(permission, "NONE")) return true;
        try {
            // Check against player
            log.debug("Checking against player...");
            log.debug(Gallium.getDatabase().playerHasPermission(permission, player)); //???
            if (Gallium.getDatabase().playerHasPermission(permission, player)) {
                log.debug("Player has");
                return true;
            }

            // Check against groups
            log.debug("Checking against groups...");
            Optional<Group> group = Gallium.getDatabase().getPlayerGroup(player);
            return group.map(g -> g.getPermissions().contains(permission)).orElse(false);
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    /**
     * Get the prefix of a player
     * @param player the player
     * @return the player's prefix
     */
    public String getPlayerPrefix(Player player) {
        return Gallium.getDatabase().getPlayerPrefix(player);
    }

    /**
     * Get the player's group
     * @param player the player
     * @return the player's group
     */
    public Optional<Group> getPlayerGroup(Player player) {
        return Gallium.getDatabase().getPlayerGroup(player);
    }

    public ArrayList<String> getPlayerPermissions(Player player) {
        return Gallium.getDatabase().getPlayerPermissions(player);
    }

    /**
     * Returns an {@link ArrayList} of all permission nodes
     * @return All registered permission nodes
     */
    public ArrayList<PermissionNode> getPermissions() {
        return permissions;
    }

    /**
     * Get a {@link PermissionNode} from its path
     * @param path The path of the permission node you are trying to find - i.e. myplugin.myfeature.mycommand
     * @return Permission node with the respective path
     */
    public PermissionNode findPermissionByPath(String path) {
        for (PermissionNode permission : permissions) {
            if (permission.getPath().equals(path)) {
                return permission;
            }
        }
        return null;
    }
}
