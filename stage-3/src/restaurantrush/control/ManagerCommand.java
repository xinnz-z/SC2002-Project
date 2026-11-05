package restaurantrush.control;

/**
 * One instruction from the manager, already parsed from text but not yet
 * checked against the rules (the staff member does that when it is carried out).
 */
public final class ManagerCommand {

    private static final ManagerCommand WAIT = new ManagerCommand(CommandType.WAIT, 0);

    private final CommandType type;
    private final int customerId;

    private ManagerCommand(CommandType type, int customerId) {
        this.type = type;
        this.customerId = customerId;
    }

    public static ManagerCommand waitCommand() {
        return WAIT;
    }

    public static ManagerCommand forCustomer(CommandType type, int customerId) {
        if (type == CommandType.WAIT || customerId < 1) {
            throw new IllegalArgumentException("A " + type + " command needs a customer number");
        }
        return new ManagerCommand(type, customerId);
    }

    public CommandType getType() {
        return type;
    }

    public int getCustomerId() {
        return customerId;
    }
}
