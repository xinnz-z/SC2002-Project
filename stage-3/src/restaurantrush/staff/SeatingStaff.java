package restaurantrush.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.model.Customer;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;

/**
 * Staff who can seat queued customers: the Waiter (as one of several tasks)
 * and the Host (as their only task). Seating works the same for both, so it
 * lives here once.
 */
public abstract class SeatingStaff extends Staff {

    protected SeatingStaff(int tasksPerTurn) {
        super(tasksPerTurn);
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
        return ActionResult.success(getRole() + " seats " + customer.getLabel() + " at Table " + table.getNumber() + ".");
    }

    /** "seat Cn" for each queued customer, if a table is free. */
    protected List<String> describeSeatingTasks(Restaurant restaurant) {
        List<String> tasks = new ArrayList<>();
        if (restaurant.findFreeTable().isPresent()) {
            for (Customer customer : restaurant.getQueue().getCustomers()) {
                tasks.add("seat " + customer.getLabel());
            }
        }
        return tasks;
    }
}
