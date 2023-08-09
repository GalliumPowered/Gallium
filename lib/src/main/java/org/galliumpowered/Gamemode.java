package org.galliumpowered;

public enum Gamemode {
    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(2),
    SPECTATOR(3);

    private final int id;
    Gamemode(int id) {
        this.id = id;
    }

    /**
     * Get the gamemode's ID
     * @return The gamemode's ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get a gamemode by its ID
     * @param id The ID of the gamemode
     * @return The gamemode
     */
    public static Gamemode fromId(int id) {
        for (Gamemode gamemode : values()) {
            if (gamemode.id == id) {
                return gamemode;
            }
        }
        return SURVIVAL;
    }
}
