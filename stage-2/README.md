# Restaurant Rush: Stage 2

A turn-based, command-line restaurant management game in Java. You are the manager: each turn you give the Waiter one task and the Chef one preparation unit, and the Cashier works automatically.

**Stage 2: customer behaviour.** Adds VIP and Critic customers to Stage 1. The win and lose criteria are unchanged.

Requires Java 11 or later. No libraries, no network.

## Build and run

```bash
javac -d out -sourcepath src src/restaurantrush/RestaurantRushApp.java
java -cp out RestaurantRushApp.Main
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

## Customer types

| Type | Satisfaction lost per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | Full price | Seeded random pick from the menu |
| VIP | 5 | 10% off, rounded to the nearest cent (Burger $13.50) | Seeded random pick from the menu |
| Critic | 12 | Full price | **Always the dish that takes the most preparation units** (ties: the higher price, then the first on the menu). On the base menu this is Pasta. |

**Critic behaviour (our definition):** a Critic tests the kitchen by ordering its most demanding dish. Together with losing 12 satisfaction per waiting turn, this makes the Critic the customer the manager must prioritise.

All three types follow the same seating, ordering, serving, eating and payment flow.

**Arrival schedule (base setting):** six arrivals on turns 1, 4, 7, 10, 13 and 16: Regular, **Critic**, **VIP**, Regular, Regular, Regular.

## Rules as implemented

| Decision | Implementation |
|---|---|
| How customers choose food | Seeded randomness (seed 2002) for Regular and VIP customers, with each customer's random source derived from the seed in arrival order. Critics use their fixed preference. Seed 2002 gives C1 Salad, C2 Pasta (Critic), C3 Burger (VIP), C4 Burger, C5 Pasta, C6 Burger. |
| When the start-of-turn display appears | After step 1 (arrivals) |
| Which table seating uses | The first free table |
| Cashier "oldest first" | The customer who became READY_TO_PAY earliest; ties go to the earlier arrival |
| "Served count" | Customers who have been served (whether or not they have paid yet) |
| Input runs out | Every remaining choice is Wait, and the game still plays all 18 turns |

## Design

| Package | Classes |
|---|---|
| `model` | `Restaurant`, `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `Order`, `MenuItem`, `Menu`, `Table`, `WaitingQueue`, `GameStats`, `Money`, enums `CustomerStatus`, `OrderStatus` |
| `staff` | `Staff` (abstract), `Waiter`, `Chef`, `Cashier`, `ActionResult` |
| `control` | `GameController`, `GameConfig`, `ScheduledArrival`, `CustomerFactory`, `Objective`, `GameResult`, `ManagerCommand`, `GameView` (interface), enums `CustomerType`, `CommandType`, `GameOutcome` |
| `ui` | `ConsoleView`, `CommandParser`, `InvalidCommandException` |

**Polymorphism for customer behaviour:** each behaviour is dispatched through the customer object, never by checking its type:
- `Restaurant.applyWaitingPenalties` uses `getSatisfactionLossPerTurn()`, which is 8, 5 or 12;
- `Waiter.takeOrder` calls `chooseMenuItem()` (random, or the Critic's preference) and `calculateBill()` (full price, or the VIP discount).

Each `CustomerType` constant overrides `create(...)`, so there is no switch on customer type anywhere.

## Stage 1 → Stage 2 changes

**Added**

| Class | Responsibility |
|---|---|
| `model/VIPCustomer` | Loses 5 per waiting turn; overrides `calculateBill` for 10% off |
| `model/CriticCustomer` | Loses 12 per waiting turn; overrides `chooseMenuItem` with the most-demanding-dish preference |
| `control/CustomerType` | The kinds of customer that can be scheduled; each constant creates its own subclass |
| `control/ScheduledArrival` | One schedule entry: a turn and a customer type |

**Modified**

| Class | Change | Reason |
|---|---|---|
| `model/Money` | Added `percentage(int)`, and now implements `Comparable` | The VIP discount needs a percentage rounded to whole cents; the Critic compares dishes by price when preparation units tie |
| `control/GameConfig` | The arrival list holds `ScheduledArrival` (turn and type) instead of turn numbers; the base setting replaces two Regular arrivals with a Critic (turn 4) and a VIP (turn 7) | The brief requires the base run to include a VIP and a Critic, so the schedule must say which type arrives when |
| `control/CustomerFactory` | `createArrival()` became `createArrival(CustomerType)` and delegates to `type.create(...)` | The factory no longer hard-codes `RegularCustomer`. The seed sequence is unchanged, so non-Critic customers order the same dishes as in Stage 1. |
| `control/GameController` | The arrival step loops over the scheduled types (2 lines) | Small registration change; the six turn steps and command handling are unchanged |
| `ui/ConsoleView` | Stage title; the welcome screen lists the arrival schedule with types | Display only |

**Unchanged:** `Customer`, `RegularCustomer`, `Order`, `Restaurant`, `Table`, `WaitingQueue`, `GameStats`, `MenuItem`, `Menu`, all `staff` classes, `Objective`, `GameResult`, `ManagerCommand`, `CommandParser`, `Main`. No files were deleted.

**Effect on the design:** the new behaviour lives entirely in two new `Customer` subclasses. The turn sequence, the staff and `Restaurant` needed no change, because they already worked through `Customer`'s methods. Only the setup classes (`GameConfig`, `CustomerFactory`) had to learn that arrivals now have a type.

**What still works:** the 30 Stage 1 rule checks still run in Stage 2, including the section 3.3 worked example, invalid choices and Cashier rules. Two changes were made to them:
- the test factory uses the new `createArrival(CustomerType)` signature;
- the no-service check now expects C2 to leave at the end of turn 12 instead of 16, because C2 is now a Critic who loses 12 per turn. Two checks were added for the Critic and VIP in that game.

With 11 new VIP and Critic checks, the total is 43, and all pass.

## Testing
Test runs explained in `TEST_NOTES.md`

## Video timestamps
