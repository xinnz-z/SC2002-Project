## Run 1: Victory

**Strategy:** seat each customer on the turn they arrive, take the order the next turn and cook it immediately, and serve as soon as the dish can be served.

| Customer | Dish | Arrives | Seated | Ordered | READY | Served | READY_TO_PAY | Paid | Satisfaction recorded |
|---|---|---|---|---|---|---|---|---|---|
| C1 | Salad | 1 | 1 | 2 | 2 | 3 | 4 | 5 | 84 |
| C2 | Burger | 4 | 4 | 5 | 5 | 6 | 7 | 8 | 84 |
| C3 | Burger | 7 | 7 | 8 | 8 | 9 | 10 | 11 | 84 |
| C4 | Burger | 10 | 10 | 11 | 11 | 12 | 13 | 14 | 84 |
| C5 | Pasta | 13 | 13 | 14 | 15 | 16 | 17 | 18 | 76 |
| C6 | Burger | 16 | 17 | 18 | 18 | - | - | - | not paid |

What it shows:
- **Same-turn cooking (turn 2):** the Chef cooks C1's Salad on the same turn the Waiter takes the order.
- **Invalid choice (turn 15):** `serve C5` is refused because the Pasta is only 1/2 done. The game explains why and asks again; the Waiter waits and the Chef finishes the Pasta.
- **Constrained staff (turn 16):** C6 arrives while C5's Pasta is READY, and the Waiter can either seat C6 or serve C5. Serving C5 is the only way C5 can pay by turn 18 (served 16, READY_TO_PAY 17, paid 18).
- **C6 cannot pay** within 18 turns, as the brief intends.

**Result:** 5 paid (target 4), revenue $72.00 (target $45.00), average satisfaction 82.4 (target 60). **VICTORY.**

## Run 2: Defeat

**Strategy:** leave C1 in the queue for the first four turns, then serve customers one at a time in arrival order.

| Customer | Dish | Seated | Ordered | Served | Paid | Satisfaction recorded |
|---|---|---|---|---|---|---|
| C1 | Salad | 5 | 6 | 7 | 9 | 52 |
| C2 | Burger | 8 | 9 | 10 | 12 | 52 |
| C3 | Burger | 11 | 12 | 13 | 15 | 52 |
| C4 | Burger | 14 | 15 | 16 | 18 | 52 |
| C5 | Pasta | 17 | 18 | - | - | not paid |
| C6 | Burger | - | - | - | - | not paid |

Each paying customer spends six turns unserved (100 − 6 × 8 = 52).

**Result:** 4 paid (met), revenue $54.00 (met), average satisfaction 52.0 (**not met**, target 60). **DEFEAT.** The restaurant earned enough and served enough customers, but the delay at the start carried through to every later customer.
