package restaurantrush;

import java.util.Random;

import restaurantrush.control.CustomerFactory;
import restaurantrush.control.CustomerType;
import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.GameOutcome;
import restaurantrush.control.GameResult;
import restaurantrush.control.ScheduledArrival;
import restaurantrush.model.Customer;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Money;
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
        moneyIsExactCents();
        workedExampleFromSection33();
        invalidChoiceDoesNotUseUpTheTask();
        cashierChargesOnePerTurnOldestFirst();
        neglectedCustomersLeaveAndTheGameIsLost();
        vipAndCriticBehaviour();
        System.out.printf("%n%d checks passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void moneyIsExactCents() {
        section("Money");
        check("Salad + Pasta combo price is $24.30",
                Money.ofDollars(9).plus(Money.ofDollars(18)).percentage(90).toString(), "$24.30");
        check("VIP Burger in Happy Hour is $10.80",
                Money.ofDollars(15).percentage(90).percentage(80).toString(), "$10.80");
    }

    /** One Regular customer ordering Pasta with the manager's full attention (brief section 3.3). */
    private static void workedExampleFromSection33() {
        section("Worked example, section 3.3");
        ScriptedView view = new ScriptedView(
                "seat C1", "wait",          // turn 1
                "order C1", "cook C1",      // turn 2: Chef may cook the order taken this turn
                "serve C1", "wait",         // turn 3: invalid, only 1/2 units done -> choose again
                "cook C1",                  // turn 3 Chef: READY
                "serve C1", "wait");        // turn 4: SERVED (later turns all Wait)
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
        ActionResult earlyServe = view.getResults().get(4);
        check("serving an unfinished dish is refused", earlyServe.isSuccess(), false);
        check("refusal explains why", earlyServe.getMessage().contains("not READY yet (1/2"), true);
    }

    private static void invalidChoiceDoesNotUseUpTheTask() {
        section("Invalid choices and one task per turn");
        Restaurant restaurant = new Restaurant(2, GameConfig.base(0).getMenu());
        Customer c1 = new RegularCustomer(1, 1, new Random(1));
        Customer c2 = new RegularCustomer(2, 1, new Random(2));
        restaurant.admit(c1);
        restaurant.admit(c2);
        Waiter waiter = new Waiter();
        waiter.startTurn();
        check("ordering for a queued customer is refused", waiter.takeOrder(c1, restaurant.getMenu(), 1).isSuccess(), false);
        check("the Waiter can still act after a refusal", waiter.seat(c1, null, restaurant).isSuccess(), true);
        ActionResult second = waiter.seat(c2, null, restaurant);
        check("a second Waiter task in the same turn is refused", second.isSuccess(), false);
        check("refusal says the task is used", second.getMessage().contains("already done a task"), true);
        waiter.startTurn();
        check("seating at an occupied table is refused",
                waiter.seat(c2, restaurant.findTable(1).get(), restaurant).isSuccess(), false);
        check("seating at the free table works",
                waiter.seat(c2, restaurant.findTable(2).get(), restaurant).isSuccess(), true);
    }

    private static void cashierChargesOnePerTurnOldestFirst() {
        section("Cashier");
        Menu menu = GameConfig.base(0).getMenu();
        Restaurant restaurant = new Restaurant(2, menu);
        Customer c1 = new RegularCustomer(1, 1, new Random(1));
        Customer c2 = new RegularCustomer(2, 1, new Random(2));
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        for (Customer customer : new Customer[] {c1, c2}) {
            restaurant.admit(customer);
            waiter.startTurn();
            waiter.seat(customer, null, restaurant);
            customer.placeOrder(new Order(customer, menu.get(0), menu.get(0).getPrice(), 1));
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
        check("revenue counts both bills", restaurant.getStats().getRevenue().toString(), "$18.00");
        check("both tables are free", restaurant.findFreeTable().isPresent() && restaurant.getTables().get(1).isFree(), true);
    }

    private static void neglectedCustomersLeaveAndTheGameIsLost() {
        section("Abandonment and defeat");
        ScriptedView view = new ScriptedView();
        GameResult result = new GameController(GameConfig.base(GameConfig.DEFAULT_SEED), view).run();
        check("C1 is still waiting at the end of turn 12", view.satisfaction(12, "C1"), 4);
        check("C1 (Regular, from turn 1) leaves at the end of turn 13", view.status(13, "C1"), "LEFT");
        check("C2 (Critic, from turn 4) is at 4 after eight waiting turns", view.satisfaction(11, "C2"), 4);
        check("C2 (Critic) leaves at the end of turn 12", view.status(12, "C2"), "LEFT");
        check("C3 (VIP, from turn 7) is still waiting at the end of turn 18", view.satisfaction(18, "C3"), 40);
        check("two unhappy departures", result.getStats().getUnhappyDepartures(), 2);
        check("average shown as N/A when nobody paid", result.getStats().getAverageSatisfaction().isPresent(), false);
        check("result is defeat", result.getOutcome(), GameOutcome.DEFEAT);
    }

    private static void vipAndCriticBehaviour() {
        section("Stage 2: VIP and Critic");
        GameConfig config = GameConfig.base(GameConfig.DEFAULT_SEED);
        Menu menu = config.getMenu();
        int vips = 0;
        int critics = 0;
        for (ScheduledArrival arrival : config.getArrivals()) {
            vips += (arrival.getType() == CustomerType.VIP) ? 1 : 0;
            critics += (arrival.getType() == CustomerType.CRITIC) ? 1 : 0;
        }
        check("base setting still has six arrivals", config.getArrivals().size(), 6);
        check("base setting includes at least one VIP and one Critic", vips >= 1 && critics >= 1, true);

        Customer regular = CustomerType.REGULAR.create(1, 1, new Random(1));
        Customer vip = CustomerType.VIP.create(2, 1, new Random(2));
        Customer critic = CustomerType.CRITIC.create(3, 1, new Random(3));
        Restaurant restaurant = new Restaurant(2, menu);
        restaurant.admit(regular);
        restaurant.admit(vip);
        restaurant.admit(critic);
        restaurant.applyWaitingPenalties();
        check("Regular loses 8 per waiting turn", regular.getSatisfaction(), 92);
        check("VIP loses 5 per waiting turn", vip.getSatisfaction(), 95);
        check("Critic loses 12 per waiting turn", critic.getSatisfaction(), 88);

        MenuItem burger = menu.get(1);
        check("VIP Burger bill is 10% off: $13.50", vip.calculateBill(burger).toString(), "$13.50");
        check("VIP Pasta bill is $16.20", vip.calculateBill(menu.get(2)).toString(), "$16.20");
        check("Critic pays full price", critic.calculateBill(burger).toString(), "$15.00");
        check("Critic always orders the most demanding dish (Pasta)", critic.chooseMenuItem(menu).getName(), "Pasta");

        Waiter waiter = new Waiter();
        waiter.startTurn();
        waiter.seat(vip, null, restaurant);
        waiter.startTurn();
        String orderMessage = waiter.takeOrder(vip, menu, 2).getMessage();
        Money lockedBill = vip.getOrder().get().getBill();
        check("VIP follows the same order flow; bill locked when the order is taken",
                lockedBill.equals(vip.calculateBill(vip.getOrder().get().getItem())) && orderMessage.contains("chooses"), true);
    }

    /** Every customer orders Pasta, to reproduce the brief's example exactly. */
    private static final class PastaLoverFactory extends CustomerFactory {
        private int nextId = 1;

        PastaLoverFactory() {
            super(0);
        }

        @Override
        public Customer createArrival(int turn, CustomerType type) {
            return new RegularCustomer(nextId++, turn, new Random(0)) {
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
