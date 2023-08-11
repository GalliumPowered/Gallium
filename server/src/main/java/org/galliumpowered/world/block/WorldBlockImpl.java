package org.galliumpowered.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.galliumpowered.world.World;
import org.galliumpowered.world.WorldImpl;

public class WorldBlockImpl implements WorldBlock {
    private Block nmsBlock;
    private BlockPos pos;
    private World world;

    public WorldBlockImpl(Block nmsBlock, BlockPos pos, World world) {
        this.nmsBlock = nmsBlock;
        this.pos = pos;
        this.world = world;
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

    @Override
    public World getWorld() {
        return world;
    }
}
