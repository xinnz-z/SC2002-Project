package restaurantrush.model;

/**
 * Two existing menu items sold together. Its preparation units are the sum of
 * the components', and its price is 90% of their combined price, rounded to the
 * nearest cent (Salad + Pasta = $24.30, 3 units).
 * <p>
 * Extends {@link MenuItem}, so the Chef, the Waiter, orders and bills handle a
 * combo exactly like any other dish.
 */
public class ComboMeal extends MenuItem {

    private static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName(),
                first.getPrice().plus(second.getPrice()).percentage(PRICE_PERCENT),
                first.getPreparationUnits() + second.getPreparationUnits());
        this.first = first;
        this.second = second;
    }

    public boolean contains(MenuItem item) {
        return first == item || second == item;
    }
}
