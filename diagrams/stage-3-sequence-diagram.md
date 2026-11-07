
# Stage 3 Sequence Diagram — VIP Customer During Happy Hour

This sequence diagram illustrates how a manager's decisions influence a VIP customer's satisfaction and final bill during Happy Hour. It follows the Stage 3 restaurant simulation from customer arrival to payment and final game evaluation.

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

    Note over Manager,UI: Boundary - ConsoleView handles manager input and output
    Note over GC,CF: Control - GameController coordinates the game
    Note over R,OBJ: Entity - restaurant state, customers, staff and orders

    rect rgb(235,245,235)
        Note over GC,VIP: Turn 7 - VIP customer arrives
        GC->>CF: createArrival(7, VIP)
        CF-->>GC: VIPCustomer
        GC->>R: admit(vip)
        R-->>GC: Customer added to waiting queue
    end

    rect rgb(255,240,220)
        Note over GC,HH: Manager activates Happy Hour
        GC->>UI: askStartHappyHour(restaurant, 7)
        UI->>Manager: Activate Happy Hour?
        Manager-->>UI: Yes
        UI-->>GC: true
        GC->>R: startHappyHour(7)
        R->>HH: activate(7)
        R->>R: recoverDuringHappyHour(7)
        R->>VIP: recoverSatisfaction(15)
        Note over VIP,HH: Waiting customers recover up to 15 satisfaction
    end

    rect rgb(225,237,251)
        Note over GC,H: Host seats the VIP customer
        GC->>UI: askCommand(host, restaurant, 7)
        UI->>Manager: Select Host action
        Manager-->>UI: Seat VIP
        UI-->>GC: ManagerCommand(SEAT, vipId)
        GC->>H: seat(vip, table, restaurant)
        H->>R: seat(vip, table)
        R->>VIP: seatAt(table)
        Note over VIP: IN_QUEUE to SEATED
    end

    rect rgb(255,240,220)
        Note over GC,W: Manager decides whether to prioritise the VIP order
        GC->>UI: askCommand(waiter, restaurant, 7)
        UI->>Manager: Select Waiter action

        alt Manager prioritises VIP order
            Manager-->>UI: Take VIP order
            UI-->>GC: ManagerCommand(TAKE_ORDER, vipId)
            GC->>W: takeOrder(vip, restaurant, 7)
            W->>VIP: chooseMenuItem(menu)
            VIP-->>W: selected MenuItem
            W->>R: quoteBill(vip, item, 7)
            R->>VIP: calculateBill(item)
            VIP-->>R: VIP discounted price
            R->>HH: apply(vipPrice, 7)
            HH-->>R: Happy Hour discounted price
            R-->>W: final bill
            W->>O: create Order(vip, item, finalBill)
            W->>VIP: placeOrder(order)
            Note over VIP,O: VIP discount 10 percent followed by Happy Hour 20 percent
            Note over VIP,O: Bill is locked when the order is taken

        else Manager delays VIP order
            Manager-->>UI: Wait
            UI-->>GC: ManagerCommand(WAIT)
            GC->>W: waitThisTurn()
            Note over VIP: VIP continues waiting for an order
            Note over HH: Later orders may miss the Happy Hour discount
        end
    end

    opt VIP order has been placed
        rect rgb(225,237,251)
            Note over GC,C: Chef prepares the order

            loop Until all preparation units are completed
                GC->>UI: askCommand(chef, restaurant, turn)
                UI->>Manager: Select Chef action
                Manager-->>UI: Cook VIP order
                UI-->>GC: ManagerCommand(COOK, vipId)
                GC->>C: prepare(vip, turn)
                C->>O: prepareOneUnit(turn)

                alt More preparation required
                    Note over O: Order remains PREPARING
                else All preparation completed
                    Note over O: PREPARING to READY
                    Note over O: Record readyTurn
                end
            end
        end

        rect rgb(255,240,220)
            Note over GC,W: Waiter serves the order on a later turn
            Note over GC,O: Current turn must be greater than readyTurn

            GC->>UI: askCommand(waiter, restaurant, turn)
            UI->>Manager: Select Waiter action
            Manager-->>UI: Serve VIP
            UI-->>GC: ManagerCommand(SERVE, vipId)
            GC->>W: serve(vip, restaurant, turn)
            W->>O: isServableOn(turn)
            O-->>W: true
            W->>R: serve(vip, turn)
            R->>VIP: receiveFood(turn)
            VIP->>O: markServed()
            Note over VIP,O: READY to SERVED
            Note over VIP: Customer status becomes EATING
        end

        rect rgb(225,237,251)
            Note over GC,VIP: Customer finishes eating on a later turn
            GC->>R: finishMeals(turn)
            R->>VIP: finishEatingIfDone(turn)
            Note over VIP: EATING to READY_TO_PAY
        end

        rect rgb(225,237,251)
            Note over GC,CA: Cashier automatically collects payment on a later turn
            GC->>CA: collectNextPayment(restaurant, turn)
            CA->>VIP: canPayOn(turn)
            VIP-->>CA: true
            CA->>R: collectPayment(vip)
            R->>VIP: pay()
            VIP->>O: markPaid()
            R->>GS: recordPayment(bill, satisfaction)
            Note over VIP,O: Customer becomes PAID and table is released
            Note over GS: Update revenue, paid count and satisfaction
        end
    end

    rect rgb(255,240,220)
        Note over GC,OBJ: After turn 18 - evaluate the game outcome
        GC->>OBJ: evaluate(restaurant.getStats())
        OBJ-->>GC: GameResult
        GC->>UI: showFinalResult(result, 18)
        UI-->>Manager: Display VICTORY or DEFEAT
    end
```

## Design Explanation

**Boundary:** `ConsoleView` receives manager commands and displays the game state.

**Control:** `GameController` coordinates customer arrivals, staff actions, turn progression and game evaluation.

**Entity:** `Restaurant`, `VIPCustomer`, `HappyHour`, `Host`, `Waiter`, `Chef`, `Cashier`, `Order`, `GameStats` and `Objective` manage the restaurant's business logic.

**Manager decision:** Taking the VIP order while Happy Hour is active locks in both discounts. Delaying the order may result in lower satisfaction and loss of the Happy Hour discount.

**Turn constraints:** The Chef can prepare an order after it is taken, but the Waiter cannot serve it until a later turn. The customer must finish eating before becoming eligible for payment on a subsequent turn.
