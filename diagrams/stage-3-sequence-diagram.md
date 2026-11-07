# Stage 3 Sequence Diagram — VIP Customer During Happy Hour

This scenario follows the Stage 3 turn sequence and illustrates how the manager's choices affect the VIP customer's satisfaction and locked-in bill. It is a representative path, not a claim that every randomised run produces the same menu selection or victory result.

```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant UI as ConsoleView
    participant GC as GameController
    participant CF as CustomerFactory
    participant R as Restaurant
    participant VIP as VIPCustomer
    participant HH as HappyHour
    participant H as Host
    participant W as Waiter
    participant O as Order
    participant C as Chef
    participant CA as Cashier
    participant GS as GameStats
    participant OBJ as Objective

    rect rgb(235, 245, 235)
        Note over Manager,UI: Boundary: manager enters commands through ConsoleView
    end
    rect rgb(255, 240, 220)
        Note over GC,CF: Control: GameController coordinates each turn and CustomerFactory creates arrivals
    end
    rect rgb(225, 237, 251)
        Note over R,OBJ: Domain/entity objects: restaurant, customers, staff, orders, discounts and statistics
    end

    Note over GC,VIP: Turn 7 — scheduled VIP arrival
    GC->>CF: createArrival(VIP)
    CF-->>GC: vip: VIPCustomer
    GC->>R: admit(vip)
    R-->>GC: VIP joins waiting queue
    GC->>R: recoverDuringHappyHour(7)
    Note right of R: No recovery yet if Happy Hour has not started
    GC->>UI: showTurnStart(...)
    GC->>UI: askStartHappyHour(restaurant, 7)
    UI->>Manager: Activate Happy Hour?
    Manager-->>UI: Yes
    UI-->>GC: true
    GC->>R: startHappyHour(7)
    R->>HH: start(7)
    R->>R: recoverDuringHappyHour(7)
    R->>VIP: recoverSatisfaction(15)
    Note over VIP,HH: Only waiting customers recover, capped at 100

    GC->>UI: askCommand(host, restaurant, 7)
    UI->>Manager: Host action?
    Manager-->>UI: seat VIP
    UI-->>GC: ManagerCommand(SEAT, vipId)
    GC->>H: seat(vip, restaurant)
    H->>R: seat(vip)
    R->>VIP: seatAt(freeTable)
    Note over VIP: IN_QUEUE to SEATED

    GC->>UI: askCommand(waiter, restaurant, 7)
    UI->>Manager: Waiter action?
    alt Manager prioritises VIP order
        Manager-->>UI: order VIP
        UI-->>GC: ManagerCommand(TAKE_ORDER, vipId)
        GC->>W: takeOrder(vip, restaurant, 7)
        W->>VIP: chooseMenuItem(menu)
        VIP-->>W: selected MenuItem
        W->>R: quoteBill(vip, item, 7)
        R->>VIP: calculateBill(item)
        VIP-->>R: VIP price (90% of menu price)
        R->>HH: apply(vipPrice, 7)
        HH-->>R: discounted price (80% of VIP price)
        R-->>W: final bill
        W->>O: new Order(vip, item, finalBill)
        W->>VIP: placeOrder(order)
        Note over VIP,O: WAITING_FOR_FOOD; bill locked when order is taken
    else Manager delays order
        Manager-->>UI: wait
        UI-->>GC: ManagerCommand(WAIT)
        GC->>W: waitThisTurn()
        Note over VIP: VIP continues waiting; loses satisfaction at turn end
        Note over HH: Happy Hour discount is not guaranteed for a later order
    end

    opt VIP order was placed
        loop Each turn until all preparation units are complete
            GC->>UI: askCommand(chef, restaurant, turn)
            UI->>Manager: Chef action?
            Manager-->>UI: cook VIP
            UI-->>GC: ManagerCommand(COOK, vipId)
            GC->>C: prepare(vip, turn)
            C->>O: prepareOneUnit(turn)
            alt More preparation units required
                Note over O: PREPARING
            else Last unit completed
                Note over O: PREPARING to READY; record readyTurn
            end
        end

        Note over GC,O: A later turn: turn greater than readyTurn
        GC->>UI: askCommand(waiter, restaurant, turn)
        UI->>Manager: Waiter action?
        Manager-->>UI: serve VIP
        UI-->>GC: ManagerCommand(SERVE, vipId)
        GC->>W: serve(vip, restaurant, turn)
        W->>O: isServableOn(turn)
        O-->>W: true
        W->>R: serve(vip, turn)
        R->>VIP: receiveFood(turn)
        VIP->>O: markServed()
        Note over VIP,O: Order READY to SERVED; customer EATING

        Note over GC,VIP: Next turn: customer completes eating
        GC->>R: finishMeals(turn)
        R->>VIP: finishEatingIfDone(turn)
        Note over VIP: EATING to READY_TO_PAY

        Note over GC,CA: Following turn: automatic cashier payment
        GC->>CA: collectNextPayment(restaurant, turn)
        CA->>VIP: canPayOn(turn)
        VIP-->>CA: true (if oldest eligible)
        CA->>R: collectPayment(vip)
        R->>VIP: pay()
        VIP->>O: markPaid()
        Note over VIP,O: PAID; table released
        R->>GS: recordPayment(bill, satisfaction)
        Note over GS: Revenue, paid count and paid satisfaction updated
    end

    Note over GC,OBJ: After turn 18 — evaluate the entire game
    GC->>OBJ: evaluate(restaurant.getStats())
    OBJ-->>GC: GameResult (VICTORY or DEFEAT)
    GC->>UI: showFinalResult(result, 18)
    UI-->>Manager: Display game outcome
```
