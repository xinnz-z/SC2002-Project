# Restaurant Rush: Stage 3

A turn-based, command-line restaurant management game in Java. You are the manager: each turn the Host may seat a customer, the Waiter does one task and the Chef does one preparation unit; the Cashier works automatically.

**Stage 3** adds the Host, combo meals and Happy Hour to Stage 2 (Regular, VIP and Critic customers, the full turn sequence, victory and defeat). Win and lose criteria are unchanged.

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

71 checks: the brief's section 3.3 worked example turn by turn, invalid choices, one task per turn, Cashier timing and order, abandonment, money rounding, VIP and Critic behaviour, combo prices and choices, Happy Hour pricing, duration and recovery, and the Host.

## How to play

Each turn runs in this order:
1. Arrivals, and Happy Hour recovery if it is in its second turn.
2. The start-of-turn display.
3. The Happy Hour question (only until it has been used): answer `y` or `n`, where Enter means no.
4. The Host's task, then the Waiter's, then the Chef's.
5. Eating and payment, then the waiting penalty.

At each staff prompt, type one command. Customers are `C1`, `C2`, ... (or just `1`); tables are `T1`, `T2` (or `1`). Commands are case-insensitive; anything after `#` is ignored.

| Command | Who | What it does |
|---|---|---|
| `seat C1` | Host or Waiter | Seats a queued customer at the first free table |
| `seat C1 T2` | Host or Waiter | Seats a queued customer at a chosen table |
| `order C1` | Waiter | Takes a seated customer's order; the customer picks the food |
| `serve C1` | Waiter | Serves a dish that was already READY before this turn |
| `cook C1` | Chef | Does one preparation unit of C1's order |
| `wait` | Anyone | Does nothing this turn |
| `help` | Anyone | Lists the commands |

An invalid command is explained and you choose again; it does not use up the staff member's task.

## Customers and menu

| Type | Satisfaction lost per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | Full price | Seeded random dish; a combo if hungry (see below) |
| VIP | 5 | 10% off, rounded to the nearest cent | Same as Regular |
| Critic | 12 | Full price | Always the item with the most preparation units (ties: higher price, then first on the menu). With combos on the menu this is Burger + Pasta. |

Arrival schedule (base setting): turns 1, 4, 7, 10, 13 and 16, as Regular, **Critic**, **VIP**, Regular, Regular, Regular.

| Item | Price | Units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger | $21.60 | 2 |
| Salad + Pasta | $24.30 | 3 |
| Burger + Pasta | $29.70 | 3 |

A combo costs 90% of its two components, rounded to the nearest cent, and takes the sum of their preparation units.

**Combo rule (our definition).** A Regular or VIP customer first picks a single dish at random from their seeded random source. If they have waited 2 or more turns before their order is taken, they are hungry and order the cheapest combo that includes that dish instead. Salad or Burger becomes Salad + Burger; Pasta becomes Salad + Pasta. "Waited" counts the end-of-turn waiting penalties they have received, whether in the queue or seated. Critics always order the most demanding item, which is a combo once combos are on the menu.

## Happy Hour

- **When:** usable once per game, chosen at the start of a turn. It lasts that turn and the next, and the header shows its remaining duration.
- **Price:** orders taken while it is active get 20% off, applied after the VIP discount (a VIP Burger is $15 × 0.9 × 0.8 = $10.80). The bill is locked when the order is taken, so orders placed before or after are unaffected.
- **Recovery:** at the start of each active turn, customers still waiting for a table or food recover 15 satisfaction, up to 100. Customers who are eating, waiting to pay, or gone are unaffected.

## Host

The Host seats at most one queued customer per turn, in addition to the Waiter's task, and the Waiter can still seat customers. The Host acts first, so a customer can be seated by the Host, have their order taken by the Waiter and start cooking all in the turn they arrive.

## Rules as implemented

Where the brief leaves a choice to the group, this is what the code does:

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness, per customer, plus the combo and Critic rules above. With the same seed every customer always picks the same dish; whether a Regular or VIP upgrades to a combo depends on how long the manager keeps them waiting. Seed 2002: C1 Salad, C2 Critic (Burger + Pasta), C3 Burger, C4 Burger, C5 Pasta, C6 Burger. |
| Order of staff within a turn | Host, then Waiter, then Chef |
| When Happy Hour is offered | A yes/no question at the start of each turn, after the display and before the Host, until it has been used |
| When the start-of-turn display appears | After arrivals and any second-turn Happy Hour recovery |
| Which table `seat` uses | The first free table, unless the manager names one |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served (whether or not they have paid yet) |
| Running out of input | Every remaining choice becomes Wait, Happy Hour is not started, and the game still plays all 18 turns |

## Design

### Packages

| Package | Role | Classes |
|---|---|---|
| `model` | Entities and their rules | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Order`, `MenuItem`, `ComboMeal`, `Menu`, `HappyHour`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | Who may do what, and how often | `Staff` (abstract), `SeatingStaff` (abstract), `Host`, `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | Turn sequence and setup | `GameController`, `GameConfig`, `ScheduledArrival`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CustomerType`, `CommandType`, `GameOutcome` |
| `ui` | Console input and output only | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

### Responsibilities

| Class | Responsible for |
|---|---|
| `GameController` | Running 18 turns in order; offering Happy Hour; routing each command to the right staff member |
| `SeatingStaff` | Seating, shared by the Host and the Waiter |
| `Host` / `Waiter` / `Chef` / `Cashier` | Checking whether a task is allowed now, carrying it out, one task per turn |
| `Restaurant` | Changes that touch several objects at once: seating, serving, payment, pricing a new order (customer discount then Happy Hour), Happy Hour recovery, the waiting penalty and departures |
| `Customer` and subclasses | Own satisfaction, status, table, order and turns waited; type-specific behaviour: patience, food choice, bill, a description of these traits |
| `HappyHour` | When the policy is active, how many turns remain, and the discount it applies |
| `MenuItem` / `ComboMeal` / `Menu` | Prices and preparation units; combos derived from their components; finding the cheapest combo with a dish |
| `Order` | Preparation progress, locked bill, the READY to SERVED to PAID sequence |
| `GameStats` / `Objective` | Running totals; deciding victory or defeat |
| `ConsoleView` | Showing state and reading commands; no game rules |

### Where the OO principles appear

- **Encapsulation.** `Money` stores whole cents and is immutable. `Order` and `Customer` change state only through guarded methods that throw if called out of sequence. `Table.occupy` / `release`, `HappyHour.start`, `Customer.recoverSatisfaction` and the `GameStats` recorders are package-private, so only the model can change them. Collections are returned as unmodifiable views.
- **Inheritance.**
  - `RegularCustomer`, `VIPCustomer` and `CriticCustomer` extend `Customer`.
  - `ComboMeal` extends `MenuItem`.
  - `Host` and `Waiter` extend `SeatingStaff`; `SeatingStaff`, `Chef` and `Cashier` extend `Staff`.
- **Polymorphism.**
  - Customer behaviour is dispatched through the customer object, never by checking its type: `getSatisfactionLossPerTurn()`, `chooseMenuItem()`, `calculateBill()`, `describeTraits()`.
  - A `ComboMeal` is used wherever a `MenuItem` is: the Chef, `Order` and billing handle it without knowing it is a combo.
  - The display calls `describeAvailableTasks()` on every `Staff` member in one loop.
  - The controller seats through any `SeatingStaff`.
- **Composition.** `Restaurant` owns its `Table`s, `WaitingQueue`, `HappyHour` and `GameStats`; `GameController` owns the `Restaurant` and four staff members; a `ComboMeal` is composed of two `MenuItem`s; an `Order` holds a `MenuItem` and a locked `Money` bill.
- **Open/closed.**
  - Stage 2 added customer types without touching the turn sequence or staff.
  - In Stage 3, `ComboMeal` needed no change to `MenuItem`, `Order` or `Chef`.
  - The Critic's existing rule chose combos with no change to `CriticCustomer`.
- **Liskov substitution.** Any `Customer` subclass, any `MenuItem` (including `ComboMeal`) and either `SeatingStaff` can be used where its parent type is expected without breaking the life-cycle rules.
- **Dependency inversion.** `GameController` depends on the `GameView` interface. `ConsoleView` implements it for play; `ScriptedView` (in `test/`) implements it for automated checks.

## Stage 1 → Stage 2 changes

Compare link: `<add your GitHub compare link, e.g. https://github.com/OWNER/REPO/compare/stage-1...stage-2>`

**New files**

| File | Purpose |
|---|---|
| `model/VIPCustomer.java` | Loses 5 per waiting turn; overrides `calculateBill` for 10% off |
| `model/CriticCustomer.java` | Loses 12 per waiting turn; overrides `chooseMenuItem` with the most-demanding-item preference |
| `control/CustomerType.java` | Enum of customer kinds; each constant creates its own subclass |
| `control/ScheduledArrival.java` | One schedule entry: turn plus customer type |

**Changed files**

| File | Change | Reason |
|---|---|---|
| `model/Customer.java` | Added `describeTraits()` | So each type can describe itself on arrival without type checks |
| `control/GameConfig.java` | Arrivals hold `ScheduledArrival`; Critic on turn 4, VIP on turn 7 | The base run must include a VIP and a Critic |
| `control/CustomerFactory.java` | `createArrival(turn, type)` delegates to `type.create(...)` | No longer hard-codes `RegularCustomer`; seed sequence unchanged |
| `control/GameController.java` | Arrival step uses the scheduled type and shows traits (4 lines) | Registration change; turn steps unchanged |
| `ui/ConsoleView.java` | Stage title; arrival schedule on the welcome screen | Display only |
| `test/RuleChecks.java` | Updated factory signature and abandonment checks; Stage 2 section | Covers the new behaviour |

No files were deleted. `Restaurant`, `Order`, all staff classes and the turn sequence were unchanged.

## Stage 2 → Stage 3 changes

Compare link: `<add your GitHub compare link, e.g. https://github.com/OWNER/REPO/compare/stage-2...stage-3>`

**New files**

| File | Purpose |
|---|---|
| `model/ComboMeal.java` | Two menu items sold together: 90% of their price, sum of their units |
| `model/HappyHour.java` | The Happy Hour pricing policy: once per game, two turns, 20% off, +15 recovery |
| `staff/SeatingStaff.java` | Abstract parent of `Waiter` and `Host` holding the shared `seat` task |
| `staff/Host.java` | Seats one queued customer per turn |

**Changed files**

| File | Change | Reason |
|---|---|---|
| `model/Menu.java` | Holds dishes and combos separately; added `getDishes()`, `getCombos()`, `cheapestComboWith(dish)`; the one-list constructor still works | Customers need to tell dishes from combos for the combo rule, without `instanceof` checks |
| `model/Customer.java` | Counts `turnsWaited` in the existing waiting penalty; default `chooseMenuItem` picks from dishes, then upgrades to a combo when hungry; added package-private `recoverSatisfaction` | The combo rule and Happy Hour recovery are customer behaviour. With fewer than 2 waiting turns the pick is the same as in Stage 2. |
| `model/Restaurant.java` | Owns a `HappyHour`; added `quoteBill`, `startHappyHour`, `recoverDuringHappyHour`, `getHappyHour` (no existing method changed) | Pricing a new order and recovering waiting customers touch several customers and the policy together, which is this class's job |
| `staff/Waiter.java` | Now extends `SeatingStaff` (its `seat` method moved there unchanged); `takeOrder` takes the `Restaurant` and prices through `quoteBill` | Share seating with the Host instead of duplicating it; Happy Hour must apply after the VIP discount at the moment the order is taken |
| `control/GameConfig.java` | Base menu adds the three combos | Combos must be orderable in the base setting |
| `control/GameController.java` | Adds the Host and its prompt before the Waiter; offers Happy Hour at turn start and applies second-turn recovery; seat helper takes any `SeatingStaff`; `takeOrder` call updated | New role and policy in the turn sequence. Steps 4 to 6 are unchanged. |
| `control/GameView.java` | Added `askStartHappyHour`; `showTurnStart` takes general start events | The controller needs the manager's Happy Hour decision |
| `ui/ConsoleView.java` | Stage 3 welcome text with combos and Happy Hour; Happy Hour status and remaining duration in the header; the Happy Hour question; input reading shared between prompts | Display and input only |
| `test/RuleChecks.java`, `test/ScriptedView.java` | Host line in scripted turns; new `takeOrder` signature; three Stage 3 sections | Covers the new features |

No files were deleted. `Order`, `MenuItem`, `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Chef`, `Cashier`, `Staff`, `Table`, `WaitingQueue`, `GameStats`, `Money`, `Objective`, `CustomerFactory`, `CustomerType`, `CommandParser` and `Main` are unchanged.

## Files

```
src/restaurantrush/          game source (Main + model, staff, control, ui)
test/restaurantrush/         RuleChecks and ScriptedView
test-runs/                   Stage 3 victory and defeat input scripts and their full transcripts
TEST_NOTES.md                the reproducible victory and defeat runs explained
```
