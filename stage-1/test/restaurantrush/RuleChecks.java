package restaurantrush;

import java.util.Random;

import restaurantrush.control.CustomerFactory;
import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.GameOutcome;
import restaurantrush.control.GameResult;
import restaurantrush.model.Customer;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.RegularCustomer;
import restaurantrush.model.Restaurant;
import restaurantrush.staff.ActionResult;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Waiter;

/**
 * Plain-Java checks of the rules in the brief (no test library needed).
 * Run: java -cp out restaurantrush.RuleChecks
 */
public final class RuleChecks {

    private static int passed;
    private static int failed;

    private RuleChecks() {
    }

    public static void main(String[] args) {
        workedExampleFromSection33();
        invalidChoiceDoesNotUseUpTheTask();
        cashierChargesOnePerTurnOldestFirst();
        neglectedCustomersLeaveAndTheGameIsLost();
        System.out.printf("%n%d checks passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** One Regular customer ordering Pasta with the manager's full attention (brief section 3.3). */
    private static void workedExampleFromSection33() {
        section("Worked example, section 3.3");
        ScriptedView view = new ScriptedView(   // each turn: Waiter, Chef
                "seat C1", "wait",              // turn 1
                "order C1", "cook C1",          // turn 2: the Chef may cook the order taken this turn
                "serve C1", "wait",             // turn 3: refused, only 1/2 units done -> choose again
                "cook C1",                      // turn 3 Chef: READY
                "serve C1", "wait");            // turn 4: SERVED (later turns all Wait)
        new GameController(GameConfig.base(0), view, new PastaLoverFactory()).run();

        check("turn 1 satisfaction", view.satisfaction(1, "C1"), 92);
        check("turn 2 satisfaction", view.satisfaction(2, "C1"), 84);
        check("turn 3 satisfaction", view.satisfaction(3, "C1"), 76);
        check("turn 4 satisfaction (served, no further loss)", view.satisfaction(4, "C1"), 76);
        check("turn 4 status", view.status(4, "C1"), "EATING");
        check("turn 5 status", view.status(5, "C1"), "READY_TO_PAY");
        check("turn 5 revenue (cannot pay the turn it became READY_TO_PAY)", view.revenue(5), "$0.00");
        check("turn 6 status", view.status(6, "C1"), "PAID");
        check("turn 6 revenue", view.revenue(6), "$18.00");
        ActionResult earlyServe = view.firstFailure();
        check("serving an unfinished dish is refused", earlyServe != null && !earlyServe.isSuccess(), true);
        check("refusal explains why", earlyServe != null && earlyServe.getMessage().contains("not READY yet (1/2"), true);
    }

    private static void invalidChoiceDoesNotUseUpTheTask() {
        section("Invalid choices and one task per turn");
        Restaurant restaurant = new Restaurant(2, GameConfig.base(0).getMenu());
        Customer c1 = new RegularCustomer(1, new Random(1));
        Customer c2 = new RegularCustomer(2, new Random(2));
        Customer c3 = new RegularCustomer(3, new Random(3));
        restaurant.admit(c1);
        restaurant.admit(c2);
        restaurant.admit(c3);
        Waiter waiter = new Waiter();
        waiter.startTurn();
        check("ordering for a queued customer is refused", waiter.takeOrder(c1, restaurant.getMenu()).isSuccess(), false);
        check("the Waiter can still act after a refusal", waiter.seat(c1, restaurant).isSuccess(), true);
        ActionResult second = waiter.seat(c2, restaurant);
        check("a second Waiter task in the same turn is refused", second.isSuccess(), false);
        check("refusal says the task is used", second.getMessage().contains("already done a task"), true);
        waiter.startTurn();
        check("next turn the Waiter can seat again", waiter.seat(c2, restaurant).isSuccess(), true);
        waiter.startTurn();
        ActionResult full = waiter.seat(c3, restaurant);
        check("seating is refused when every table is occupied", full.isSuccess(), false);
        check("refusal explains the tables are full", full.getMessage().contains("Every table is occupied"), true);
    }

    private static void cashierChargesOnePerTurnOldestFirst() {
        section("Cashier");
        Menu menu = GameConfig.base(0).getMenu();
        Restaurant restaurant = new Restaurant(2, menu);
        Customer c1 = new RegularCustomer(1, new Random(1));
        Customer c2 = new RegularCustomer(2, new Random(2));
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        for (Customer customer : new Customer[] {c1, c2}) {
            restaurant.admit(customer);
            waiter.startTurn();
            waiter.seat(customer, restaurant);
            customer.placeOrder(new Order(customer, menu.get(0), menu.get(0).getPrice()));
            chef.startTurn();
            chef.prepare(customer, 1);
            restaurant.serve(customer, 2);
        }
        restaurant.finishMeals(3);
        Cashier cashier = new Cashier();
        cashier.startTurn();
        check("nobody pays the turn they become READY_TO_PAY", cashier.collectNextPayment(restaurant, 3).isPresent(), false);
        cashier.startTurn();
        String first = cashier.collectNextPayment(restaurant, 4).orElse("");
        check("oldest customer pays first", first.contains("from C1"), true);
        check("only one payment per turn", c2.getStatus().name(), "READY_TO_PAY");
        cashier.startTurn();
        check("next customer pays the following turn",
                cashier.collectNextPayment(restaurant, 5).orElse("").contains("from C2"), true);
        check("revenue counts both bills, shown with two decimals", restaurant.getStats().getRevenue().toString(), "$18.00");
        check("both tables are free", restaurant.getTables().get(0).isFree() && restaurant.getTables().get(1).isFree(), true);
    }

    private static void neglectedCustomersLeaveAndTheGameIsLost() {
        section("Abandonment and defeat");
        ScriptedView view = new ScriptedView();
        GameResult result = new GameController(GameConfig.base(GameConfig.DEFAULT_SEED), view).run();
        check("C1 is still waiting at the end of turn 12", view.satisfaction(12, "C1"), 4);
        check("C1 leaves at the end of turn 13", view.status(13, "C1"), "LEFT");
        check("C2 leaves at the end of turn 16", view.status(16, "C2"), "LEFT");
        check("two unhappy departures", result.getStats().getUnhappyDepartures(), 2);
        check("average shown as N/A when nobody paid", result.getStats().getAverageSatisfaction().isPresent(), false);
        check("result is defeat", result.getOutcome(), GameOutcome.DEFEAT);
    }

    /** Every customer orders Pasta, to reproduce the brief's example exactly. */
    private static final class PastaLoverFactory extends CustomerFactory {
        private int nextId = 1;

        PastaLoverFactory() {
            super(0);
        }

        @Override
        public Customer createArrival() {
            return new RegularCustomer(nextId++, new Random(0)) {
                @Override
                public MenuItem chooseMenuItem(Menu menu) {
                    return menu.get(2);
                }
            };
        }
    }

    private static void section(String name) {
        System.out.println();
        System.out.println(name);
    }

    private static void check(String description, Object actual, Object expected) {
        if (expected.equals(actual)) {
            passed++;
            System.out.println("  ok    " + description);
        } else {
            failed++;
            System.out.println("  FAIL  " + description + ": expected " + expected + " but was " + actual);
        }
    }
}
