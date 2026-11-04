package restaurantrush.control;

import java.util.Random;

import restaurantrush.model.Customer;
import restaurantrush.model.RegularCustomer;

/**
 * Creates arriving customers and numbers them C1, C2, ... in arrival order.
 * <p>
 * Each customer gets a random source derived from the game seed, so with the
 * same seed every customer always picks the same dish, whatever the manager does.
 * Stage 2 changes this class to decide which arrivals are VIPs or Critics.
 */
public class CustomerFactory {

    private final Random seedSource;
    private int nextId = 1;

    public CustomerFactory(long seed) {
        this.seedSource = new Random(seed);
    }

    public Customer createArrival(int turn) {
        return new RegularCustomer(nextId++, turn, new Random(seedSource.nextLong()));
    }
}
