package restaurantrush.model;

/** Where a customer is in the visit, from arrival to leaving. */
public enum CustomerStatus {
    IN_QUEUE("waiting in the queue"),
    SEATED("seated, not yet ordered"),
    WAITING_FOR_FOOD("waiting for food"),
    EATING("served, eating"),
    READY_TO_PAY("READY_TO_PAY"),
    PAID("paid and gone"),
    LEFT("left unhappy");

    private final String description;

    CustomerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /** Customers who have not been served yet lose satisfaction at the end of each turn. */
    public boolean isWaitingForService() {
        return this == IN_QUEUE || this == SEATED || this == WAITING_FOR_FOOD;
    }

    public boolean isGone() {
        return this == PAID || this == LEFT;
    }
}
