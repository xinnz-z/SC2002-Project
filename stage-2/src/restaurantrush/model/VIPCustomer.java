package restaurantrush.model;

import java.util.Random;

/** A patient, valued guest: loses only 5 satisfaction per waiting turn and gets 10% off the bill. */
public class VIPCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 5;
    private static final int BILL_PERCENT = 90;

    public VIPCustomer(int id, Random tasteRandom) {
        super(id, tasteRandom);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return SATISFACTION_LOSS_PER_TURN;
    }

    /** 10% off the menu price, rounded to the nearest cent. */
    @Override
    public Money calculateBill(MenuItem item) {
        return item.getPrice().percentage(BILL_PERCENT);
    }
}
