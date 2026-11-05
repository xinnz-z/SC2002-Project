package restaurantrush.model;

import java.util.Random;

/** An ordinary customer: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 8;

    public RegularCustomer(int id, Random tasteRandom) {
        super(id, tasteRandom);
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS_PER_TURN;
    }
}
