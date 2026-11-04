package restaurantrush.control;

/**
 * One instruction from the manager, already parsed from text but not yet
 * checked against the rules (the staff member does that when it is carried out).
 */
public final class ManagerCommand {

    private static final ManagerCommand WAIT = new ManagerCommand(CommandType.WAIT, 0, 0);

    private final CommandType type;
    private final int customerId;
    private final int tableNumber;

    private ManagerCommand(CommandType type, int customerId, int tableNumber) {
        this.type = type;
        this.customerId = customerId;
        this.tableNumber = tableNumber;
    }

    public static ManagerCommand waitCommand() {
        return WAIT;
    }

    public static ManagerCommand forCustomer(CommandType type, int customerId) {
        if (type == CommandType.WAIT || customerId < 1) {
            throw new IllegalArgumentException("A " + type + " command needs a customer number");
        }
        return new ManagerCommand(type, customerId, 0);
    }

    /** @param tableNumber a specific table, or 0 for the first free table */
    public static ManagerCommand seat(int customerId, int tableNumber) {
        if (customerId < 1 || tableNumber < 0) {
            throw new IllegalArgumentException("Invalid seat command");
        }
        return new ManagerCommand(CommandType.SEAT, customerId, tableNumber);
    }

    public CommandType getType() {
        return type;
    }

    public int getCustomerId() {
        return customerId;
    }

    public boolean hasTable() {
        return tableNumber > 0;
    }

    public int getTableNumber() {
        return tableNumber;
    }
}
