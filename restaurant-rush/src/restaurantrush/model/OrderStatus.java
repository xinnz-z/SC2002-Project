package restaurantrush.model;

/** Life cycle of an order from the moment the Waiter takes it. */
public enum OrderStatus {
    /** Taken by the Waiter; the Chef still has preparation units to do. */
    PREPARING,
    /** All preparation units done; waiting for the Waiter to serve it. */
    READY,
    /** Served to the customer, who is eating or about to pay. */
    SERVED,
    /** The customer has paid the bill. */
    PAID,
    /** The customer left before being served. */
    CANCELLED
}
