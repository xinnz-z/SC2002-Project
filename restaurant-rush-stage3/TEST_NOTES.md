# Stage 3 test notes

Both runs use the base setting and the default seed 2002. The same six customers arrive in both runs; only the manager's choices differ.

| Customer | Type | Arrives | Dish picked by seed |
|---|---|---|---|
| C1 | Regular | 1 | Salad |
| C2 | **Critic** | 4 | Burger + Pasta (Critic rule: most preparation units) |
| C3 | **VIP** | 7 | Burger |
| C4 | Regular | 10 | Burger |
| C5 | Regular | 13 | Pasta |
| C6 | Regular | 16 | Burger |

A Regular or VIP who has waited 2 or more turns before ordering is hungry and orders a combo instead, so the dish actually ordered depends on the manager.

Reproduce each run with:

```bash
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main --echo < test-runs/victory.txt
java -cp out restaurantrush.Main --echo < test-runs/defeat.txt
```

The full transcripts are in `test-runs/victory-output.txt` and `test-runs/defeat-output.txt`.

## Run 1: Victory

**Strategy:** the Host seats each customer on arrival, the Waiter takes the order the same turn, and the Chef starts cooking at once. Happy Hour is saved for turn 7, when the Critic's dish is waiting and the VIP arrives.

| Customer | Ordered | Seated | Order taken | READY | Served | Paid | Satisfaction | Bill |
|---|---|---|---|---|---|---|---|---|
| C1 Regular | Salad | 1 (Host) | 1 | 1 | 2 | 4 | 92 | $9.00 |
| C2 Critic | Burger + Pasta | 4 (Host) | 4 | 6 | 8 | 10 | 82 | $29.70 |
| C3 VIP | Burger | 7 (Host) | 7, Happy Hour | 7 | 9 | 11 | 95 | $10.80 |
| C4 Regular | Burger | 11 (Host) | 11 | 11 | 12 | 14 | 84 | $15.00 |
| C5 Regular | Pasta | 13 (Host) | 13 | 14 | 15 | 17 | 84 | $18.00 |
| C6 Regular | Burger | 16 (Host) | 16 | 16 | 17 | - | not paid | - |

Features and decisions shown in this run:

- **Host (turn 1):** the Host seats C1, the Waiter takes the order and the Chef cooks it, all in the arrival turn. C1 loses only one turn of satisfaction.
- **Invalid choice (turn 2):** `serve C1` at the Host prompt is refused because the Host can only seat.
- **ComboMeal (turn 4):** the Critic orders Burger + Pasta, 3 preparation units at $29.70 ($15 + $18 at 90%).
- **Happy Hour (turns 7-8):**
  - Started on turn 7: the waiting Critic recovers from 64 to 79.
  - The VIP's Burger is ordered during Happy Hour and priced $15 × 0.9 × 0.8 = **$10.80**, locked in.
  - On turn 8 the header shows "ACTIVE - last turn", and the Critic (67) and VIP (95) recover again.
  - The Waiter serves the Critic first at 82. That is higher than serving them on turn 7 (79), because the second recovery outweighs one more turn of waiting.
- **Table bottleneck (turn 10):** C4 arrives but both tables are taken (C2 waiting to pay, C3 eating). `seat C4` is refused with the reason. C2 pays at the end of the turn, and the Host seats C4 on turn 11. C4 has waited only one turn, so they order a Burger, not a combo.
- **C6 cannot pay** within 18 turns, as the brief intends.

**Final result:** 5 paid (target 4), revenue $82.50 (target $45.00), average satisfaction 87.4 (target 60). **VICTORY.**

## Run 2: Defeat

**Strategy:** the manager never uses the Host or Happy Hour, leaves C1 in the queue for four turns, then deals with one customer at a time in arrival order.

| Customer | Ordered | Seated | Order taken | Served | Outcome | Satisfaction | Bill |
|---|---|---|---|---|---|---|---|
| C1 Regular | Salad + Burger (hungry) | 5 | 6 | 8 | paid turn 10 | 44 | $21.60 |
| C2 Critic | Burger + Pasta | 9 | 10 | - | **left at end of turn 12**; order cancelled although READY | 0 | - |
| C3 VIP | Salad + Burger (hungry) | 13 | 14 | 16 | paid turn 18 | 55 | $19.44 |
| C4 Regular | Salad + Burger (hungry) | 17 | 18 | - | not paid | - | - |

Points shown in this run:

- **Hungry customers order combos.** C1 waited 5 turns and C3 waited 7 before ordering, so each orders the cheapest combo with their dish (Salad + Burger, $21.60). The VIP's 10% discount applies to the combo: $19.44.
- **The Critic runs out of patience.** Seated only on turn 9, already at 40, and waiting for a 3-unit combo, they reach 0 at the end of turn 12 and leave without paying. Their finished order is cancelled and their table freed.

**Final result:** 2 paid (target 4), revenue $41.04 (target $45.00), average satisfaction 49.5 (target 60). **DEFEAT**, with all three targets missed.

## Rule checks

`java -cp out restaurantrush.RuleChecks` (after compiling with `-sourcepath src:test`) runs 71 checks. The Stage 3 ones are:

- **Combos.**
  - Salad + Pasta costs $24.30 and takes 3 units; Salad + Burger costs $21.60 and takes 2.
  - The Critic now orders Burger + Pasta.
  - A customer who has waited 1 turn orders their dish; after 2 turns they order the cheapest combo with it.
  - The VIP discount applies to combos ($19.44).
- **Happy Hour.**
  - Available once, active on its start turn and the next, with 2 then 1 turns remaining.
  - Prices: Regular Burger $12.00, VIP Burger $10.80. An order placed before Happy Hour keeps its price, and there is no discount afterwards.
  - Recovery is +15, capped at 100, only for waiting customers. A second activation is refused.
- **Host.**
  - Refuses to take orders.
  - Seat, order and cook can all happen in one turn.
  - The Host and Waiter can each seat someone in the same turn; the Host cannot seat when no table is free.
- **Happy Hour in a full game:** a Salad ordered in Happy Hour pays $7.20, and the turn-2 recovery takes C1 from 92 to 100 before serving.

The 43 Stage 1 and Stage 2 checks still pass, including the section 3.3 worked example.
