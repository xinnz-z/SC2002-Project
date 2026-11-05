package restaurantrush.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.model.Customer;
import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.Restaurant;

/**
 * Advances one order by one preparation unit per turn. The Chef may work on an
 * order the Waiter took earlier in the same turn.
 */
public class Chef extends Staff {

    public Chef() {
        super(1);
    }

    @Override
    public String getRole() {
        return "Chef";
    }

    public ActionResult prepare(Customer customer, int turn) {
        if (!hasTaskLeft()) {
            return alreadyBusy();
        }
        Optional<Order> maybeOrder = customer.getOrder();
        if (!maybeOrder.isPresent()) {
            return ActionResult.failure(customer.getLabel() + " has not ordered yet ("
                    + customer.getStatus().getDescription() + ").");
        }
        Order order = maybeOrder.get();
        String dish = customer.getLabel() + "'s " + order.getItem().getName();
        if (order.getStatus() != OrderStatus.PREPARING) {
            return ActionResult.failure(dish + " needs no more cooking (it is " + order.getStatus() + ").");
        }
        order.prepareOneUnit(turn);
        recordTaskDone();
        if (order.getStatus() == OrderStatus.READY) {
            return ActionResult.success(String.format(
                    "Chef finishes %s (%d/%d units): READY. It can be served next turn at the earliest.",
                    dish, order.getUnitsPrepared(), order.getUnitsRequired()));
        }
        return ActionResult.success(String.format("Chef prepares %s: %d/%d units done.",
                dish, order.getUnitsPrepared(), order.getUnitsRequired()));
    }

    @Override
    public List<String> describeAvailableTasks(Restaurant restaurant, int turn) {
        List<String> tasks = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            customer.getOrder()
                    .filter(order -> order.getStatus() == OrderStatus.PREPARING)
                    .ifPresent(order -> tasks.add(String.format("cook %s (%s %d/%d)", customer.getLabel(),
                            order.getItem().getName(), order.getUnitsPrepared(), order.getUnitsRequired())));
        }
        tasks.add("wait");
        return tasks;
    }
}
