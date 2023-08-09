package org.galliumpowered.world;

public interface World {
    enum Difficulty {
        PEACEFUL(0),
        EASY(1),
        NORMAL(2),
        HARD(3);

        private final int id;

        Difficulty(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Difficulty fromId(int id) {
            return switch (id) {
                case 0 -> PEACEFUL;
                case 1 -> EASY;
                case 3 -> HARD;
                default -> NORMAL;
            };
        }
    }

    enum Dimension {
        OVERWORLD("overworld"),
        THE_NETHER("the_nether"),
        THE_END("the_end"),
        UNKNOWN(null);

        private String path;
        Dimension(String path) {
            this.path = path;
        }

        public static Dimension fromPath(String path) {
            return switch (path) {
                case "overworld" -> Dimension.OVERWORLD;
                case "the_nether" -> Dimension.THE_NETHER;
                case "the_end" -> Dimension.THE_END;
                default -> Dimension.UNKNOWN;
            };
        }
    }

    /**
     * Get the world dimension, example: OVERWORLD
     * @return World dimension
     */
    Dimension getDimension();

    /**
     * Get the world difficulty
     * @return World difficulty
     */
    Difficulty getDifficulty();
}
