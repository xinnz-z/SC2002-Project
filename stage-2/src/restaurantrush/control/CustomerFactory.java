package restaurantrush.control;

import java.util.Random;

import restaurantrush.model.Customer;

/**
 * Creates arriving customers and numbers them C1, C2, ... in arrival order.
 * <p>
 * Each customer gets a random source derived from the game seed, so with the
 * same seed every customer always picks the same dish, whatever the manager does.
 * The customer's type comes from the arrival schedule; the type builds the right subclass.
 */
public class CustomerFactory {

    private final Random seedSource;
    private int nextId = 1;

    public CustomerFactory(long seed) {
        this.seedSource = new Random(seed);
    }

    public Customer createArrival(CustomerType type) {
        return type.create(nextId++, new Random(seedSource.nextLong()));
    }
}
