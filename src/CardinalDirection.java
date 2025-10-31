public enum CardinalDirection {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    LEFT,
    RIGHT,
    BACK,
    FORWARD;

    public static CardinalDirection turnDirection(CardinalDirection theStart, CardinalDirection theEnd) {
        switch(theStart) {
            case NORTH:
                switch(theEnd) {
                    case NORTH: return FORWARD;
                    case SOUTH: return BACK;
                    case EAST: return RIGHT;
                    case WEST: return LEFT;
                }
                break;
            case SOUTH:
                switch(theEnd) {
                    case NORTH: return BACK;
                    case SOUTH: return FORWARD;
                    case EAST: return LEFT;
                    case WEST: return RIGHT;
                }
                break;
            case EAST:
                switch(theEnd) {
                    case NORTH: return LEFT;
                    case SOUTH: return RIGHT;
                    case EAST: return FORWARD;
                    case WEST: return BACK;
                }
                break;
            case WEST:
                switch(theEnd) {
                    case NORTH: return RIGHT;
                    case SOUTH: return LEFT;
                    case EAST: return BACK;
                    case WEST: return FORWARD;
                }
                break;
        }
        return null;
    }

    public static CardinalDirection swapDirection(CardinalDirection theDirection) {
        return switch (theDirection) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            default -> null;
        };
    }
}
