# Stage 2 test notes

Both runs use the base setting and the default seed 2002. The same six customers arrive in both runs; only the manager's choices differ.

| Customer | Type | Arrives | Dish |
|---|---|---|---|
| C1 | Regular | 1 | Salad $9.00 |
| C2 | **Critic** | 4 | Pasta $18.00 (Critic preference) |
| C3 | **VIP** | 7 | Burger $13.50 (10% off) |
| C4 | Regular | 10 | Burger $15.00 |
| C5 | Regular | 13 | Pasta $18.00 |
| C6 | Regular | 16 | Burger $15.00 |

Reproduce each run with:

```bash
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main --echo < test-runs/victory.txt
java -cp out restaurantrush.Main --echo < test-runs/defeat.txt
```

The full transcripts are in `test-runs/victory-output.txt` and `test-runs/defeat-output.txt`.

## Run 1: Victory

**Strategy:** seat customers promptly, cook each order the turn it is taken, and when the Waiter is needed in two places, favour the customer who loses satisfaction fastest or whose payment is at risk.

| Customer | Seated | Ordered | READY | Served | Paid | Satisfaction recorded | Bill |
|---|---|---|---|---|---|---|---|
| C1 Regular | 1 | 2 | 2 | 3 | 5 | 84 | $9.00 |
| C2 Critic | 4 | 5 | 6 | 7 | 9 | 64 | $18.00 |
| C3 VIP | 8 | 9 | 9 | 10 | 12 | 85 | $13.50 |
| C4 Regular | 11 | 12 | 12 | 15 | 17 | 60 | $15.00 |
| C5 Regular | 13 | 14 | 15 | 16 | 18 | 76 | $18.00 |
| C6 Regular | 18 | - | - | - | - | not paid | - |

Decisions shown in this run:

- **Turn 7, Critic vs VIP.** The Critic's Pasta is READY and the VIP has just arrived. The Waiter serves the Critic, who would lose 12 more satisfaction, and leaves the VIP in the queue, who loses only 5. The VIP still reaches the table at 85.
- **Turn 13, payment vs satisfaction.** C4's Burger is READY and C5 has just arrived with a Pasta order to come. Serving C4 first would keep C4 at 76 but leave C5 unable to pay by turn 18. The manager seats C5 instead, so C4 waits two more turns (recorded at 60) but five customers pay instead of four.
- **Turn 17, invalid choice.** `seat C6` is refused because both tables are occupied (C4 is waiting to pay, C5 is eating). The game explains why and asks again.

**Final result:** 5 paid (target 4), revenue $73.50 (target $45.00), average satisfaction 73.8 (target 60). **VICTORY.**

If on turn 13 the manager serves C4 instead, the game still ends in victory, but with 4 paid, $55.50 and an average of 77.3. Both are wins; the choice trades one more paying customer against a higher average.

## Run 2: Defeat

**Strategy:** the manager leaves the first customer in the queue for four turns, then deals with one customer at a time in arrival order.

| Customer | Seated | Served | Paid | Turns waited before being served | Satisfaction recorded |
|---|---|---|---|---|---|
| C1 Regular | 5 | 7 | 9 | 6 | 52 |
| C2 Critic | 8 | 11 | 13 | 7 | **16** |
| C3 VIP | 12 | 14 | 16 | 7 | **65** |
| C4 Regular | 15 | 17 | - | 7 | not paid (pays turn 19 at the earliest) |
| C5 Regular | 18 | - | - | - | not paid |

**Final result:** 3 paid (target 4), revenue $40.50 (target $45.00), average satisfaction 44.3 (target 60). **DEFEAT**, with all three targets missed.

The Critic and the VIP both waited seven turns before being served. The Critic ends at 16 (7 × 12 lost) and the VIP at 65 (7 × 5 lost). This is the Stage 2 behaviour coming from the customer objects themselves, under the same turn rules.

## Rule checks

`java -cp out restaurantrush.RuleChecks` (after compiling with `-sourcepath src:test`) runs 43 checks. The Stage 2 ones are:

- the base setting still has six arrivals and includes at least one VIP and one Critic;
- after one waiting turn a Regular is at 92, a VIP at 95 and a Critic at 88;
- a VIP pays $13.50 for a Burger and $16.20 for Pasta; a Critic pays full price;
- a Critic chooses Pasta from the base menu;
- a VIP goes through the same order flow and the discounted bill is locked when the order is taken;
- with no service at all, the Critic (from turn 4) leaves at the end of turn 12, a turn before the Regular who arrived on turn 1.
