# Restaurant Rush: Stage 1

A turn-based, command-line restaurant management game in Java. You are the manager: each turn you give the Waiter one task and the Chef one preparation unit; the Cashier works automatically. This is **Stage 1** (Regular customers, the three-item menu, Waiter, Chef, Cashier, the full turn sequence, victory and defeat). It contains no Stage 2 or Stage 3 features.

Requires Java 11 or later. No libraries, no network.

## Build and run

From the project folder:

```bash
# macOS / Linux / Windows (PowerShell or cmd)
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

Options:

| Option | Effect |
|---|---|
| `--seed N` | Seed for the customers' food choices (default `2002`) |
| `--echo` | Prints each command read; use it when feeding commands from a file |

Replay a recorded run (macOS/Linux, or Windows cmd):

```bash
java -cp out restaurantrush.Main --echo < test-runs/victory.txt
```

In PowerShell: `Get-Content test-runs/victory.txt | java -cp out restaurantrush.Main --echo`

**IntelliJ / Eclipse:** mark `src` as a source root (and `test` as a test source root), then run `restaurantrush.Main`.

### Rule checks

```bash
javac -d out -sourcepath src:test test/restaurantrush/RuleChecks.java      # Windows: src;test
java -cp out restaurantrush.RuleChecks
```

31 checks, including the brief's section 3.3 worked example turn by turn, invalid choices, one-task-per-turn, Cashier timing and order, abandonment, and the money rounding examples from Stage 3.

## How to play

At each prompt, type one command. Customers are `C1`, `C2`, ... (or just `1`); tables are `T1`, `T2` (or `1`). Commands are case-insensitive; anything after `#` is ignored.

| Command | Who | What it does |
|---|---|---|
| `seat C1` | Waiter | Seats a queued customer at the first free table |
| `seat C1 T2` | Waiter | Seats a queued customer at a chosen table |
| `order C1` | Waiter | Takes a seated customer's order; the customer picks the dish |
| `serve C1` | Waiter | Serves a dish that was already READY before this turn |
| `cook C1` | Chef | Does one preparation unit of C1's order |
| `wait` | Either | Does nothing this turn |
| `help` | Either | Lists the commands |

An invalid command is explained and you choose again; it does not use up the staff member's task.

## Rules as implemented

Everything follows the brief. Where the brief leaves a choice to the group, this is what the code does:

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness. Each customer gets their own `Random`, derived from the game seed in arrival order, and picks uniformly from the menu when the order is taken. With the same seed every customer always picks the same dish, whatever the manager does. Seed 2002 gives C1 Salad, C2-C4 Burger, C5 Pasta, C6 Burger. |
| When the start-of-turn display appears | After step 1 (arrivals), so the manager can see and seat the new customer |
| Which table `seat` uses | The first free table, unless the manager names one |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served a dish (whether or not they have paid yet) |
| Running out of input | Every remaining Waiter and Chef choice becomes Wait, and the game still plays all 18 turns |

## Design

### Packages

| Package | Role | Classes |
|---|---|---|
| `model` | Entities and their rules | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `Order`, `MenuItem`, `Menu`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | Who may do what, and how often | `Staff` (abstract), `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | Turn sequence and setup | `GameController`, `GameConfig`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CommandType`, `GameOutcome` |
| `ui` | Console input and output only | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

### Responsibilities

| Class | Responsible for |
|---|---|
| `GameController` | Running 18 turns, the six steps in order, and routing each command to the right staff member |
| `Waiter` / `Chef` / `Cashier` | Checking whether a task is allowed now, carrying it out, one task per turn |
| `Restaurant` | Changes that touch several objects at once: seating (queue to table), serving, payment (customer, order, table and totals together), the end-of-turn waiting penalty and departures |
| `Customer` | Its own satisfaction, status, table and order; type-specific behaviour (patience, food choice, bill) |
| `Order` | Preparation progress, locked bill, the READY to SERVED to PAID sequence |
| `GameStats` | Revenue, paid count, satisfaction at payment, served count, unhappy departures |
| `Objective` | Deciding victory or defeat from the three targets |
| `ConsoleView` | Showing state and reading commands; no game rules |

### Where the OO principles appear

- **Encapsulation.** `Money` stores whole cents and is immutable. `Order` and `Customer` change state only through guarded methods that throw if called out of sequence (an order cannot be served before READY or charged before SERVED). `Table.occupy` / `release` and the `GameStats` recorders are package-private, so only the model can change them. Collections are returned as unmodifiable views.
- **Inheritance.** `RegularCustomer extends Customer`; `Waiter`, `Chef` and `Cashier extend Staff`, which holds the shared per-turn task budget.
- **Polymorphism.** The controller and staff call `getSatisfactionLossPerTurn()`, `chooseMenuItem()` and `calculateBill()` on any `Customer` without checking its type. The display calls `describeAvailableTasks()` on every `Staff` member in one loop.
- **Composition.** `Restaurant` owns its `Table`s, `WaitingQueue` and `GameStats`; `GameController` owns the `Restaurant` and the three staff members; an `Order` is made of a `MenuItem` and a locked `Money` bill.
- **Dependency inversion.** `GameController` depends on the `GameView` interface, not on the console. `ConsoleView` implements it for play; `ScriptedView` (in `test/`) implements it to drive automated checks.

### How Stages 2 and 3 slot in

- **VIP and Critic (Stage 2):** new subclasses of `Customer` overriding `getSatisfactionLossPerTurn()` (5 and 12), `calculateBill()` (VIP: `price.percentage(90)`) and `chooseMenuItem()` or another hook for the Critic's behaviour. `CustomerFactory.createArrival` decides which arrivals are which. The controller does not change.
- **ComboMeal (Stage 3):** `MenuItem` is not final, so `ComboMeal extends MenuItem` can pass its combined name, `sum.percentage(90)` and summed units to the superclass constructor.
- **Host (Stage 3):** a new `Staff` subclass that reuses `Restaurant.checkCanSeat` and `Restaurant.seat`, plus one extra prompt in `GameController`.
- **Happy Hour (Stage 3):** applied where the Waiter computes the bill (after `customer.calculateBill`, so VIP discount comes first) and in a start-of-turn recovery step; the bill is already locked in `Order`.

## Files

```
src/restaurantrush/          game source (Main + model, staff, control, ui)
test/restaurantrush/         RuleChecks and ScriptedView
test-runs/                   victory and defeat input scripts and their full transcripts
TEST_NOTES.md                the reproducible victory and defeat runs explained
```
