package restaurantrush.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * A food critic: impatient (loses 12 satisfaction per waiting turn) and pays full price.
 * <p>
 * Order preference (our documented Critic behaviour): a Critic tests the kitchen by
 * always ordering the dish that takes the most preparation units. Ties go to the more
 * expensive dish, then to the one listed first on the menu. On the base menu this is Pasta.
 */
public class CriticCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 12;

    private static final Comparator<MenuItem> MOST_DEMANDING =
            Comparator.comparingInt(MenuItem::getPreparationUnits).thenComparing(MenuItem::getPrice);

    public CriticCustomer(int id, Random tasteRandom) {
        super(id, tasteRandom);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS_PER_TURN;
    }

    /** Collections.max keeps the first of equal items, so full ties go to the earlier menu entry. */
    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        return Collections.max(menu.getItems(), MOST_DEMANDING);
    }
}
