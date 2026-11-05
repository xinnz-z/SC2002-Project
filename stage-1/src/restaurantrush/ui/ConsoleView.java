package restaurantrush.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameOutcome;
import restaurantrush.control.GameResult;
import restaurantrush.control.GameView;
import restaurantrush.control.ManagerCommand;
import restaurantrush.control.Objective;
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

    private static final String LINE = "==============================================================================";

    private final BufferedReader in;
    private final PrintStream out;
    private final CommandParser parser = new CommandParser();
    private boolean inputEnded;

    public ConsoleView(InputStream in, PrintStream out) {
        this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        this.out = out;
    }

    @Override
    public void showWelcome(GameConfig config) {
        Objective objective = config.getObjective();
        out.println(LINE);
        out.println(" RESTAURANT RUSH - Stage 1");
        out.println(LINE);
        out.printf(" %d turns, %d tables. Customers arrive on turns %s.%n",
                config.getMaxTurns(), config.getTableCount(), joinNumbers(config.getArrivalTurns()));
        out.println(" Menu: " + describeItems(config.getMenu().getItems()));
        out.printf(" Goal: at least %d paid customers, revenue at least %s and average satisfaction%n",
                objective.getMinPaidCustomers(), objective.getMinRevenue());
        out.printf("       of paid customers at least %d after turn %d.%n",
                objective.getMinAverageSatisfaction(), config.getMaxTurns());
        out.println(" Commands: seat C1, order C1, serve C1, cook C1, wait");
    }

    @Override
    public void showTurnStart(int turn, int maxTurns, Restaurant restaurant, List<Staff> staff, List<String> arrivals) {
        GameStats stats = restaurant.getStats();
        out.println();
        out.println(LINE);
        out.printf(" TURN %d of %d | Revenue %s | Avg satisfaction (paid) %s%n",
                turn, maxTurns, stats.getRevenue(), formatAverage(stats.getAverageSatisfaction()));
        out.println(LINE);
        for (String arrival : arrivals) {
            out.println(" " + arrival);
        }
        List<String> queued = new ArrayList<>();
        for (Customer customer : restaurant.getQueue().getCustomers()) {
            queued.add(customer.getLabel());
        }
        out.println(" Waiting queue : " + (queued.isEmpty() ? "(empty)" : String.join(", ", queued)));
        List<String> tables = new ArrayList<>();
        for (Table table : restaurant.getTables()) {
            tables.add("Table " + table.getNumber() + ": " + table.getOccupant().map(Customer::getLabel).orElse("free"));
        }
        out.println(" Tables        : " + String.join(" | ", tables));
        printCustomers(restaurant);
        out.println(" Staff tasks available:");
        for (Staff member : staff) {
            out.printf("   %-8s %s%n", member.getRole() + ":",
                    String.join(" | ", member.describeAvailableTasks(restaurant, turn)));
        }
    }

    private void printCustomers(Restaurant restaurant) {
        List<String> paid = new ArrayList<>();
        List<String> left = new ArrayList<>();
        boolean headerPrinted = false;
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.getStatus() == CustomerStatus.PAID) {
                String bill = customer.getOrder().map(order -> order.getBill().toString()).orElse("");
                paid.add(customer.getLabel() + " " + bill + " (satisfaction " + customer.getSatisfaction() + ")");
            } else if (customer.getStatus() == CustomerStatus.LEFT) {
                left.add(customer.getLabel());
            } else {
                if (!headerPrinted) {
                    out.printf(" %-4s %-8s %-12s %-24s %s%n", "Cust", "Type", "Satisfaction", "Status", "Order");
                    headerPrinted = true;
                }
                out.printf(" %-4s %-8s %-12d %-24s %s%n", customer.getLabel(), customer.getTypeName(),
                        customer.getSatisfaction(), customer.getStatus().getDescription(), describeOrder(customer));
            }
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
                ? "PREPARING " + order.getUnitsPrepared() + "/" + order.getUnitsRequired()
                : order.getStatus().name();
        return order.getItem().getName() + " " + order.getBill() + ", " + progress;
    }

    @Override
    public ManagerCommand askCommand(Staff staff, Restaurant restaurant, int turn) {
        String options = String.join(" | ", staff.describeAvailableTasks(restaurant, turn));
        while (true) {
            String line = readLine(" " + staff.getRole() + " (" + options + ")> ");
            if (line == null) {
                return ManagerCommand.waitCommand();
            }
            try {
                return parser.parse(line);
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
        out.println(LINE);
        out.println(" GAME OVER after " + turnsPlayed + " turns");
        out.printf(" Paid customers          %-8s (target at least %d)  %s%n", stats.getPaidCount(),
                objective.getMinPaidCustomers(), verdict(result.isPaidTargetMet()));
        out.printf(" Revenue                 %-8s (target at least %s)  %s%n", stats.getRevenue(),
                objective.getMinRevenue(), verdict(result.isRevenueTargetMet()));
        out.printf(" Avg satisfaction (paid) %-8s (target at least %d)  %s%n",
                formatAverage(stats.getAverageSatisfaction()), objective.getMinAverageSatisfaction(),
                verdict(result.isSatisfactionTargetMet()));
        out.println(" RESULT: " + (result.getOutcome() == GameOutcome.VICTORY ? "VICTORY" : "DEFEAT"));
        out.println(LINE);
    }

    /** Prints the prompt and reads a line; returns null once input has run out. */
    private String readLine(String prompt) {
        if (inputEnded) {
            return null;
        }
        out.print(prompt);
        out.flush();
        String line;
        try {
            line = in.readLine();
        } catch (IOException e) {
            line = null;
        }
        if (line == null) {
            inputEnded = true;
            out.println();
            out.println("   (No more input: every remaining choice is Wait.)");
        }
        return line;
    }

    private static String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format("%.1f", average.getAsDouble()) : "N/A";
    }

    private static String verdict(boolean met) {
        return met ? "met" : "NOT met";
    }

    private static String describeItems(List<MenuItem> items) {
        List<String> parts = new ArrayList<>();
        for (MenuItem item : items) {
            parts.add(item.getName() + " " + item.getPrice() + " (" + item.getPreparationUnits() + " unit"
                    + (item.getPreparationUnits() == 1 ? "" : "s") + ")");
        }
        return String.join(", ", parts);
    }

    private static String joinNumbers(List<Integer> numbers) {
        List<String> parts = new ArrayList<>();
        for (Integer number : numbers) {
            parts.add(String.valueOf(number));
        }
        return String.join(", ", parts);
    }
}
