package restaurantrush.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import restaurantrush.model.Customer;
import restaurantrush.model.HappyHour;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.staff.ActionResult;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Host;
import restaurantrush.staff.SeatingStaff;
import restaurantrush.staff.Staff;
import restaurantrush.staff.Waiter;

/**
 * Runs the game: all turns in order, each turn's six steps in order, then the
 * result check. It routes the manager's commands to the right staff member;
 * the staff and domain objects decide whether each command is allowed.
 */
public class GameController {

    private final GameConfig config;
    private final GameView view;
    private final CustomerFactory customerFactory;
    private final Restaurant restaurant;
    private final Host host = new Host();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Staff> staff;

    public GameController(GameConfig config, GameView view) {
        this(config, view, new CustomerFactory(config.getSeed()));
    }

    public GameController(GameConfig config, GameView view, CustomerFactory customerFactory) {
        this.config = config;
        this.view = view;
        this.customerFactory = customerFactory;
        this.restaurant = new Restaurant(config.getTableCount(), config.getMenu());
        this.staff = Collections.unmodifiableList(Arrays.<Staff>asList(host, waiter, chef, cashier));
    }

    /** Plays every turn (there is no early ending) and returns the final result. */
    public GameResult run() {
        view.showWelcome(config);
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        GameResult result = config.getObjective().evaluate(restaurant.getStats());
        view.showFinalResult(result, config.getMaxTurns());
        return result;
    }

    private void playTurn(int turn) {
        for (Staff member : staff) {
            member.startTurn();
        }

        // Step 1: arrivals, then Happy Hour recovery if it is still running from last turn
        List<String> startEvents = admitArrivals(turn);
        restaurant.recoverDuringHappyHour(turn).ifPresent(startEvents::add);
        view.showTurnStart(turn, config.getMaxTurns(), restaurant, staff, startEvents);

        // Start of turn: the manager may switch on Happy Hour (once per game)
        offerHappyHour(turn);

        // Steps 2 and 3: manager choices; the Host acts first, the Waiter before the Chef
        askUntilValid(host, command -> executeHostCommand(command), turn);
        askUntilValid(waiter, command -> executeWaiterCommand(command, turn), turn);
        askUntilValid(chef, command -> executeChefCommand(command, turn), turn);

        // Step 4: eating and payment
        List<String> events = new ArrayList<>();
        for (Customer customer : restaurant.finishMeals(turn)) {
            events.add(customer.getLabel() + " finishes eating: READY_TO_PAY (pays next turn at the earliest).");
        }
        cashier.collectNextPayment(restaurant, turn).ifPresent(events::add);

        // Step 5: waiting and abandonment
        events.addAll(restaurant.applyWaitingPenalties());

        view.showTurnEnd(turn, config.getMaxTurns(), events, restaurant.getStats());
    }

    private List<String> admitArrivals(int turn) {
        List<String> messages = new ArrayList<>();
        for (CustomerType type : config.getArrivalsAt(turn)) {
            Customer customer = customerFactory.createArrival(turn, type);
            restaurant.admit(customer);
            messages.add("Arrival: " + customer.getLabel() + " (" + customer.getTypeName()
                    + ") arrives and joins the waiting queue with satisfaction " + customer.getSatisfaction()
                    + ". " + customer.getTypeName() + ": " + customer.describeTraits() + ".");
        }
        return messages;
    }

    private void offerHappyHour(int turn) {
        if (!restaurant.getHappyHour().isAvailable() || !view.askStartHappyHour(restaurant, turn)) {
            return;
        }
        String recovery = restaurant.startHappyHour(turn);
        int lastTurn = turn + HappyHour.DURATION_TURNS - 1;
        view.showActionResult(ActionResult.success(String.format(
                "Happy Hour is on for turns %d-%d: orders taken now get %d%% off. %s",
                turn, lastTurn, HappyHour.DISCOUNT_PERCENT, recovery)));
    }

    /** An invalid choice is explained and asked again; it does not use up the staff task. */
    private void askUntilValid(Staff member, Function<ManagerCommand, ActionResult> executor, int turn) {
        while (true) {
            ManagerCommand command = view.askCommand(member, restaurant, turn);
            ActionResult result = executor.apply(command);
            view.showActionResult(result);
            if (result.isSuccess()) {
                return;
            }
        }
    }

    private ActionResult executeHostCommand(ManagerCommand command) {
        switch (command.getType()) {
            case WAIT:
                return host.waitThisTurn();
            case SEAT:
                return withCustomer(command, customer -> seat(host, customer, command));
            default:
                return ActionResult.failure("The Host can only seat a queued customer or wait, e.g. 'seat C1'.");
        }
    }

    private ActionResult executeWaiterCommand(ManagerCommand command, int turn) {
        switch (command.getType()) {
            case WAIT:
                return waiter.waitThisTurn();
            case SEAT:
                return withCustomer(command, customer -> seat(waiter, customer, command));
            case TAKE_ORDER:
                return withCustomer(command, customer -> waiter.takeOrder(customer, restaurant, turn));
            case SERVE:
                return withCustomer(command, customer -> waiter.serve(customer, restaurant, turn));
            default:
                return ActionResult.failure("The Waiter can't do that. Waiter tasks are: seat, order, serve or wait.");
        }
    }

    private ActionResult executeChefCommand(ManagerCommand command, int turn) {
        switch (command.getType()) {
            case WAIT:
                return chef.waitThisTurn();
            case COOK:
                return withCustomer(command, customer -> chef.prepare(customer, turn));
            default:
                return ActionResult.failure("The Chef can only cook an order or wait, e.g. 'cook C1'.");
        }
    }

    private ActionResult seat(SeatingStaff seater, Customer customer, ManagerCommand command) {
        Table table = null;
        if (command.hasTable()) {
            Optional<Table> found = restaurant.findTable(command.getTableNumber());
            if (!found.isPresent()) {
                return ActionResult.failure("There is no Table " + command.getTableNumber()
                        + ". Tables are numbered 1 to " + restaurant.getTables().size() + ".");
            }
            table = found.get();
        }
        return seater.seat(customer, table, restaurant);
    }

    private ActionResult withCustomer(ManagerCommand command, Function<Customer, ActionResult> action) {
        Optional<Customer> customer = restaurant.findCustomer(command.getCustomerId());
        if (!customer.isPresent()) {
            return ActionResult.failure("There is no customer C" + command.getCustomerId() + " in the restaurant yet.");
        }
        return action.apply(customer.get());
    }
}
