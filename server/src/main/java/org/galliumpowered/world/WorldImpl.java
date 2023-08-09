package org.galliumpowered.world;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class WorldImpl implements World {
    private Level level;

    public WorldImpl(Level level) {
        this.level = level;
    }

    @Override
    public Dimension getDimension() {
        ResourceLocation mc = level.dimension().location();

        return Dimension.fromPath(mc.getPath());
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.fromId(level.getDifficulty().getId());
    }
}
