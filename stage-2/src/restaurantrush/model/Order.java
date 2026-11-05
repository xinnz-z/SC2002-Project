package restaurantrush.model;

/**
 * One customer's order. The bill is locked when the order is taken, so later
 * pricing changes never affect an order already placed.
 * <p>
 * The status only moves forward through guarded methods, so an order can never
 * be served before it is READY or charged before it is SERVED.
 */
public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final Money bill;
    private int unitsPrepared;
    private int readyTurn;
    private OrderStatus status = OrderStatus.PREPARING;

    public Order(Customer customer, MenuItem item, Money bill) {
        if (customer == null || item == null || bill == null) {
            throw new IllegalArgumentException("An order needs a customer, an item and a bill");
        }
        this.customer = customer;
        this.item = item;
        this.bill = bill;
    }

    /** The Chef spends one preparation unit; the last unit makes the order READY. */
    public void prepareOneUnit(int turn) {
        requireStatus(OrderStatus.PREPARING, "prepare");
        unitsPrepared++;
        if (unitsPrepared == item.getPreparationUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        }
    }

    /**
     * A dish can be served only if it was already READY when the Waiter acts.
     * The Waiter acts before the Chef, so that means READY on an earlier turn.
     */
    public boolean isServableOn(int turn) {
        return status == OrderStatus.READY && readyTurn < turn;
    }

    /** True while the order has not yet reached the customer. */
    public boolean isUnfinished() {
        return status == OrderStatus.PREPARING || status == OrderStatus.READY;
    }

    void markServed() {
        requireStatus(OrderStatus.READY, "serve");
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        requireStatus(OrderStatus.SERVED, "charge for");
        status = OrderStatus.PAID;
    }

    void cancel() {
        if (!isUnfinished()) {
            throw new IllegalStateException("Cannot cancel an order that is " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + action + " an order that is " + status);
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public Money getBill() {
        return bill;
    }


    public int getUnitsPrepared() {
        return unitsPrepared;
    }

    public int getUnitsRequired() {
        return item.getPreparationUnits();
    }

    public OrderStatus getStatus() {
        return status;
    }
}
