package restaurantrush.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import restaurantrush.model.ComboMeal;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;

/** The settings for one game. {@link #base(long)} is the required base setting. */
public final class GameConfig {

    /** Seed used when none is given, so a TA can reproduce a run exactly. */
    public static final long DEFAULT_SEED = 2002L;

    private final int maxTurns;
    private final int tableCount;
    private final List<ScheduledArrival> arrivals;
    private final Menu menu;
    private final Objective objective;
    private final long seed;

    private GameConfig(int maxTurns, int tableCount, List<ScheduledArrival> arrivals, Menu menu,
                       Objective objective, long seed) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.arrivals = Collections.unmodifiableList(new ArrayList<>(arrivals));
        this.menu = menu;
        this.objective = objective;
        this.seed = seed;
    }

    /**
     * 18 turns, 2 tables, the three dishes plus three combos of them, and six arrivals
     * on turns 1, 4, 7, 10, 13 and 16. Two of the six Regular arrivals are replaced: a
     * Critic on turn 4 and a VIP on turn 7.
     */
    public static GameConfig base(long seed) {
        MenuItem salad = new MenuItem("Salad", Money.ofDollars(9), 1);
        MenuItem burger = new MenuItem("Burger", Money.ofDollars(15), 1);
        MenuItem pasta = new MenuItem("Pasta", Money.ofDollars(18), 2);
        Menu menu = new Menu(
                Arrays.asList(salad, burger, pasta),
                Arrays.asList(new ComboMeal(salad, burger), new ComboMeal(salad, pasta), new ComboMeal(burger, pasta)));
        List<ScheduledArrival> arrivals = Arrays.asList(
                new ScheduledArrival(1, CustomerType.REGULAR),
                new ScheduledArrival(4, CustomerType.CRITIC),
                new ScheduledArrival(7, CustomerType.VIP),
                new ScheduledArrival(10, CustomerType.REGULAR),
                new ScheduledArrival(13, CustomerType.REGULAR),
                new ScheduledArrival(16, CustomerType.REGULAR));
        Objective objective = new Objective(4, Money.ofDollars(45), 60);
        return new GameConfig(18, 2, arrivals, menu, objective, seed);
    }

    /** The types of customer arriving at the start of the given turn, in schedule order. */
    public List<CustomerType> getArrivalsAt(int turn) {
        List<CustomerType> types = new ArrayList<>();
        for (ScheduledArrival arrival : arrivals) {
            if (arrival.getTurn() == turn) {
                types.add(arrival.getType());
            }
        }
        return types;
    }

    public List<ScheduledArrival> getArrivals() {
        return arrivals;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public Menu getMenu() {
        return menu;
    }

    public Objective getObjective() {
        return objective;
    }

    public long getSeed() {
        return seed;
    }
}
