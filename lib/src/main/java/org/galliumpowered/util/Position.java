package org.galliumpowered.util;

/**
 * A position, holding x, y, and z coordinates
 */
public class Position {
    private final double x;
    private final double y;
    private final double z;

    private Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * The {@link Position} factory
     *
     * @param x The X coordinate of the position
     * @param y The Y coordinate of the position
     * @param z The Z coordinate of the position
     * @return A position
     */
    public static Position of(double x, double y, double z) {
        return new Position(x, y, z);
    }

    /**
     * Get the X coordinate of this position
     *
     * @return X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Get the Y coordinate of this position
     *
     * @return Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Get the Z coordinate of this position
     *
     * @return Z coordinate
     */
    public double getZ() {
        return z;
    }
}
