package org.galliumpowered.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class WorldBlockImpl implements WorldBlock {
    private Block nmsBlock;
    private BlockPos pos;

    public WorldBlockImpl(Block nmsBlock, BlockPos pos) {
        this.nmsBlock = nmsBlock;
        this.pos = pos;
    }
    @Override
    public String getId() {
        return nmsBlock.getDescriptionId();
    }

    @Override
    public String getName() {
        return "FIXME";
    }

    @Override
    public int getX() {
        return pos.getX();
    }

    @Override
    public int getY() {
        return pos.getY();
    }

    @Override
    public int getZ() {
        return pos.getZ();
    }
}
