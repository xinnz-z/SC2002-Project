package restaurantrush.staff;

import java.util.List;

import restaurantrush.model.Restaurant;

/**
 * A member of staff with a limited number of tasks per turn.
 * <p>
 * Subclasses add the tasks they can perform and describe which of them are
 * possible right now, so the start-of-turn display can list every staff
 * member's options without knowing which kind of staff it is.
 */
public abstract class Staff {

    private final int tasksPerTurn;
    private int tasksDoneThisTurn;

    protected Staff(int tasksPerTurn) {
        if (tasksPerTurn < 1) {
            throw new IllegalArgumentException("Staff must be able to do at least one task per turn");
        }
        this.tasksPerTurn = tasksPerTurn;
    }

    public abstract String getRole();

    /** Tasks this staff member could perform right now, in the form the manager types them. */
    public abstract List<String> describeAvailableTasks(Restaurant restaurant, int turn);

    public void startTurn() {
        tasksDoneThisTurn = 0;
    }

    public boolean hasTaskLeft() {
        return tasksDoneThisTurn < tasksPerTurn;
    }

    /** Choosing Wait is always allowed and simply ends this staff member's turn. */
    public ActionResult waitThisTurn() {
        return ActionResult.success(getRole() + " waits this turn.");
    }

    protected void recordTaskDone() {
        tasksDoneThisTurn++;
    }

    protected ActionResult alreadyBusy() {
        return ActionResult.failure("The " + getRole() + " has already done a task this turn.");
    }
}
