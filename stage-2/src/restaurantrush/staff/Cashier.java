package restaurantrush.staff;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import restaurantrush.model.Customer;
import restaurantrush.model.Money;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;

/**
 * Works automatically: charges at most one READY_TO_PAY customer per turn,
 * oldest first (the earliest to become READY_TO_PAY, then the earliest arrival).
 */
public class Cashier extends Staff {

    private static final Comparator<Customer> OLDEST_FIRST =
            Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId);

    public Cashier() {
        super(1);
    }

    @Override
    public String getRole() {
        return "Cashier";
    }

    /** @return a description of the payment, or empty if no one could pay this turn */
    public Optional<String> collectNextPayment(Restaurant restaurant, int turn) {
        if (!hasTaskLeft()) {
            return Optional.empty();
        }
        Optional<Customer> next = findNextToPay(restaurant, turn);
        if (!next.isPresent()) {
            return Optional.empty();
        }
        Customer customer = next.get();
        int tableNumber = customer.getTable().map(Table::getNumber).orElse(0);
        int satisfaction = customer.getSatisfaction();
        Money bill = restaurant.collectPayment(customer);
        recordTaskDone();
        return Optional.of(String.format("Cashier collects %s from %s: PAID, satisfaction %d recorded, Table %d is free.",
                bill, customer.getLabel(), satisfaction, tableNumber));
    }

    private Optional<Customer> findNextToPay(Restaurant restaurant, int turn) {
        return restaurant.getCustomers().stream()
                .filter(customer -> customer.canPayOn(turn))
                .min(OLDEST_FIRST);
    }

    @Override
    public List<String> describeAvailableTasks(Restaurant restaurant, int turn) {
        String plan = findNextToPay(restaurant, turn)
                .map(customer -> "automatic: will charge " + customer.getLabel() + " this turn")
                .orElse("automatic: nobody can pay this turn");
        return Collections.singletonList(plan);
    }
}
