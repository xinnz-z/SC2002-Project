package restaurantrush.staff;

/**
 * Outcome of a manager's instruction to a staff member. A failure explains why
 * and does not use up the staff member's task, so the manager can choose again.
 */
public final class ActionResult {

    private final boolean success;
    private final String message;

    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult failure(String reason) {
        return new ActionResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
