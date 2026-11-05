# Restaurant Rush: Stage 1

A turn-based, command-line restaurant management game in Java. You are the manager: each turn you give the Waiter one task and the Chef one preparation unit, and the Cashier works automatically.

**Stage 1: basic restaurant operation.** Regular customers, the three-item menu, Waiter, Chef, Cashier, Table, Order, the waiting queue, the full turn sequence, victory and defeat.

Requires Java 11 or later. No libraries, no network.

## Links

| | |
|---|---|
| Repository | `<add repository URL>` |
| Stage tags | `<link to stage-1>` · `<link to stage-2>` · `<link to stage-3>` |
| AI history | `<member 1: link to ai-history on their branch>` · `<member 2>` · `<member 3>` · `<member 4>` |
| Video | `<add video link>` (timestamps below) |

## Build and run

From the project folder:

```bash
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

## How to play

At each prompt, type one command. The prompt lists the commands that staff member can do right now. Customers are written `C1`, `C2`, ... (`1` also works), and commands are not case-sensitive.

| Command | Who | Effect |
|---|---|---|
| `seat C1` | Waiter | Seats a queued customer at the first free table |
| `order C1` | Waiter | Takes a seated customer's order; the customer picks the dish |
| `serve C1` | Waiter | Serves a dish that was already READY before this turn |
| `cook C1` | Chef | Does one preparation unit of C1's order |
| `wait` | Either | Does nothing this turn |

An invalid command is explained and you choose again. It does not use up the staff member's task.

## Rules as implemented

Everything follows the brief. Where the brief leaves a choice to the group:

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness (seed 2002). Each customer gets their own random source derived from the seed in arrival order and picks uniformly from the menu when their order is taken. The same customer always picks the same dish, whatever the manager does: C1 Salad, C2 to C4 Burger, C5 Pasta, C6 Burger. |
| When the start-of-turn display appears | After step 1 (arrivals), so the manager can see and seat the new customer |
| Which table seating uses | The first free table |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served (whether or not they have paid yet) |
| Input runs out | Every remaining choice is Wait, and the game still plays all 18 turns |

## Design

| Package | Role | Classes |
|---|---|---|
| `model` | Entities and their rules | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `Order`, `MenuItem`, `Menu`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | Who may do what, and how often | `Staff` (abstract), `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | Turn sequence and setup | `GameController`, `GameConfig`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CommandType`, `GameOutcome` |
| `ui` | Console input and output only | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

| Class | Responsible for |
|---|---|
| `GameController` | Running the 18 turns and the six steps in order; routing each command to the right staff member |
| `Waiter` / `Chef` / `Cashier` | Checking whether a task is allowed now, carrying it out, one task per turn |
| `Restaurant` | Changes that touch several objects at once: seating (queue to table), serving, payment (customer, order, table and totals together), the end-of-turn waiting penalty and departures |
| `Customer` | Its own satisfaction, status, table and order; patience, food choice and bill |
| `Order` | Preparation progress, the locked bill, the READY to SERVED to PAID sequence |
| `GameStats` / `Objective` | Running totals; deciding victory or defeat |
| `ConsoleView` / `CommandParser` | Showing state and reading commands; no game rules |

**OO principles in the code:**
- **Encapsulation:** `Money` stores whole cents and is immutable. `Order` and `Customer` change state only through guarded methods that refuse out-of-sequence calls. `Table.occupy` and `release`, and the `GameStats` recorders, are package-private, so only the model can change them.
- **Inheritance:** `RegularCustomer` extends `Customer`. `Waiter`, `Chef` and `Cashier` extend `Staff`, which holds the shared one-task-per-turn budget.
- **Polymorphism:** the display lists every staff member's options through `Staff.describeAvailableTasks()` in one loop. Satisfaction loss is read through `Customer.getSatisfactionLossPerTurn()`.
- **Composition:** `Restaurant` owns its `Table`s, `WaitingQueue` and `GameStats`. `GameController` owns the `Restaurant` and the three staff members. An `Order` holds a `MenuItem` and a locked `Money` bill.
- **Dependency inversion:** `GameController` depends on the `GameView` interface, not on the console.

## Testing

Rule checks (plain Java, no library):

```bash
javac -d out -sourcepath src:test test/restaurantrush/RuleChecks.java      # Windows: src;test
java -cp out restaurantrush.RuleChecks
```

There are 30 checks, including the brief's section 3.3 worked example turn by turn, invalid choices, one task per turn, Cashier timing and order, and abandonment.

Reproducible victory and defeat runs are in `test-runs/` and explained in `TEST_NOTES.md`:

```bash
java -cp out restaurantrush.Main < test-runs/victory.txt
java -cp out restaurantrush.Main < test-runs/defeat.txt
```

In PowerShell: `Get-Content test-runs/victory.txt | java -cp out restaurantrush.Main`

## Video timestamps

| # | Segment | Time |
|---|---|---|
| 1 | Game launching from the submitted project | `--:--` |
| 2 | Manager choices, including a constrained staff decision | `--:--` |
| 3 | A dish going READY → SERVED → payment | `--:--` |
| 4 | A VIP or Critic behaviour | `--:--` |
| 5 | A Stage 3 feature | `--:--` |
| 6 | One victory and one defeat | `--:--` |
| 7 | Stage tag comparisons and the diagrams | `--:--` |
