package org.galliumpowered.event.player;

import org.galliumpowered.world.block.Block;
import org.galliumpowered.world.block.WorldBlock;
import org.galliumpowered.world.entity.Player;

public class PlayerPlaceBlockEvent extends PlayerEvent {
    private Player player;
    private WorldBlock block;

    /**
     * A player break block event
     *
     * @param player The player
     * @param block The block
     */
    public PlayerPlaceBlockEvent(Player player, WorldBlock block) {
        super(player);
        this.player = player;
        this.block = block;
    }

    public WorldBlock getBlock() {
        return block;
    }
}
