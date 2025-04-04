package org.galliumpowered.permission;

import org.galliumpowered.Gallium;

import java.util.ArrayList;
import java.util.Optional;

public class GroupManager {
    private final ArrayList<Group> groups = new ArrayList<>();

    /**
     * Add a {@link Group}
     * This does NOT add it to the database
     * @param group the group
     */
    public void addGroup(Group group) {
        groups.add(group);
    }

    /**
     * Create a group and add it to the database
     * @param group the group
     */
    public void createGroup(Group group) {
        addGroup(group);
        Gallium.getDatabase().insertGroup(group);
    }

    /**
     * Remove a {@link Group}
     * @param group the group
     */
    public void removeGroup(Group group) {
        groups.remove(group);
    }

    /**
     * Get the groups
      * @return an ArrayList of the {@link Group}s
     */
    public ArrayList<Group> getGroups() {
        return groups;
    }

    /**
     * Get a group by its name
     * @param name the name of the group
     * @return the group
     */
    public Optional<Group> getGroupByName(String name) {
        return groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}
