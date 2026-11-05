package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The restaurant floor: its tables, waiting queue, customers, menu and running
 * totals. It is the single place where a state change touches several objects
 * at once (seating moves a customer from the queue to a table; payment updates
 * the customer, the order, the table and the totals together).
 */
public class Restaurant {

    private final List<Table> tables;
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final Menu menu;
    private final GameStats stats = new GameStats();

    public Restaurant(int tableCount, Menu menu) {
        if (tableCount < 1) {
            throw new IllegalArgumentException("A restaurant needs at least one table");
        }
        List<Table> created = new ArrayList<>();
        for (int number = 1; number <= tableCount; number++) {
            created.add(new Table(number));
        }
        this.tables = Collections.unmodifiableList(created);
        this.menu = Objects.requireNonNull(menu, "menu");
    }

    /** A new arrival joins the waiting queue. */
    public void admit(Customer customer) {
        customers.add(customer);
        queue.join(customer);
    }

    // ----- Seating -----

    /** Explains why the customer cannot be seated, or returns empty if they can. */
    public Optional<String> checkCanSeat(Customer customer) {
        if (customer.getStatus() != CustomerStatus.IN_QUEUE) {
            return Optional.of(customer.getLabel() + " is not in the waiting queue ("
                    + customer.getStatus().getDescription() + ").");
        }
        if (!findFreeTable().isPresent()) {
            return Optional.of("Every table is occupied. A table is freed only when its customer pays or leaves.");
        }
        return Optional.empty();
    }

    /** Moves a queued customer to the first free table and returns that table. */
    public Table seat(Customer customer) {
        Optional<String> problem = checkCanSeat(customer);
        if (problem.isPresent()) {
            throw new IllegalStateException(problem.get());
        }
        Table table = findFreeTable().get();
        queue.remove(customer);
        customer.seatAt(table);
        return table;
    }

    // ----- Serving, eating and paying -----

    public void serve(Customer customer, int turn) {
        customer.receiveFood(turn);
        stats.recordServed();
    }

    /** Customers served on an earlier turn finish eating; returns those who became READY_TO_PAY. */
    public List<Customer> finishMeals(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.finishEatingIfDone(turn)) {
                finished.add(customer);
            }
        }
        return finished;
    }

    /** Charges the customer, records revenue and satisfaction, and frees the table. */
    public Money collectPayment(Customer customer) {
        Money bill = customer.getOrder()
                .orElseThrow(() -> new IllegalStateException(customer.getLabel() + " has no order to pay for"))
                .getBill();
        int satisfaction = customer.getSatisfaction();
        customer.pay();
        stats.recordPayment(bill, satisfaction);
        return bill;
    }

    // ----- Waiting and abandonment -----

    /**
     * Every customer still waiting for service loses satisfaction. Anyone who
     * reaches 0 leaves without paying. Returns descriptions of what happened.
     */
    public List<String> applyWaitingPenalties() {
        List<String> drops = new ArrayList<>();
        List<String> departures = new ArrayList<>();
        for (Customer customer : customers) {
            if (!customer.getStatus().isWaitingForService()) {
                continue;
            }
            int before = customer.getSatisfaction();
            boolean ranOutOfPatience = customer.loseSatisfactionForWaiting();
            if (!ranOutOfPatience) {
                drops.add(customer.getLabel() + " " + before + "->" + customer.getSatisfaction());
                continue;
            }
            String cancelled = customer.getOrder()
                    .filter(Order::isUnfinished)
                    .map(order -> " Their " + order.getItem().getName() + " order is cancelled.")
                    .orElse("");
            String freedTable = customer.getTable()
                    .map(table -> " Table " + table.getNumber() + " is free.")
                    .orElse("");
            queue.remove(customer);
            customer.leaveUnhappy();
            stats.recordUnhappyDeparture();
            departures.add(customer.getLabel() + " (" + before + "->0) leaves without paying." + cancelled + freedTable);
        }
        List<String> events = new ArrayList<>();
        if (!drops.isEmpty()) {
            events.add("Still waiting, satisfaction drops: " + String.join(", ", drops));
        }
        events.addAll(departures);
        return events;
    }

    // ----- Queries -----

    public Optional<Customer> findCustomer(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) {
                return Optional.of(customer);
            }
        }
        return Optional.empty();
    }

    public Optional<Table> findFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }

    public List<Customer> getCustomersWithStatus(CustomerStatus status) {
        List<Customer> matching = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == status) {
                matching.add(customer);
            }
        }
        return matching;
    }

    /** All customers who have arrived, in arrival order. */
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Table> getTables() {
        return tables;
    }

    public WaitingQueue getQueue() {
        return queue;
    }

    public Menu getMenu() {
        return menu;
    }

    public GameStats getStats() {
        return stats;
    }
}
