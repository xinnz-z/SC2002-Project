package restaurantrush.model;

import java.util.OptionalDouble;

/**
 * Running totals used for the end-of-turn summary and the final result.
 * Only the {@link Restaurant} records events, so the totals cannot drift from
 * what actually happened.
 */
public class GameStats {

    private Money revenue = Money.ZERO;
    private int paidCount;
    private int paidSatisfactionTotal;
    private int servedCount;
    private int unhappyDepartures;

    void recordServed() {
        servedCount++;
    }

    /** Revenue only ever includes paid bills; satisfaction is the value at the moment of payment. */
    void recordPayment(Money bill, int satisfactionAtPayment) {
        revenue = revenue.plus(bill);
        paidCount++;
        paidSatisfactionTotal += satisfactionAtPayment;
    }

    void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    public Money getRevenue() {
        return revenue;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    /** Empty when no one has paid yet (shown as N/A). */
    public OptionalDouble getAverageSatisfaction() {
        if (paidCount == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) paidSatisfactionTotal / paidCount);
    }

    /** Exact integer comparison, so 59.99 never rounds up to a pass. */
    public boolean isAverageSatisfactionAtLeast(int target) {
        return paidCount > 0 && paidSatisfactionTotal >= target * paidCount;
    }
}
