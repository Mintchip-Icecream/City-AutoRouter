package Map;

/**
 * Enum representation of the four cardinal directions found on a compass.
 * We do not include intermediate directions such as Northwest, so some interpretation is needed.
 * For example, if a road is going West, and there's another road that goes northwest, we would
 * consider the second road as North because we would turn right from that road, and vice versa.
 * If a road forks, then it is fair to consider both roads as going in the same direction, however they may
 * be visually represented differently depending on the map visualization implementation.
 *
 * @author June Flores
 * @version 11/15/25
 */
public enum CardinalDirection {
    /**
     * The direction of North.
     */
    NORTH,
    /**
     * The direction of South.
     */
    SOUTH,
    /**
     * The direction of East.
     */
    EAST,
    /**
     * The direction of West.
     */
    WEST;

    /**
     * Returns the relative direction given from one direction to another. For example,
     * if our starting position is heading North, and the upcoming road is also North, we're heading forward.
     * However, if the upcoming road is going west, we're making a left-ward turn.
     *
     * @param theStart The direction of the current position in cardinal directions.
     * @param theEnd The direction of the upcoming position in cardinal directions.
     * @return The relative direction from the current to the next position.
     */
    public static Direction turnDirection(final CardinalDirection theStart, final CardinalDirection theEnd) {
        return switch (theStart) {
            case NORTH -> switch (theEnd) {
                case NORTH -> Direction.FORWARD;
                case SOUTH -> Direction.BACK;
                case EAST -> Direction.RIGHT;
                case WEST -> Direction.LEFT;
            };
            case SOUTH -> switch (theEnd) {
                case NORTH -> Direction.BACK;
                case SOUTH -> Direction.FORWARD;
                case EAST -> Direction.LEFT;
                case WEST -> Direction.RIGHT;
            };
            case EAST -> switch (theEnd) {
                case NORTH -> Direction.LEFT;
                case SOUTH -> Direction.RIGHT;
                case EAST -> Direction.FORWARD;
                case WEST -> Direction.BACK;
            };
            case WEST -> switch (theEnd) {
                case NORTH -> Direction.RIGHT;
                case SOUTH -> Direction.LEFT;
                case EAST -> Direction.BACK;
                case WEST -> Direction.FORWARD;
            };
        };
    }

    /**
     * Gives the opposite direction of the passed cardinal direction.
     * May be useful if a road claims to be going North however a user is traversing South from the road,
     * and we want to reflect this by not saying the user is headng North.
     *
     * @param theDirection The stated cardinal direction.
     * @return The opposite direction from the given direction (North returns South, East returns West, etc).
     */
    public static CardinalDirection swapDirection(final CardinalDirection theDirection) {
        return switch (theDirection) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    /**
     * Provides the string representation of the direction value.
     * @return the direction's value in a capitalized form.
     */
    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
