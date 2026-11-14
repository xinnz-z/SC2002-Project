## Run 1: Victory

**Strategy:** seat customers promptly and cook each order the turn it is taken. When the Waiter is needed in two places, favour the customer who loses satisfaction fastest or whose payment is at risk.

| Customer | Seated | Ordered | READY | Served | Paid | Satisfaction recorded | Bill |
|---|---|---|---|---|---|---|---|
| C1 Regular | 1 | 2 | 2 | 3 | 5 | 84 | $9.00 |
| C2 Critic | 4 | 5 | 6 | 7 | 9 | 64 | $18.00 |
| C3 VIP | 8 | 9 | 9 | 10 | 12 | 85 | $13.50 |
| C4 Regular | 11 | 12 | 12 | 15 | 17 | 60 | $15.00 |
| C5 Regular | 13 | 14 | 15 | 16 | 18 | 76 | $18.00 |
| C6 Regular | 18 | - | - | - | - | not paid | - |

Decisions shown:
- **Turn 7, Critic vs VIP.** The Critic's Pasta is READY and the VIP has just arrived. The Waiter serves the Critic, who would lose 12 more, and leaves the VIP in the queue, who loses only 5. The VIP still reaches the table at 85.
- **Turn 13, payment vs satisfaction.** C4's Burger is READY and C5 has just arrived with a Pasta order to come. Serving C4 first would leave C5 unable to pay by turn 18, so the manager seats C5. C4 waits two more turns (recorded at 60), but five customers pay instead of four.
- **Turn 17, invalid choice.** `seat C6` is refused because both tables are occupied. The game explains why and asks again.

**Result:** 5 paid (target 4), revenue $73.50 (target $45.00), average satisfaction 73.8 (target 60). **VICTORY.**

## Run 2: Defeat

**Strategy:** leave the first customer in the queue for four turns, then deal with one customer at a time in arrival order.

| Customer | Seated | Served | Paid | Turns waited before being served | Satisfaction recorded |
|---|---|---|---|---|---|
| C1 Regular | 5 | 7 | 9 | 6 | 52 |
| C2 Critic | 8 | 11 | 13 | 7 | **16** |
| C3 VIP | 12 | 14 | 16 | 7 | **65** |
| C4 Regular | 15 | 17 | - | 7 | not paid |
| C5 Regular | 18 | - | - | - | not paid |

**Result:** 3 paid (target 4), revenue $40.50 (target $45.00), average satisfaction 44.3 (target 60). **DEFEAT**, with all three targets missed.

The Critic and the VIP each waited seven turns before being served. The Critic ends at 16 (7 × 12 lost) and the VIP at 65 (7 × 5 lost), which is the Stage 2 behaviour coming from the customer objects under the same turn rules.
