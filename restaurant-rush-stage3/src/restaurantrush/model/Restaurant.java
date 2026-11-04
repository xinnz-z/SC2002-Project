package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The restaurant floor: its tables, waiting queue, customers, menu, Happy Hour
 * policy and running totals. It is the single place where a state change touches
 * several objects at once (seating moves a customer from the queue to a table;
 * payment updates the customer, the order, the table and the totals together).
 */
public class Restaurant {

    private final List<Table> tables;
    private final WaitingQueue queue = new WaitingQueue();
    private final List<Customer> customers = new ArrayList<>();
    private final Menu menu;
    private final HappyHour happyHour = new HappyHour();
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

    /**
     * Explains why the customer cannot be seated, or returns empty if they can.
     *
     * @param table a specific table, or null for the first free table
     */
    public Optional<String> checkCanSeat(Customer customer, Table table) {
        if (customer.getStatus() != CustomerStatus.IN_QUEUE) {
            return Optional.of(customer.getLabel() + " is not in the waiting queue ("
                    + customer.getStatus().getDescription() + ").");
        }
        if (table == null) {
            if (findFreeTable().isPresent()) {
                return Optional.empty();
            }
            return Optional.of("Every table is occupied. A table is freed only when its customer pays or leaves.");
        }
        return table.getOccupant()
                .map(occupant -> "Table " + table.getNumber() + " is occupied by " + occupant.getLabel() + ".");
    }

    /** Moves a queued customer to a table and returns the table used. */
    public Table seat(Customer customer, Table table) {
        Optional<String> problem = checkCanSeat(customer, table);
        if (problem.isPresent()) {
            throw new IllegalStateException(problem.get());
        }
        Table chosen = (table != null) ? table : findFreeTable().get();
        queue.remove(customer);
        customer.seatAt(chosen);
        return chosen;
    }

    // ----- Pricing and Happy Hour -----

    /**
     * The bill to lock into a new order: the customer's own price first (e.g. the
     * VIP discount), then the Happy Hour discount if it is active this turn.
     */
    public Money quoteBill(Customer customer, MenuItem item, int turn) {
        return happyHour.apply(customer.calculateBill(item), turn);
    }

    /** Starts Happy Hour on this turn and gives this turn's recovery. Returns what the recovery did. */
    public String startHappyHour(int turn) {
        happyHour.start(turn);
        return recoverDuringHappyHour(turn).orElse("");
    }

    /**
     * At the start of an active Happy Hour turn, customers still waiting for a
     * table or food recover satisfaction. Returns empty if Happy Hour is not active.
     */
    public Optional<String> recoverDuringHappyHour(int turn) {
        if (!happyHour.isActiveOn(turn)) {
            return Optional.empty();
        }
        List<String> changes = new ArrayList<>();
        for (Customer customer : customers) {
            int before = customer.getSatisfaction();
            customer.recoverSatisfaction(HappyHour.RECOVERY_PER_TURN);
            if (customer.getSatisfaction() != before) {
                changes.add(customer.getLabel() + " " + before + "->" + customer.getSatisfaction());
            }
        }
        return Optional.of("Happy Hour recovery (+" + HappyHour.RECOVERY_PER_TURN + " for customers waiting): "
                + (changes.isEmpty() ? "nobody needed it" : String.join(", ", changes)));
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

    public Optional<Table> findTable(int number) {
        if (number < 1 || number > tables.size()) {
            return Optional.empty();
        }
        return Optional.of(tables.get(number - 1));
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

    public HappyHour getHappyHour() {
        return happyHour;
    }

    public GameStats getStats() {
        return stats;
    }
}
