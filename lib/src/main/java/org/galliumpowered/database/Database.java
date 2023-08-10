package org.galliumpowered.database;

import org.galliumpowered.Gallium;
import org.galliumpowered.chat.Colors;
import org.galliumpowered.world.entity.Player;
import org.galliumpowered.exceptions.GalliumDatabaseException;
import org.galliumpowered.permission.Group;
import org.galliumpowered.permission.GroupManager;
import org.galliumpowered.permission.PermissionOwner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

public class Database {
    private static final Logger log = LogManager.getLogger("Gallium/Database");
    private Connection conn;
    String connectionString;

    private PreparedStatement insertPlayer;
    private PreparedStatement getPlayer;
    private PreparedStatement insertGroup;
    private PreparedStatement setGroup;
    private PreparedStatement getGroups;
    private PreparedStatement getPlayerGroup;
    private PreparedStatement setPlayerPrefix;
    private PreparedStatement getPlayerPrefix;
    private PreparedStatement setGroupPrefix;
    private PreparedStatement getGroupPrefix;
    private PreparedStatement insertPermission;
    private PreparedStatement removePermisson;
    private PreparedStatement hasPermission;
    private PreparedStatement getPlayerPermisions;
    private PreparedStatement getPermsByOwner;
    public Database(String connectionString) {
        this.connectionString = connectionString;
    }

    public void open() throws SQLException {
        log.info("Opening database connection");
        conn = DriverManager.getConnection(connectionString);

        // Create tables if they don't exist
        PreparedStatement createPlayersTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS players (uuid MEDIUMTEXT, player_group TINYTEXT, prefix TINYTEXT)");
        PreparedStatement createGroupsTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS groups (name TINYTEXT, prefix TINYTEXT)");
        PreparedStatement createPermsTable = conn.prepareStatement("CREATE TABLE IF NOT EXISTS permissions (node TINYTEXT, owner TINYTEXT)");

        createPlayersTable.execute();
        createGroupsTable.execute();
        createPermsTable.execute();

        insertPlayer = conn.prepareStatement("INSERT INTO players (uuid) VALUES (?)");
        getPlayer = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?");
        insertGroup = conn.prepareStatement("INSERT INTO groups (name) VALUES (?)");
        setGroup = conn.prepareStatement("UPDATE players SET player_group = ? WHERE uuid = ?");
        getGroups = conn.prepareStatement("SELECT * FROM groups");
        getPlayerGroup = conn.prepareStatement("SELECT player_group FROM players WHERE uuid = ?");
        setPlayerPrefix = conn.prepareStatement("UPDATE players SET prefix = ? WHERE uuid = ?");
        getPlayerPrefix = conn.prepareStatement("SELECT prefix FROM players WHERE uuid = ?");
        setGroupPrefix = conn.prepareStatement("UPDATE groups SET prefix = ? WHERE name = ?");
        getGroupPrefix = conn.prepareStatement("SELECT prefix FROM groups where name = ?");
        insertPermission = conn.prepareStatement("INSERT INTO permissions (node, owner) VALUES (?, ?)");
        removePermisson = conn.prepareStatement("DELETE FROM permissions WHERE node = ? AND owner = ?");
        hasPermission = conn.prepareStatement("SELECT * FROM permissions WHERE (node = ? OR node = 'ALL') AND owner = ?");
        getPlayerPermisions = conn.prepareStatement("SELECT * FROM permissions WHERE owner = ?");
        getPermsByOwner = conn.prepareStatement("SELECT node FROM permissions WHERE owner = ?");
    }


    /**
     * Add a player to the database
     * @param player the player
     */
    public void insertPlayer(Player player) {
        try {
            insertPlayer.setString(1, player.getUUID());
            insertPlayer.execute();
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    /**
     * Insert a group
     * @param group the group
     */
    public void insertGroup(Group group) {
        try {
            insertGroup.setString(1, group.getName());
            insertGroup.execute();
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    /**
     * Check if a player is in the database
     * @param player the player
     * @return whether the player is in the database
     */

    public boolean playerExists(Player player) throws SQLException {
        getPlayer.setString(1, player.getUUID());
        ResultSet rs = getPlayer.executeQuery();
        return rs.next();
    }

    /**
     * Set a player's group
     * @param player the player
     * @param group the group
     * @throws SQLException
     */
    public void setPlayerGroup(Player player, @Nullable Group group) throws SQLException {
        if (group == null) {
            setGroup.setString(1, null);
        } else {
            setGroup.setString(1, group.getName());
        }
        setGroup.setString(2, player.getUUID());
        setGroup.execute();
    }

    /**
     * Get a player's group
     * @param player the player
     * @return player's group
     */
    public Optional<Group> getPlayerGroup(Player player) {
        log.debug("getting player group");
        try {
            getPlayerGroup.setString(1, player.getUUID());
            ResultSet rs = getPlayerGroup.executeQuery();
            if (rs.next()) {
                log.debug("a");
                String groupName = rs.getString("player_group");
                log.debug(groupName);
                return Gallium.getGroupManager().getGroupByName(groupName);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    public ArrayList<String> getPlayerPermissions(Player player) {
        try {
            ArrayList<String> permissions = new ArrayList();
            getPlayerPermisions.setString(1, player.getUUID());
            ResultSet rs = getPlayerPermisions.executeQuery();
            while (rs.next()) {
                permissions.add(rs.getString("node"));
            }
            return permissions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a group's prefix
     * @param group the group
     * @return the prefix
     */
    public Optional<String> getGroupPrefix(Group group) {
        try {
            getGroupPrefix.setString(1, group.getName());
            ResultSet rs = getGroupPrefix.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("prefix"));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    /**
     * Set a group's prefix
     * @param group the group
     * @param prefix the prefix
     * @throws SQLException
     */
    public void setGroupPrefix(Group group, String prefix) throws SQLException {
        setGroupPrefix.setString(2, group.getName());
        setGroupPrefix.setString(1, prefix);
        setGroupPrefix.execute();
    }

    /**
     * Get a player's prefix
     * If they are not in a group or don't have a custom prefix, it returns a white prefix
     * @param player the player
     * @return the prefix
     */
    // FIXME
    public String getPlayerPrefix(Player player) {
        try {
            getPlayerPrefix.setString(1, player.getUUID());
            ResultSet rs = getPlayerPrefix.executeQuery();
            if (rs.next()) {
                // Get from player
                return rs.getString("prefix");
            } else {
                // Get from group
                Optional<Group> groupOptional = getPlayerGroup(player);
                if (groupOptional.isPresent()) {
                    Optional<String> groupPrefixOptional = getGroupPrefix(groupOptional.get());
                    if (groupPrefixOptional.isPresent()) {
                        return groupPrefixOptional.get();
                    }
                }
            }
            return Colors.WHITE;
        } catch (SQLException e) {
            throw new GalliumDatabaseException(e);
        }
    }

    /**
     * Set a player's prefix
     * @param player the player
     * @param prefix the prefix
     * @throws SQLException
     */
    public void setPlayerPrefix(Player player, String prefix) throws SQLException {
        setPlayerPrefix.setString(2, prefix);
        setPlayerPrefix.setString(1, player.getUUID());
        setPlayerPrefix.execute();
    }

    /**
     * Set a group or player permission
     * @param permission the permission to add
     * @param owner group or player permission owner
     * @throws SQLException
     */
    public void insertPermission(String permission, PermissionOwner owner) throws SQLException {
        insertPermission.setString(1, permission);
        insertPermission.setString(2, owner.getName());
        insertPermission.execute();
    }

    /**
     * Set a group player or permission
     * @param permission the permission to remove
     * @param owner group or player permission owner
     * @throws SQLException
     */
    public void removePermission(String permission, PermissionOwner owner) throws SQLException {
        removePermisson.setString(1, permission);
        removePermisson.setString(2, owner.getName());
        removePermisson.execute();
    }

    /**
     * Whether a player has a permission
     * @param permission the permission
     * @param player the player
     * @return whether the player has the specified permission
     * @throws SQLException
     */
    public boolean playerHasPermission(String permission, Player player) throws SQLException {
        log.debug("called playerHasPermission (Database)");
        hasPermission.setString(1, permission);
        hasPermission.setString(2, player.getUUID());
        ResultSet rs = hasPermission.executeQuery();
        return rs.next();
    }

    /**
     * Get an owner's permissions
     * @param owner owner name
     * @return ArrayList of permission nodes
     * @throws SQLException
     */
    public ArrayList<String> getOwnerPermisions(String owner) throws SQLException {
        ArrayList<String> permissions = new ArrayList<>();
        getPermsByOwner.setString(1, owner);
        ResultSet rs = getPermsByOwner.executeQuery();
        while (rs.next()) {
            permissions.add(rs.getString("node"));
        }
        return permissions;
    }

    /**
     * Add {@link Group}s to a {@link GroupManager}
     * @param manager the {@link GroupManager}
     * @throws SQLException
     */
    public void addGroupsToGroupManager(GroupManager manager) throws SQLException {
        ResultSet rs = getGroups.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            String prefix = rs.getString("prefix");
            ArrayList<String> permissions = getOwnerPermisions(name);
            manager.addGroup(new Group(name, permissions, prefix));
        }
    }
}
