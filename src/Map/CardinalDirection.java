package Map;

public enum CardinalDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST;

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

    public static CardinalDirection swapDirection(final CardinalDirection theDirection) {
        return switch (theDirection) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
