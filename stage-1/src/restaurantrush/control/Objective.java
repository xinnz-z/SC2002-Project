package restaurantrush.control;

import restaurantrush.model.GameStats;
import restaurantrush.model.Money;

/** The three targets that must all be met after the last turn for a victory. */
public final class Objective {

    private final int minPaidCustomers;
    private final Money minRevenue;
    private final int minAverageSatisfaction;

    public Objective(int minPaidCustomers, Money minRevenue, int minAverageSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenue = minRevenue;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public GameResult evaluate(GameStats stats) {
        boolean paidMet = stats.getPaidCount() >= minPaidCustomers;
        boolean revenueMet = stats.getRevenue().isAtLeast(minRevenue);
        boolean satisfactionMet = stats.isAverageSatisfactionAtLeast(minAverageSatisfaction);
        return new GameResult(this, stats, paidMet, revenueMet, satisfactionMet);
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public Money getMinRevenue() {
        return minRevenue;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
