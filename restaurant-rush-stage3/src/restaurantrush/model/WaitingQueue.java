package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customers who have arrived but not yet been seated, in arrival order.
 * The manager may seat any of them, not only the first.
 */
public class WaitingQueue {

    private final List<Customer> customers = new ArrayList<>();

    void join(Customer customer) {
        customers.add(customer);
    }

    boolean remove(Customer customer) {
        return customers.remove(customer);
    }

    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    public boolean isEmpty() {
        return customers.isEmpty();
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }
}
