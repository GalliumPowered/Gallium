package org.galliumpowered.world.entity;

import org.galliumpowered.util.Position;
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
    @Deprecated(since = "1.2.0")
    void teleport(double x, double y, double z);

    /**
     * Set this entity's position
     *
     * @param position The position to teleport this entity to
     */
    void setPosition(Position position);

    /**
     * Get this entity's position
     *
     * @return Entity position
     */
    Position getPosition();
}
