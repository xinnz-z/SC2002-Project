package restaurantrush.control;

import java.util.List;

import restaurantrush.model.GameStats;
import restaurantrush.model.Restaurant;
import restaurantrush.staff.ActionResult;
import restaurantrush.staff.Staff;

/**
 * What the controller needs from a user interface: show state and collect the
 * manager's decisions. It contains no game rules.
 * <p>
 * The controller depends on this interface rather than on the console, so the
 * same game can be driven by the console view or by a scripted view in tests.
 */
public interface GameView {

    void showWelcome(GameConfig config);

    void showTurnStart(int turn, int maxTurns, Restaurant restaurant, List<Staff> staff, List<String> arrivals);

    /** Asks the manager for this staff member's task. Must never return null. */
    ManagerCommand askCommand(Staff staff, Restaurant restaurant, int turn);

    void showActionResult(ActionResult result);

    void showTurnEnd(int turn, int maxTurns, List<String> events, GameStats stats);

    void showFinalResult(GameResult result, int turnsPlayed);
}
