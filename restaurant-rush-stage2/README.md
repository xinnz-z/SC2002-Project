# Restaurant Rush: Stage 2

A turn-based, command-line restaurant management game in Java. You are the manager: each turn you give the Waiter one task and the Chef one preparation unit; the Cashier works automatically.

**Stage 2** adds VIP and Critic customers to the Stage 1 game (Regular customers, the three-item menu, Waiter, Chef, Cashier, the full turn sequence, victory and defeat). It contains no Stage 3 features. Win and lose criteria are unchanged.

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

43 checks: the brief's section 3.3 worked example turn by turn, invalid choices, one task per turn, Cashier timing and order, abandonment, money rounding, and the Stage 2 customer behaviours.

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

## Customer types

| Type | Satisfaction lost per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | Full price | Seeded random pick from the menu |
| VIP | 5 | 10% off, rounded to the nearest cent (Burger $13.50) | Seeded random pick from the menu |
| Critic | 12 | Full price | **Always the dish that takes the most preparation units** (ties: higher price, then first on the menu). On the base menu, Pasta. |

**Critic behaviour (our definition):** a Critic tests the kitchen by ordering its most demanding dish. Together with losing 12 satisfaction per waiting turn, this makes the Critic the customer the manager must prioritise: even with perfect service, a Critic on Pasta reaches the table at 64.

All three types follow exactly the same seating, ordering, serving, eating and payment flow.

**Arrival schedule (base setting):** six arrivals on turns 1, 4, 7, 10, 13 and 16, as Regular, **Critic**, **VIP**, Regular, Regular, Regular. A Critic followed by a VIP gives the manager a real decision on turn 7: serve the impatient Critic's Pasta or seat the patient VIP.

## Rules as implemented

Everything follows the brief. Where the brief leaves a choice to the group, this is what the code does:

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness for Regular and VIP customers. Each customer gets their own `Random`, derived from the game seed in arrival order, and picks uniformly from the menu when the order is taken. With the same seed every customer always picks the same dish, whatever the manager does. Critics use their fixed preference instead. Seed 2002 gives C1 Salad, C2 Pasta (Critic), C3 Burger (VIP), C4 Burger, C5 Pasta, C6 Burger. |
| When the start-of-turn display appears | After step 1 (arrivals), so the manager can see and seat the new customer |
| Which table `seat` uses | The first free table, unless the manager names one |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served a dish (whether or not they have paid yet) |
| Running out of input | Every remaining Waiter and Chef choice becomes Wait, and the game still plays all 18 turns |

## Design

### Packages

| Package | Role | Classes |
|---|---|---|
| `model` | Entities and their rules | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Order`, `MenuItem`, `Menu`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | Who may do what, and how often | `Staff` (abstract), `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | Turn sequence and setup | `GameController`, `GameConfig`, `ScheduledArrival`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CustomerType`, `CommandType`, `GameOutcome` |
| `ui` | Console input and output only | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

### Responsibilities

| Class | Responsible for |
|---|---|
| `GameController` | Running 18 turns, the six steps in order, and routing each command to the right staff member |
| `Waiter` / `Chef` / `Cashier` | Checking whether a task is allowed now, carrying it out, one task per turn |
| `Restaurant` | Changes that touch several objects at once: seating (queue to table), serving, payment (customer, order, table and totals together), the end-of-turn waiting penalty and departures |
| `Customer` and subclasses | Own satisfaction, status, table and order; type-specific behaviour: patience, food choice, bill, and a description of these traits |
| `CustomerType` | Creating the right `Customer` subclass for a scheduled arrival |
| `Order` | Preparation progress, locked bill, the READY to SERVED to PAID sequence |
| `GameStats` | Revenue, paid count, satisfaction at payment, served count, unhappy departures |
| `Objective` | Deciding victory or defeat from the three targets |
| `ConsoleView` | Showing state and reading commands; no game rules |

### Where the OO principles appear

- **Encapsulation.** `Money` stores whole cents and is immutable. `Order` and `Customer` change state only through guarded methods that throw if called out of sequence (an order cannot be served before READY or charged before SERVED). `Table.occupy` / `release` and the `GameStats` recorders are package-private, so only the model can change them. Collections are returned as unmodifiable views.
- **Inheritance.** `RegularCustomer`, `VIPCustomer` and `CriticCustomer` extend `Customer`; `Waiter`, `Chef` and `Cashier` extend `Staff`, which holds the shared per-turn task budget.
- **Polymorphism.** Customer behaviour is dispatched through the customer object, never by checking its type:
  - `Restaurant.applyWaitingPenalties` calls `loseSatisfactionForWaiting()`, which uses `getSatisfactionLossPerTurn()` (8, 5 or 12);
  - `Waiter.takeOrder` calls `chooseMenuItem()` (random, or the Critic's preference) and `calculateBill()` (full price, or the VIP discount);
  - `GameController` shows `describeTraits()` on arrival.

  `CustomerType` constants each override `create(...)`, so no switch on customer type exists anywhere. The display calls `describeAvailableTasks()` on every `Staff` member in one loop.
- **Composition.** `Restaurant` owns its `Table`s, `WaitingQueue` and `GameStats`; `GameController` owns the `Restaurant` and the three staff members; `GameConfig` owns the arrival schedule; an `Order` is made of a `MenuItem` and a locked `Money` bill.
- **Open/closed.** Stage 2 added two customer types without changing the turn sequence, the staff classes or `Restaurant` (see the change list below).
- **Liskov substitution.** A `VIPCustomer` or `CriticCustomer` can go anywhere a `Customer` is expected; each keeps the same life-cycle guarantees and only changes the values its hooks return.
- **Dependency inversion.** `GameController` depends on the `GameView` interface, not on the console. `ConsoleView` implements it for play; `ScriptedView` (in `test/`) implements it to drive automated checks.

### How Stage 3 slots in

- **ComboMeal:** `MenuItem` is not final, so `ComboMeal extends MenuItem` can pass its combined name, `sum.percentage(90)` and summed units to the superclass constructor. The Critic's rule already handles combos: it picks whichever item, combo or not, has the most preparation units.
- **Host:** a new `Staff` subclass that reuses `Restaurant.checkCanSeat` and `Restaurant.seat`, plus one extra prompt in `GameController`.
- **Happy Hour:** applied where the Waiter computes the bill (after `customer.calculateBill`, so the VIP discount comes first) and in a start-of-turn recovery step; the bill is already locked in `Order`.

## Stage 1 → Stage 2 changes

Compare link: `<add your GitHub compare link, e.g. https://github.com/OWNER/REPO/compare/stage-1...stage-2>`

**New files**

| File | Purpose |
|---|---|
| `model/VIPCustomer.java` | Loses 5 per waiting turn; overrides `calculateBill` for 10% off |
| `model/CriticCustomer.java` | Loses 12 per waiting turn; overrides `chooseMenuItem` with the most-demanding-dish preference |
| `control/CustomerType.java` | Enum of customer kinds; each constant creates its own subclass |
| `control/ScheduledArrival.java` | One schedule entry: turn plus customer type |

**Changed files**

| File | Change | Reason |
|---|---|---|
| `model/Customer.java` | Added `describeTraits()` with a default built from `getSatisfactionLossPerTurn()` | So each type can describe its own behaviour on arrival without the controller or view checking types. No existing method changed. |
| `control/GameConfig.java` | Arrival list now holds `ScheduledArrival` (turn and type) instead of plain turn numbers; base setting replaces two Regular arrivals with a Critic (turn 4) and a VIP (turn 7); `arrivalsAt(turn)` / `getArrivalTurns()` replaced by `getArrivalsAt(turn)` / `getArrivals()` | The brief requires the reproducible base run to include a VIP and a Critic, so the schedule must say which type arrives when |
| `control/CustomerFactory.java` | `createArrival(turn)` became `createArrival(turn, type)` and delegates to `type.create(...)` | The factory no longer hard-codes `RegularCustomer`. The seed sequence is unchanged, so every customer still picks the same dish as in Stage 1 unless they are a Critic. |
| `control/GameController.java` | The arrival step loops over the scheduled types and adds `describeTraits()` to the arrival message (4 lines) | Small registration change; the six turn steps and all command handling are unchanged |
| `ui/ConsoleView.java` | Title says Stage 2; welcome screen lists the arrival schedule with types | Display only |
| `test/RuleChecks.java` | Test factory signature updated; two abandonment checks added for the Critic and VIP; new Stage 2 section (10 checks) | Matches the new schedule and covers the new behaviour |

**Unchanged:** `Restaurant`, `Order`, `Table`, `WaitingQueue`, `GameStats`, `Money`, `MenuItem`, `Menu`, `RegularCustomer`, all `staff` classes, `Objective`, `GameResult`, `CommandParser`, `Main`. No files were deleted.

## Files

```
src/restaurantrush/          game source (Main + model, staff, control, ui)
test/restaurantrush/         RuleChecks and ScriptedView
test-runs/                   Stage 2 victory and defeat input scripts and their full transcripts
TEST_NOTES.md                the reproducible victory and defeat runs explained
```
