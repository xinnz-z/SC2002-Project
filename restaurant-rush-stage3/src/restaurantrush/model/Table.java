package restaurantrush.model;

import java.util.Optional;

/**
 * A table seats one customer until that customer pays or leaves.
 * Only {@link Customer} (same package) can occupy or release it, which keeps
 * the table and the customer's record of it in step.
 */
public class Table {

    private final int number;
    private Customer occupant;

    public Table(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("Table numbers start at 1");
        }
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public Optional<Customer> getOccupant() {
        return Optional.ofNullable(occupant);
    }

    void occupy(Customer customer) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + number + " is already occupied by " + occupant.getLabel());
        }
        occupant = customer;
    }

    void release() {
        occupant = null;
    }
}
