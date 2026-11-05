package restaurantrush.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.model.Customer;
import restaurantrush.model.CustomerStatus;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;
import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;

/** Seats customers, takes orders and serves READY dishes: one of these per turn. */
public class Waiter extends Staff {

    public Waiter() {
        super(1);
    }

    @Override
    public String getRole() {
        return "Waiter";
    }

    /** Seats a queued customer at the first free table. */
    public ActionResult seat(Customer customer, Restaurant restaurant) {
        if (!hasTaskLeft()) {
            return alreadyBusy();
        }
        Optional<String> problem = restaurant.checkCanSeat(customer);
        if (problem.isPresent()) {
            return ActionResult.failure(problem.get());
        }
        Table table = restaurant.seat(customer);
        recordTaskDone();
        return ActionResult.success("Waiter seats " + customer.getLabel() + " at Table " + table.getNumber() + ".");
    }

    /** Takes a seated customer's order. The customer, not the manager, chooses the dish. */
    public ActionResult takeOrder(Customer customer, Menu menu) {
        if (!hasTaskLeft()) {
            return alreadyBusy();
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            return ActionResult.failure("Only a seated customer without an order can order; "
                    + customer.getLabel() + " is " + customer.getStatus().getDescription() + ".");
        }
        MenuItem choice = customer.chooseMenuItem(menu);
        Money bill = customer.calculateBill(choice);
        customer.placeOrder(new Order(customer, choice, bill));
        recordTaskDone();
        int units = choice.getPreparationUnits();
        return ActionResult.success(String.format("Waiter takes %s's order: %s chooses %s (%s, %d preparation unit%s).",
                customer.getLabel(), customer.getLabel(), choice.getName(), bill, units, units == 1 ? "" : "s"));
    }

    /** Serves a dish that was already READY before this turn. */
    public ActionResult serve(Customer customer, Restaurant restaurant, int turn) {
        if (!hasTaskLeft()) {
            return alreadyBusy();
        }
        Optional<Order> maybeOrder = customer.getOrder();
        if (customer.getStatus() != CustomerStatus.WAITING_FOR_FOOD || !maybeOrder.isPresent()) {
            return ActionResult.failure(customer.getLabel() + " has no dish waiting to be served ("
                    + customer.getStatus().getDescription() + ").");
        }
        Order order = maybeOrder.get();
        String dish = customer.getLabel() + "'s " + order.getItem().getName();
        if (order.getStatus() == OrderStatus.PREPARING) {
            return ActionResult.failure(String.format("%s is not READY yet (%d/%d preparation units done).",
                    dish, order.getUnitsPrepared(), order.getUnitsRequired()));
        }
        if (!order.isServableOn(turn)) {
            return ActionResult.failure(dish + " only became READY this turn, so it can be served next turn at the earliest.");
        }
        restaurant.serve(customer, turn);
        recordTaskDone();
        return ActionResult.success(String.format("Waiter serves %s: SERVED. %s starts eating; satisfaction stays at %d.",
                dish, customer.getLabel(), customer.getSatisfaction()));
    }

    @Override
    public List<String> describeAvailableTasks(Restaurant restaurant, int turn) {
        List<String> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            boolean servable = customer.getOrder().map(order -> order.isServableOn(turn)).orElse(false);
            if (servable) {
                tasks.add("serve " + customer.getLabel());
            }
        }
        for (Customer customer : restaurant.getCustomersWithStatus(CustomerStatus.SEATED)) {
            tasks.add("order " + customer.getLabel());
        }
        if (restaurant.findFreeTable().isPresent()) {
            for (Customer customer : restaurant.getQueue().getCustomers()) {
                tasks.add("seat " + customer.getLabel());
            }
        }
        tasks.add("wait");
        return tasks;
    }
}
