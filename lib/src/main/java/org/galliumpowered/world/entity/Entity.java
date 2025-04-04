package org.galliumpowered.world.entity;

import org.galliumpowered.world.World;

public interface Entity {

    /**
     * The name of the entity
     * @return The name of the entity
     */
    String getName();

    /**
     * Kill the entity
     */
    void kill();

    /**
     * Get the world this entity is in
     *
     * @return Entity world
     */
    World getWorld();

    /**
     * Teleport an entity to a specific set of coordinates
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param z The Z coordinate
     */
    void teleport(double x, double y, double z);
}
