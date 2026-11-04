package restaurantrush.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;

/** The settings for one game. {@link #base(long)} is the required base setting. */
public final class GameConfig {

    /** Seed used when none is given, so a TA can reproduce a run exactly. */
    public static final long DEFAULT_SEED = 2002L;

    private final int maxTurns;
    private final int tableCount;
    private final List<Integer> arrivalTurns;
    private final Menu menu;
    private final Objective objective;
    private final long seed;

    private GameConfig(int maxTurns, int tableCount, List<Integer> arrivalTurns, Menu menu,
                       Objective objective, long seed) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.arrivalTurns = Collections.unmodifiableList(new ArrayList<>(arrivalTurns));
        this.menu = menu;
        this.objective = objective;
        this.seed = seed;
    }

    /** 18 turns, 2 tables, arrivals on turns 1, 4, 7, 10, 13 and 16, the three-item menu. */
    public static GameConfig base(long seed) {
        Menu menu = new Menu(Arrays.asList(
                new MenuItem("Salad", Money.ofDollars(9), 1),
                new MenuItem("Burger", Money.ofDollars(15), 1),
                new MenuItem("Pasta", Money.ofDollars(18), 2)));
        Objective objective = new Objective(4, Money.ofDollars(45), 60);
        return new GameConfig(18, 2, Arrays.asList(1, 4, 7, 10, 13, 16), menu, objective, seed);
    }

    /** Number of customers who arrive at the start of the given turn. */
    public int arrivalsAt(int turn) {
        return Collections.frequency(arrivalTurns, turn);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public List<Integer> getArrivalTurns() {
        return arrivalTurns;
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
