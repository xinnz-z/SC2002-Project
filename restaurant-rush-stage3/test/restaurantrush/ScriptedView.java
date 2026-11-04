package restaurantrush;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameResult;
import restaurantrush.control.GameView;
import restaurantrush.control.ManagerCommand;
import restaurantrush.model.Customer;
import restaurantrush.model.GameStats;
import restaurantrush.model.Restaurant;
import restaurantrush.staff.ActionResult;
import restaurantrush.staff.Staff;
import restaurantrush.ui.CommandParser;
import restaurantrush.ui.InvalidCommandException;

/**
 * A view for automated checks: feeds a fixed list of commands to the controller
 * (Wait once they run out) and records what happened at the end of each turn.
 */
class ScriptedView implements GameView {

    private final Deque<String> commands;
    private final CommandParser parser = new CommandParser();
    private final List<ActionResult> results = new ArrayList<>();
    private final Map<Integer, Map<String, Integer>> satisfactionAtTurnEnd = new HashMap<>();
    private final Map<Integer, Map<String, String>> statusAtTurnEnd = new HashMap<>();
    private final Map<Integer, String> revenueAtTurnEnd = new HashMap<>();
    private Restaurant restaurant;
    private int happyHourTurn;

    ScriptedView(String... commands) {
        this.commands = new ArrayDeque<>(Arrays.asList(commands));
    }

    /** Answer yes to the Happy Hour question on this turn (and no on every other). */
    ScriptedView withHappyHourOn(int turn) {
        this.happyHourTurn = turn;
        return this;
    }

    @Override
    public boolean askStartHappyHour(Restaurant restaurant, int turn) {
        return turn == happyHourTurn;
    }

    @Override
    public void showWelcome(GameConfig config) {
    }

    @Override
    public void showTurnStart(int turn, int maxTurns, Restaurant restaurant, List<Staff> staff, List<String> startEvents) {
        this.restaurant = restaurant;
    }

    @Override
    public ManagerCommand askCommand(Staff staff, Restaurant restaurant, int turn) {
        if (commands.isEmpty()) {
            return ManagerCommand.waitCommand();
        }
        try {
            return parser.parse(commands.poll());
        } catch (InvalidCommandException e) {
            throw new IllegalStateException("Bad command in test script: " + e.getMessage(), e);
        }
    }

    @Override
    public void showActionResult(ActionResult result) {
        results.add(result);
    }

    @Override
    public void showTurnEnd(int turn, int maxTurns, List<String> events, GameStats stats) {
        Map<String, Integer> satisfaction = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        for (Customer customer : restaurant.getCustomers()) {
            satisfaction.put(customer.getLabel(), customer.getSatisfaction());
            status.put(customer.getLabel(), customer.getStatus().name());
        }
        satisfactionAtTurnEnd.put(turn, satisfaction);
        statusAtTurnEnd.put(turn, status);
        revenueAtTurnEnd.put(turn, stats.getRevenue().toString());
    }

    @Override
    public void showFinalResult(GameResult result, int turnsPlayed) {
    }

    List<ActionResult> getResults() {
        return results;
    }

    /** The first refused command, or null if none was refused. */
    ActionResult firstFailure() {
        for (ActionResult result : results) {
            if (!result.isSuccess()) {
                return result;
            }
        }
        return null;
    }

    int satisfaction(int turn, String customer) {
        return satisfactionAtTurnEnd.get(turn).get(customer);
    }

    String status(int turn, String customer) {
        return statusAtTurnEnd.get(turn).get(customer);
    }

    String revenue(int turn) {
        return revenueAtTurnEnd.get(turn);
    }
}
