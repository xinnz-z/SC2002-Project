# Restaurant Rush: Stage 3

A turn-based, command-line restaurant management game in Java. You are the manager: each turn the Host may seat a customer, the Waiter does one task and the Chef does one preparation unit, and the Cashier works automatically.

**Stage 3: new role and menu policy.** Adds the Host, combo meals and Happy Hour to Stage 2. The win and lose criteria are unchanged.

Requires Java 11 or later. No libraries, no network.

## Build and run

```bash
javac -d out -sourcepath src src/restaurantrush/RestaurantRushApp.java
java -cp out restaurantrush.RestaurantRushApp
```

## How to play

Each turn runs in this order:
1. Arrivals, and the Happy Hour recovery if it is in its second turn.
2. The start-of-turn display.
3. The Happy Hour question, asked until it has been used: answer `y` or `n`.
4. The Host's task, then the Waiter's, then the Chef's.
5. Eating and payment, then the waiting penalty.

At each staff prompt, type one command. The prompt lists the commands that staff member can do right now. Customers are written `C1`, `C2`, ... (`1` also works), and commands are not case-sensitive.

| Command | Who | Effect |
|---|---|---|
| `seat C1` | Host or Waiter | Seats a queued customer at the first free table |
| `order C1` | Waiter | Takes a seated customer's order; the customer picks the food |
| `serve C1` | Waiter | Serves a dish that was already READY before this turn |
| `cook C1` | Chef | Does one preparation unit of C1's order |
| `wait` | Anyone | Does nothing this turn |

An invalid command is explained and you choose again. It does not use up the staff member's task.

## Customers and menu

| Type | Satisfaction lost per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | Full price | Seeded random dish; a combo if hungry (see below) |
| VIP | 5 | 10% off, rounded to the nearest cent | Same as Regular |
| Critic | 12 | Full price | Always the item with the most preparation units (ties: the higher price, then the first on the menu). With combos on the menu this is Burger + Pasta. |

Arrival schedule (base setting): turns 1, 4, 7, 10, 13 and 16: Regular, **Critic**, **VIP**, Regular, Regular, Regular.

| Item | Price | Units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger | $21.60 | 2 |
| Salad + Pasta | $24.30 | 3 |
| Burger + Pasta | $29.70 | 3 |

A combo costs 90% of its two components, rounded to the nearest cent, and takes the sum of their preparation units.

**Combo rule (our definition).** A Regular or VIP customer first picks a single dish at random from their seeded random source. If they have waited 2 or more turns before their order is taken, they are hungry and order the cheapest combo that includes that dish instead: Salad or Burger becomes Salad + Burger, and Pasta becomes Salad + Pasta. "Waited" counts the end-of-turn waiting penalties received, in the queue or seated. Critics always order the most demanding item.

**Happy Hour:**
- **When:** usable once per game, chosen at the start of a turn. It lasts that turn and the next, and the header shows the turns left.
- **Price:** orders taken while it is active get 20% off, applied after the VIP discount (a VIP Burger is $15 × 0.9 × 0.8 = $10.80). The bill is locked when the order is taken.
- **Recovery:** at the start of each active turn, customers still waiting for a table or food recover 15 satisfaction, up to 100.

**Host:** seats at most one queued customer per turn, in addition to the Waiter's task. The Waiter can still seat customers. The Host acts first, so a customer can be seated, ordered for and start cooking all on the turn they arrive.

## Rules as implemented

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness (seed 2002), plus the combo and Critic rules above. Seed 2002: C1 Salad, C2 Critic (Burger + Pasta), C3 Burger, C4 Burger, C5 Pasta, C6 Burger. A Regular or VIP orders a combo instead if kept waiting 2+ turns. |
| Order of staff within a turn | Host, then Waiter, then Chef |
| When Happy Hour is offered | A y/n question at the start of each turn, after the display and before the Host, until it has been used |
| When the start-of-turn display appears | After arrivals and any second-turn Happy Hour recovery |
| Which table seating uses | The first free table |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served (whether or not they have paid yet) |
| Input runs out | Every remaining choice is Wait, Happy Hour is not started, and the game still plays all 18 turns |

## Design

| Package | Classes |
|---|---|
| `model` | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Order`, `MenuItem`, `ComboMeal`, `Menu`, `HappyHour`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | `Staff` (abstract), `SeatingStaff` (abstract), `Host`, `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | `GameController`, `GameConfig`, `ScheduledArrival`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CustomerType`, `CommandType`, `GameOutcome` |
| `ui` | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

| Class | Responsible for |
|---|---|
| `GameController` | Running the 18 turns in order, offering Happy Hour, and routing each command to the right staff member |
| `SeatingStaff` | Seating, shared by the Host and the Waiter |
| `Host` / `Waiter` / `Chef` / `Cashier` | Checking whether a task is allowed now, carrying it out, one task per turn |
| `Restaurant` | Changes that touch several objects at once: seating, serving, payment, pricing a new order (the customer's price, then Happy Hour), Happy Hour recovery, the waiting penalty and departures |
| `Customer` and subclasses | Own satisfaction, status, table, order and turns waited; type-specific patience, food choice and bill |
| `HappyHour` | Whether the policy is active, how many turns remain, and the discount it applies |
| `MenuItem` / `ComboMeal` / `Menu` | Prices and preparation units; combos derived from their components; finding the cheapest combo with a dish |
| `Order` | Preparation progress, the locked bill, the READY to SERVED to PAID sequence |
| `GameStats` / `Objective` | Running totals; deciding victory or defeat |
| `ConsoleView` / `CommandParser` | Showing state and reading commands; no game rules |

## Stage 1 → Stage 2 changes

**Added:**
- `VIPCustomer`: loses 5 per turn, 10% off.
- `CriticCustomer`: loses 12 per turn, orders the most demanding dish.
- `CustomerType`: creates each customer subclass.
- `ScheduledArrival`: a turn plus a customer type.

**Modified**

| Class | Change | Reason |
|---|---|---|
| `Money` | Added `percentage(int)`; implements `Comparable` | VIP discount in whole cents; the Critic compares prices on ties |
| `GameConfig` | Arrivals hold `ScheduledArrival`; Critic on turn 4 and VIP on turn 7 | The base run must include a VIP and a Critic |
| `CustomerFactory` | `createArrival(CustomerType)` delegates to `type.create(...)` | No longer hard-codes `RegularCustomer`; seed sequence unchanged |
| `GameController` | The arrival step loops over scheduled types (2 lines) | Registration change; turn steps unchanged |
| `ConsoleView` | Stage title; arrival schedule on the welcome screen | Display only |

**Effect on the design:** the new behaviour lives in two `Customer` subclasses. The turn sequence, staff and `Restaurant` were unchanged, because they already worked through `Customer`'s methods.

**What still works:** all Stage 1 rule checks still run. C2's departure turn changed from 16 to 12 because C2 is now a Critic. Total: 43 checks, all passing.

## Stage 2 → Stage 3 changes

**Added**

| Class | Responsibility |
|---|---|
| `model/ComboMeal` | Two menu items sold together: 90% of their price, the sum of their units |
| `model/HappyHour` | The Happy Hour pricing policy: once per game, two turns, 20% off, +15 recovery |
| `staff/SeatingStaff` | Abstract parent of `Waiter` and `Host` holding the shared `seat` task |
| `staff/Host` | Seats one queued customer per turn, before the Waiter acts |

**Modified**

| Class | Change | Reason |
|---|---|---|
| `model/Menu` | Holds dishes and combos separately, with `getDishes()`, `getCombos()` and `cheapestComboWith(dish)`. `get(int)` and `size()` were removed because nothing uses them any more. | Customers must tell dishes from combos for the combo rule, without `instanceof` checks |
| `model/Customer` | Counts `turnsWaited` in the existing waiting penalty. The default `chooseMenuItem` picks a dish, then upgrades to a combo when hungry. Added package-private `recoverSatisfaction`. | The combo rule and Happy Hour recovery are customer behaviour. With fewer than 2 waiting turns the choice is the same as in Stage 2. |
| `model/Restaurant` | Owns a `HappyHour`. Added `quoteBill`, `startHappyHour`, `recoverDuringHappyHour` and `getHappyHour`; no existing method changed. | Pricing a new order and recovering waiting customers involve several customers and the policy together, which is this class's job |
| `staff/Waiter` | Now extends `SeatingStaff`, with its `seat` method moved up unchanged. `takeOrder(customer, restaurant, turn)` prices through `quoteBill`. | Share seating with the Host instead of duplicating it. Happy Hour must apply after the VIP discount, at the moment the order is taken. |
| `control/GameConfig` | The base menu adds the three combos | Combos must be orderable in the base setting |
| `control/GameController` | Adds the Host and its prompt before the Waiter; offers Happy Hour at turn start and applies the second-turn recovery; updates the `takeOrder` call | The new role and policy in the turn sequence. Steps 4 to 6 are unchanged. |
| `control/GameView` | Added `askStartHappyHour`; `showTurnStart` now receives all start-of-turn events, not just arrivals | The controller needs the manager's Happy Hour decision, and the second-turn recovery is shown at turn start |
| `ui/ConsoleView` | Stage 3 welcome text; Happy Hour status and turns left in the header; the y/n question | Display and input only |

**Unchanged:** `Order`, `MenuItem`, `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Chef`, `Cashier`, `Staff`, `Table`, `WaitingQueue`, `GameStats`, `Money`, `Objective`, `GameResult`, `CustomerFactory`, `CustomerType`, `ScheduledArrival`, `ManagerCommand`, `CommandParser`, `Main`. No files were deleted.

**Effect on the design:**
- **Combos:** because `ComboMeal` is a `MenuItem`, the Chef, `Order` and billing handle combos with no change. The Critic's existing rule chose combos with no change to `CriticCustomer` either.
- **Happy Hour:** it is a separate policy object owned by `Restaurant`. Because bills are locked into `Order` when it is taken, starting or ending Happy Hour can never change an order already placed.
- **Host:** moving `seat` into `SeatingStaff` let the Host reuse it instead of copying the Waiter's code.

**What still works:** all 43 Stage 1 and 2 checks still run, with three updates:
- the scripted turns now include a Host line;
- `takeOrder` calls use the new signature;
- the Critic check uses a menu without combos, because with combos the Critic correctly picks Burger + Pasta.

With 28 new Stage 3 checks, the total is 71, and all pass.

## Testing
Test runs explained in `TEST_NOTES.md`
