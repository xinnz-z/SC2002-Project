package restaurantrush.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameOutcome;
import restaurantrush.control.GameResult;
import restaurantrush.control.GameView;
import restaurantrush.control.ManagerCommand;
import restaurantrush.control.Objective;
import restaurantrush.control.ScheduledArrival;
import restaurantrush.model.Customer;
import restaurantrush.model.CustomerStatus;
import restaurantrush.model.GameStats;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.OrderStatus;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.staff.ActionResult;
import restaurantrush.staff.Staff;

/**
 * Command-line boundary: prints the game state and reads the manager's typed
 * commands. It holds no game rules; everything it shows comes from the model.
 */
public class ConsoleView implements GameView {

    private static final String DOUBLE_LINE = repeat('=', 78);

    private final BufferedReader in;
    private final PrintStream out;
    private final boolean echoInput;
    private final CommandParser parser = new CommandParser();
    private final Map<String, List<String>> lastShownTasks = new HashMap<>();
    private boolean inputEnded;

    /**
     * @param echoInput print each line read, so a run fed from a file produces
     *                  a readable transcript
     */
    public ConsoleView(InputStream in, PrintStream out, boolean echoInput) {
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
        this.echoInput = echoInput;
    }

    @Override
    public void showWelcome(GameConfig config) {
        Objective objective = config.getObjective();
        out.println(DOUBLE_LINE);
        out.println("  RESTAURANT RUSH  -  Stage 2: customer behaviour");
        out.println(DOUBLE_LINE);
        out.println("You are the manager. Each turn, give the Waiter one task and the Chef one");
        out.println("preparation unit. The Cashier charges customers automatically.");
        out.println();
        out.printf("Setting : %d turns, %d tables%n", config.getMaxTurns(), config.getTableCount());
        out.println("Arrivals: " + describeArrivals(config.getArrivals()));
        List<String> items = new ArrayList<>();
        for (MenuItem item : config.getMenu().getItems()) {
            items.add(String.format("%s %s (%d unit%s)", item.getName(), item.getPrice(),
                    item.getPreparationUnits(), item.getPreparationUnits() == 1 ? "" : "s"));
        }
        out.println("Menu    : " + String.join(", ", items));
        out.printf("Goal    : after turn %d, at least %d paid customers, revenue at least %s,%n",
                config.getMaxTurns(), objective.getMinPaidCustomers(), objective.getMinRevenue());
        out.printf("          and average satisfaction of paid customers at least %d%n",
                objective.getMinAverageSatisfaction());
        out.printf("Seed    : %d (customers choose their own food from this seed)%n", config.getSeed());
        out.println();
        out.println("Type 'help' at any prompt for the list of commands.");
    }

    @Override
    public void showTurnStart(int turn, int maxTurns, Restaurant restaurant, List<Staff> staff, List<String> arrivals) {
        GameStats stats = restaurant.getStats();
        out.println();
        out.println(DOUBLE_LINE);
        out.printf(" TURN %d of %d     Revenue %s     Avg satisfaction (paid) %s%n",
                turn, maxTurns, stats.getRevenue(), formatAverage(stats.getAverageSatisfaction()));
        out.println(DOUBLE_LINE);
        for (String arrival : arrivals) {
            out.println(" Arrival: " + arrival);
        }

        List<String> queued = new ArrayList<>();
        for (Customer customer : restaurant.getQueue().getCustomers()) {
            queued.add(customer.getLabel());
        }
        out.println(" Waiting queue : " + (queued.isEmpty() ? "(empty)" : String.join(", ", queued)));
        List<String> tables = new ArrayList<>();
        for (Table table : restaurant.getTables()) {
            tables.add("Table " + table.getNumber() + ": "
                    + table.getOccupant().map(Customer::getLabel).orElse("free"));
        }
        out.println(" Tables        : " + String.join("   ", tables));
        out.println();
        printCustomers(restaurant);
        out.println();
        out.println(" Staff tasks available");
        for (Staff member : staff) {
            List<String> tasks = member.describeAvailableTasks(restaurant, turn);
            out.printf("   %-8s: %s%n", member.getRole(), String.join(" | ", tasks));
            lastShownTasks.put(member.getRole(), tasks);
        }
    }

    private void printCustomers(Restaurant restaurant) {
        List<String> paid = new ArrayList<>();
        List<String> left = new ArrayList<>();
        boolean headerPrinted = false;
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.getStatus() == CustomerStatus.PAID) {
                paid.add(customer.getLabel() + " " + customer.getOrder().map(Order::getBill).orElse(null)
                        + " (" + customer.getSatisfaction() + ")");
                continue;
            }
            if (customer.getStatus() == CustomerStatus.LEFT) {
                left.add(customer.getLabel());
                continue;
            }
            if (!headerPrinted) {
                out.printf(" %-4s %-8s %-18s %-24s %s%n", "Cust", "Type", "Satisfaction", "Status", "Order");
                headerPrinted = true;
            }
            out.printf(" %-4s %-8s %3d %-14s %-24s %s%n", customer.getLabel(), customer.getTypeName(),
                    customer.getSatisfaction(), satisfactionBar(customer.getSatisfaction()),
                    customer.getStatus().getDescription(), describeOrder(customer));
        }
        if (!headerPrinted) {
            out.println(" No customers in the restaurant.");
        }
        if (!paid.isEmpty()) {
            out.println(" Paid          : " + String.join(", ", paid));
        }
        if (!left.isEmpty()) {
            out.println(" Left unhappy  : " + String.join(", ", left));
        }
    }

    private String describeOrder(Customer customer) {
        if (!customer.getOrder().isPresent()) {
            return "-";
        }
        Order order = customer.getOrder().get();
        String progress = (order.getStatus() == OrderStatus.PREPARING)
                ? order.getUnitsPrepared() + "/" + order.getUnitsRequired() + " units"
                : order.getStatus().name();
        return order.getItem().getName() + " " + progress + ", bill " + order.getBill();
    }

    @Override
    public ManagerCommand askCommand(Staff staff, Restaurant restaurant, int turn) {
        List<String> tasks = staff.describeAvailableTasks(restaurant, turn);
        if (!tasks.equals(lastShownTasks.get(staff.getRole()))) {
            out.println("   " + staff.getRole() + " can now: " + String.join(" | ", tasks));
            lastShownTasks.put(staff.getRole(), tasks);
        }
        while (true) {
            if (inputEnded) {
                return ManagerCommand.waitCommand();
            }
            out.print(" " + staff.getRole() + "> ");
            out.flush();
            String line = readLine();
            if (line == null) {
                inputEnded = true;
                out.println();
                out.println("   (No more input: every remaining Waiter and Chef choice is Wait.)");
                return ManagerCommand.waitCommand();
            }
            if (echoInput) {
                out.println(line);
            }
            String command = stripComment(line).trim();
            if (command.equalsIgnoreCase("help") || command.equals("?")) {
                printHelp();
                continue;
            }
            try {
                return parser.parse(command);
            } catch (InvalidCommandException e) {
                out.println("   ! " + e.getMessage());
            }
        }
    }

    @Override
    public void showActionResult(ActionResult result) {
        if (result.isSuccess()) {
            out.println("   -> " + result.getMessage());
        } else {
            out.println("   ! " + result.getMessage() + " Choose again.");
        }
    }

    @Override
    public void showTurnEnd(int turn, int maxTurns, List<String> events, GameStats stats) {
        out.println(" --- End of turn " + turn + " ---");
        for (String event : events) {
            out.println("   * " + event);
        }
        out.printf(" Served %d | Unhappy departures %d | Revenue %s | Avg satisfaction (paid) %s | Turns used %d/%d%n",
                stats.getServedCount(), stats.getUnhappyDepartures(), stats.getRevenue(),
                formatAverage(stats.getAverageSatisfaction()), turn, maxTurns);
    }

    @Override
    public void showFinalResult(GameResult result, int turnsPlayed) {
        GameStats stats = result.getStats();
        Objective objective = result.getObjective();
        out.println();
        out.println(DOUBLE_LINE);
        out.println(" GAME OVER - all " + turnsPlayed + " turns played");
        out.println(DOUBLE_LINE);
        out.printf(" %-26s %-8s at least %-8s %s%n", "Paid customers", stats.getPaidCount(),
                objective.getMinPaidCustomers(), verdict(result.isPaidTargetMet()));
        out.printf(" %-26s %-8s at least %-8s %s%n", "Revenue", stats.getRevenue(),
                objective.getMinRevenue(), verdict(result.isRevenueTargetMet()));
        out.printf(" %-26s %-8s at least %-8s %s%n", "Avg satisfaction (paid)",
                formatAverage(stats.getAverageSatisfaction()), objective.getMinAverageSatisfaction(),
                verdict(result.isSatisfactionTargetMet()));
        out.printf(" Served %d | Unhappy departures %d%n", stats.getServedCount(), stats.getUnhappyDepartures());
        out.println();
        out.println(" RESULT: " + (result.getOutcome() == GameOutcome.VICTORY ? "VICTORY" : "DEFEAT"));
        out.println(DOUBLE_LINE);
    }

    private void printHelp() {
        out.println("   Commands (customers as C1 or 1, tables as T1 or 1):");
        out.println("     seat C1      Waiter seats a queued customer at the first free table");
        out.println("     seat C1 T2   Waiter seats a queued customer at a chosen table");
        out.println("     order C1     Waiter takes a seated customer's order (the customer picks the dish)");
        out.println("     serve C1     Waiter serves a dish that was already READY before this turn");
        out.println("     cook C1      Chef does one preparation unit of C1's order");
        out.println("     wait         This staff member does nothing this turn");
        out.println("   Anything after '#' on a line is ignored.");
    }

    private String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return (hash >= 0) ? line.substring(0, hash) : line;
    }

    private static String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "N/A";
    }

    private static String verdict(boolean met) {
        return met ? "met" : "NOT met";
    }

    private static String satisfactionBar(int satisfaction) {
        int filled = (satisfaction + 5) / 10;
        return "[" + repeat('#', filled) + repeat('.', 10 - filled) + "]";
    }

    private static String describeArrivals(List<ScheduledArrival> arrivals) {
        List<String> parts = new ArrayList<>();
        for (ScheduledArrival arrival : arrivals) {
            parts.add("turn " + arrival.getTurn() + " " + arrival.getType().getLabel());
        }
        return String.join(", ", parts);
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }
}
