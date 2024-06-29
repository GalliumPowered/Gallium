package org.galliumpowered.event.player;

import org.galliumpowered.world.block.WorldBlock;
import org.galliumpowered.world.entity.Player;

public class PlayerBreakBlockEvent extends PlayerEvent {
    private Player player;
    private WorldBlock block;

    /**
     * A player break block event
     *
     * @param player The player
     * @param block The block
     */
    public PlayerBreakBlockEvent(Player player, WorldBlock block) {
        super(player);
        this.player = player;
        this.block = block;
    }

    /**
     * The block the player broke
     * @return The block the player broke
     */
    public WorldBlock getBlock() {
        return block;
    }
}
