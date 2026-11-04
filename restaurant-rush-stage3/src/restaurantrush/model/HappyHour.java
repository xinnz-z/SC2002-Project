package restaurantrush.model;

/**
 * The Happy Hour pricing policy. The manager may switch it on once per game, at
 * the start of a turn; it lasts for that turn and the next.
 * <ul>
 *   <li>Orders taken while it is active get 20% off, applied after any customer
 *       discount (so a VIP Burger costs $15 x 0.9 x 0.8 = $10.80).</li>
 *   <li>At the start of each active turn, customers still waiting for a table or
 *       food recover 15 satisfaction (up to 100).</li>
 * </ul>
 * Bills are locked into an {@link Order} when it is taken, so starting or ending
 * Happy Hour never changes an order already placed.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int DISCOUNT_PERCENT = 20;
    public static final int RECOVERY_PER_TURN = 15;

    private int startTurn;

    public boolean isAvailable() {
        return startTurn == 0;
    }

    void start(int turn) {
        if (!isAvailable()) {
            throw new IllegalStateException("Happy Hour can only be used once per game; it started on turn " + startTurn);
        }
        if (turn < 1) {
            throw new IllegalArgumentException("Turns start at 1");
        }
        startTurn = turn;
    }

    public boolean isActiveOn(int turn) {
        return startTurn > 0 && turn >= startTurn && turn < startTurn + DURATION_TURNS;
    }

    /** Active turns left, counting the given turn: 2 on the first turn, 1 on the last, 0 otherwise. */
    public int turnsRemaining(int turn) {
        return isActiveOn(turn) ? startTurn + DURATION_TURNS - turn : 0;
    }

    /** The bill after Happy Hour: 20% off if active on this turn, otherwise unchanged. */
    public Money apply(Money bill, int turn) {
        return isActiveOn(turn) ? bill.percentage(100 - DISCOUNT_PERCENT) : bill;
    }

    /** 0 until Happy Hour has been started. */
    public int getStartTurn() {
        return startTurn;
    }
}
