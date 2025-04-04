package org.galliumpowered.event.player;

import org.galliumpowered.world.entity.Player;

// Now I am become death, destroyer of worlds
public class PlayerDeathEvent extends PlayerEvent {
    private String deathCause;
    /**
     * A player death event
     * @param player The Player
     * @param deathCause The cause of the player's death
     */
    // TODO: Make deathCause some sort of damageSource or something, which provides additional information.
    public PlayerDeathEvent(Player player, String deathCause) {
        super(player);
        this.deathCause = deathCause;
    }

    /**
     * Cause of the death
     * @return Death cause
     */
    public String getDeathCause() {
        return deathCause;
    }
}
