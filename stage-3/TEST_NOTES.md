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

What it shows:
- **Host (turn 1):** the Host seats C1, the Waiter takes the order and the Chef cooks it, all on the arrival turn.
- **Invalid choice (turn 2):** `serve C1` at the Host prompt is refused because the Host can only seat.
- **ComboMeal (turn 4):** the Critic orders Burger + Pasta: 3 preparation units at $29.70 ($15 + $18 at 90%).
- **Happy Hour (turns 7–8):**
  - Started on turn 7: the waiting Critic recovers from 64 to 79.
  - The VIP's Burger is ordered during Happy Hour and priced $15 × 0.9 × 0.8 = **$10.80**, locked in.
  - On turn 8 the header shows one turn left, and the Critic and VIP recover again.
  - The Waiter serves the Critic at 82. That's higher than serving them on turn 7 (79), because the second recovery outweighs one more turn of waiting.
- **Table bottleneck (turn 10):** both tables are taken, so `seat C4` is refused with the reason. C2 pays at the end of the turn, and the Host seats C4 on turn 11. C4 has waited only one turn, so orders a Burger, not a combo.

**Result:** 5 paid (target 4), revenue $82.50 (target $45.00), average satisfaction 87.4 (target 60). **VICTORY.**

## Run 2: Defeat

**Strategy:** never use the Host or Happy Hour, leave C1 in the queue for four turns, then deal with one customer at a time in arrival order.

| Customer | Ordered | Seated | Order taken | Served | Outcome | Satisfaction | Bill |
|---|---|---|---|---|---|---|---|
| C1 Regular | Salad + Burger (hungry) | 5 | 6 | 8 | paid turn 10 | 44 | $21.60 |
| C2 Critic | Burger + Pasta | 9 | 10 | - | **left at end of turn 12**; order cancelled although READY | 0 | - |
| C3 VIP | Salad + Burger (hungry) | 13 | 14 | 16 | paid turn 18 | 55 | $19.44 |
| C4 Regular | Salad + Burger (hungry) | 17 | 18 | - | not paid | - | - |

What it shows:
- **Hungry customers order combos:** C1 waited 5 turns and C3 waited 7 before ordering, so each orders the cheapest combo with their dish. The VIP discount applies to the combo, giving $19.44.
- **The Critic runs out of patience:** seated on turn 9 already at 40 and waiting for a 3-unit combo, the Critic reaches 0 at the end of turn 12 and leaves without paying.

**Result:** 2 paid (target 4), revenue $41.04 (target $45.00), average satisfaction 49.5 (target 60). **DEFEAT**, with all three targets missed.

## Rule checks

`java -cp out restaurantrush.RuleChecks` runs 71 checks: the 43 from Stages 1 and 2, plus the Stage 3 ones below.

**Combos:**
- Salad + Pasta costs $24.30 with 3 units; Salad + Burger costs $21.60 with 2.
- The Critic orders Burger + Pasta.
- A customer who has waited 1 turn orders their dish; after 2 turns, the cheapest combo with it.
- The VIP discount applies to combos.

**Happy Hour:**
- It can be used once, is active on its start turn and the next, and shows 2 then 1 turns remaining.
- Prices: a Regular Burger is $12.00 and a VIP Burger $10.80. An earlier order keeps its price, and there's no discount afterwards.
- Recovery is +15, capped at 100, for waiting customers only. A second activation is refused.

**Host:**
- It cannot take orders.
- Seat, order and cook can all happen on one turn.
- The Host and Waiter can each seat someone on the same turn; the Host cannot seat when no table is free.
- In a full game, a Salad ordered in Happy Hour pays $7.20.
