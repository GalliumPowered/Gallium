package org.galliumpowered.data;

import org.galliumpowered.world.entity.Player;

/**
 * An operator on the server
 */
public class ServerOperator {
    // TODO: setters for level and bypassesPlayerLimit
    private final Player player;
    private int level;
    private boolean bypassesPlayerLimit;

    public ServerOperator(Player player, int level, boolean bypassesPlayerLimit) {
        this.player = player;
        this.level = level;
        this.bypassesPlayerLimit = bypassesPlayerLimit;
    }

    /***
     * Get the player which holds this operator status
     *
     * @return Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the operator's level
     *
     * @return Operator level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get whether this operator bypasses the player limit
     *
     * @return Operator player limit bypass
     */
    public boolean isBypassesPlayerLimit() {
        return bypassesPlayerLimit;
    }
}
