package restaurantrush.model;

import java.util.Optional;
import java.util.Random;

/**
 * A guest of the restaurant. Holds the guest's own state (satisfaction, status,
 * table, order) and the behaviour that can differ between customer types.
 * <p>
 * Subclasses decide how quickly they lose patience, and can override how they
 * choose food and what they are billed. The turn controller never checks a
 * customer's type; it just calls these methods.
 */
public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private final int arrivalTurn;
    private final Random tasteRandom;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerStatus status = CustomerStatus.IN_QUEUE;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    /**
     * @param tasteRandom this customer's own seeded random source, so the dish
     *                    they pick does not depend on the order the manager
     *                    serves people in
     */
    protected Customer(int id, int arrivalTurn, Random tasteRandom) {
        if (id < 1) {
            throw new IllegalArgumentException("Customer id must be positive");
        }
        if (tasteRandom == null) {
            throw new IllegalArgumentException("A customer needs a random source for choosing food");
        }
        this.id = id;
        this.arrivalTurn = arrivalTurn;
        this.tasteRandom = tasteRandom;
    }

    // ----- Behaviour that differs by customer type -----

    public abstract String getTypeName();

    /** Satisfaction lost at the end of every turn spent waiting for service. */
    public abstract int getSatisfactionLossPerTurn();

    /**
     * The customer picks their own dish; the manager never does.
     * Default rule: a uniformly random item drawn from this customer's seeded random source.
     */
    public MenuItem chooseMenuItem(Menu menu) {
        return menu.get(tasteRandom.nextInt(menu.size()));
    }

    /** What this customer is billed for an item. Default: full menu price. */
    public Money calculateBill(MenuItem item) {
        return item.getPrice();
    }

    /** One-line summary of how this customer behaves, shown when they arrive. Subclasses add their own traits. */
    public String describeTraits() {
        return "loses " + getSatisfactionLossPerTurn() + " satisfaction per waiting turn";
    }

    // ----- Life cycle (each step checks the customer is in the right state) -----

    void seatAt(Table newTable) {
        requireStatus(CustomerStatus.IN_QUEUE, "be seated");
        newTable.occupy(this);
        table = newTable;
        status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order newOrder) {
        requireStatus(CustomerStatus.SEATED, "place an order");
        if (newOrder.getCustomer() != this) {
            throw new IllegalArgumentException("That order belongs to another customer");
        }
        order = newOrder;
        status = CustomerStatus.WAITING_FOR_FOOD;
    }

    void receiveFood(int turn) {
        requireStatus(CustomerStatus.WAITING_FOR_FOOD, "be served");
        order.markServed();
        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    /** A customer served on an earlier turn has finished one full turn of eating. */
    boolean finishEatingIfDone(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    /** A customer who became READY_TO_PAY this turn pays next turn at the earliest. */
    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn;
    }

    void pay() {
        requireStatus(CustomerStatus.READY_TO_PAY, "pay");
        order.markPaid();
        status = CustomerStatus.PAID;
        leaveTable();
    }

    /**
     * Applies the end-of-turn waiting penalty (never below 0).
     *
     * @return true if the customer has run out of patience
     */
    boolean loseSatisfactionForWaiting() {
        if (!status.isWaitingForService()) {
            return false;
        }
        satisfaction = Math.max(0, satisfaction - getSatisfactionLossPerTurn());
        return satisfaction == 0;
    }

    /** Leaves without paying: any unfinished order is cancelled and the table freed. */
    void leaveUnhappy() {
        if (status.isGone()) {
            throw new IllegalStateException(getLabel() + " has already gone");
        }
        if (order != null && order.isUnfinished()) {
            order.cancel();
        }
        status = CustomerStatus.LEFT;
        leaveTable();
    }

    private void leaveTable() {
        if (table != null) {
            table.release();
            table = null;
        }
    }

    private void requireStatus(CustomerStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(getLabel() + " cannot " + action + " while " + status.getDescription());
        }
    }

    // ----- Queries -----

    public int getId() {
        return id;
    }

    /** Short label used on screen and in commands, e.g. {@code C3}. */
    public String getLabel() {
        return "C" + id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Optional<Table> getTable() {
        return Optional.ofNullable(table);
    }

    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }
}
