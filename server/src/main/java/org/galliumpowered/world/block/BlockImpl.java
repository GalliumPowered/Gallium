package org.galliumpowered.world.block;

public class BlockImpl implements Block {
    private net.minecraft.world.level.block.Block nmsBlock;

    public BlockImpl(net.minecraft.world.level.block.Block nmsBlock) {
        this.nmsBlock = nmsBlock;
    }
    @Override
    public String getId() {
        return nmsBlock.getDescriptionId().strip();
    }

    @Override
    public String getName() {
        return nmsBlock.getName().getContents().strip();
    }
}
