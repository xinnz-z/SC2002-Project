package restaurantrush.staff;

import java.util.List;

import restaurantrush.model.Restaurant;

/**
 * Seats at most one queued customer per turn, in addition to the Waiter's task.
 * The Host acts first in a turn, so the Waiter can take the order of a customer
 * the Host has just seated.
 */
public class Host extends SeatingStaff {

    public Host() {
        super(1);
    }

    @Override
    public String getRole() {
        return "Host";
    }

    @Override
    public List<String> describeAvailableTasks(Restaurant restaurant, int turn) {
        List<String> tasks = describeSeatingTasks(restaurant);
        tasks.add("wait");
        return tasks;
    }
}
