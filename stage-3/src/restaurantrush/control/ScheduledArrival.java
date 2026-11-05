package restaurantrush.control;

/** One entry in the arrival schedule: which kind of customer arrives at the start of which turn. */
public final class ScheduledArrival {

    private final int turn;
    private final CustomerType type;

    public ScheduledArrival(int turn, CustomerType type) {
        if (turn < 1 || type == null) {
            throw new IllegalArgumentException("An arrival needs a turn of at least 1 and a customer type");
        }
        this.turn = turn;
        this.type = type;
    }

    public int getTurn() {
        return turn;
    }

    public CustomerType getType() {
        return type;
    }
}
