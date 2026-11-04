# Stage 1 test notes

Both runs use the base setting and the default seed 2002, so customers always order: C1 Salad, C2 Burger, C3 Burger, C4 Burger, C5 Pasta, C6 Burger. The same six customers arrive in both runs; only the manager's choices differ.

Reproduce each run with:

```bash
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main --echo < test-runs/victory.txt
java -cp out restaurantrush.Main --echo < test-runs/defeat.txt
```

The full transcripts are in `test-runs/victory-output.txt` and `test-runs/defeat-output.txt`.

## Run 1: Victory

**Strategy:** seat each customer the turn they arrive, take the order the next turn and cook it immediately, serve as soon as the dish is servable.

| Customer | Dish | Arrives | Seated | Ordered | READY | Served | READY_TO_PAY | Paid | Satisfaction recorded |
|---|---|---|---|---|---|---|---|---|---|
| C1 | Salad | 1 | 1 | 2 | 2 | 3 | 4 | 5 | 84 |
| C2 | Burger | 4 | 4 | 5 | 5 | 6 | 7 | 8 | 84 |
| C3 | Burger | 7 | 7 | 8 | 8 | 9 | 10 | 11 | 84 |
| C4 | Burger | 10 | 10 | 11 | 11 | 12 | 13 | 14 | 84 |
| C5 | Pasta | 13 | 13 | 14 | 15 | 16 | 17 | 18 | 76 |
| C6 | Burger | 16 | 17 | 18 | 18 | - | - | - | not paid |

Points shown in this run:

- **Same-turn cooking (turn 2):** the Chef cooks C1's Salad in the same turn the Waiter takes the order.
- **Invalid choice (turn 15):** `serve C5` is refused because the Pasta is only 1/2 done. The game explains why and asks again; the Waiter then waits and the Chef finishes the Pasta.
- **Constrained staff (turn 16):** C6 arrives while C5's Pasta is READY. The Waiter can only seat C6 or serve C5. Serving C5 is the only way C5 can pay by turn 18 (served 16, READY_TO_PAY 17, paid 18); seating C6 instead would have left C5 unpaid.
- **C6 cannot pay** within 18 turns, as the brief intends.

**Final result:** 5 paid (target 4), revenue $72.00 (target $45.00), average satisfaction 82.4 (target 60). **VICTORY.**

## Run 2: Defeat

**Strategy:** the manager leaves C1 in the queue for the first four turns, then always seats the longest-waiting customer, takes their order and serves them, one customer at a time.

| Customer | Dish | Arrives | Seated | Ordered | Served | Paid | Satisfaction recorded |
|---|---|---|---|---|---|---|---|
| C1 | Salad | 1 | 5 | 6 | 7 | 9 | 52 |
| C2 | Burger | 4 | 8 | 9 | 10 | 12 | 52 |
| C3 | Burger | 7 | 11 | 12 | 13 | 15 | 52 |
| C4 | Burger | 10 | 14 | 15 | 16 | 18 | 52 |
| C5 | Pasta | 13 | 17 | 18 | - | - | not paid |
| C6 | Burger | 16 | - | - | - | - | not paid |

Each paying customer spends six turns unserved (100 - 6 x 8 = 52).

**Final result:** 4 paid (met), revenue $54.00 (met), average satisfaction 52.0 (**not met**, target 60). **DEFEAT.**

This shows that the satisfaction target matters on its own: the restaurant earned enough and served enough customers, but the four-turn delay at the start carried through every later customer.

## Rule checks

`java -cp out restaurantrush.RuleChecks` (after compiling with `-sourcepath src:test`) runs 31 checks, including:

- the brief's section 3.3 example: satisfaction 92, 84, 76, 76 at the end of turns 1 to 4, READY_TO_PAY on turn 5, $18.00 paid on turn 6;
- an invalid command does not use up the task; a second Waiter task in one turn is refused;
- the Cashier charges at most one customer per turn, oldest first, never in the turn they became READY_TO_PAY;
- with no service at all, C1 leaves at the end of turn 13 and C2 at the end of turn 16, the average shows N/A, and the result is DEFEAT;
- $9 + $18 at 90% is $24.30, and a $15 Burger at 90% then 80% is $10.80.
