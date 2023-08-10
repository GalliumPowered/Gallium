package org.galliumpowered.world.block;

public interface WorldBlock extends Block {
    /**
     * Get the X coordinate of the block
     * @return X coordinate
     */
    int getX();

    /**
     * Get the Y coordinate of the block
     * @return Y coordinate
     */
    int getY();

    /**
     * Get the Z coordinate of the block
     * @return Z coordinate
     */
    int getZ();
}
