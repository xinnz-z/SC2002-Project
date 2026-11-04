
```mermaid
classDiagram
direction TB

namespace model {
    class Customer {
        <<abstract>>
        -int id
        -int arrivalTurn
        -Random tasteRandom
        -int satisfaction
        -CustomerStatus status
        -Table table
        -Order order
        +getTypeName() String
        +getSatisfactionLossPerTurn() int
        +chooseMenuItem(Menu) MenuItem
        +calculateBill(MenuItem) Money
        +describeTraits() String
        +placeOrder(Order) void
        +canPayOn(int) boolean
        +getId() int
        +getLabel() String
        +getArrivalTurn() int
        +getSatisfaction() int
        +getTurnsWaited() int
        +getStatus() CustomerStatus
    }

    class RegularCustomer
    class VIPCustomer
    class CriticCustomer

    class Restaurant {
        -List~Table~ tables
        -WaitingQueue queue
        -List~Customer~ customers
        -Menu menu
        -HappyHour happyHour
        -GameStats stats
        +admit(Customer) void
        +checkCanSeat(Customer, Table) Optional
        +seat(Customer, Table) Table
        +quoteBill(Customer, MenuItem, int) Money
        +startHappyHour(int) String
        +recoverDuringHappyHour(int) Optional
        +serve(Customer, int) void
        +finishMeals(int) List
        +collectPayment(Customer) Money
        +applyWaitingPenalties() List
        +getHappyHour() HappyHour
        +getStats() GameStats
    }

    class Table {
        -int number
        -Customer occupant
        +getNumber() int
        +isFree() boolean
        +getOccupant() Optional
    }

    class WaitingQueue {
        -List~Customer~ customers
        +contains(Customer) boolean
        +isEmpty() boolean
        +getCustomers() List
    }

    class Order {
        -Customer customer
        -MenuItem item
        -Money bill
        -int turnTaken
        -int unitsPrepared
        -int readyTurn
        -OrderStatus status
        +prepareOneUnit(int) void
        +isServableOn(int) boolean
        +isUnfinished() boolean
        +getCustomer() Customer
        +getItem() MenuItem
        +getBill() Money
        +getStatus() OrderStatus
    }

    class Menu {
        -List~MenuItem~ items
        -List~ComboMeal~ combos
        +getItems() List
        +getDishes() List
        +getCombos() List
        +size() int
        +get(int) MenuItem
        +cheapestComboWith(MenuItem) Optional
    }

    class MenuItem {
        -String name
        -Money price
        -int preparationUnits
        +getName() String
        +getPrice() Money
        +getPreparationUnits() int
    }

    class ComboMeal {
        -MenuItem first
        -MenuItem second
        +ComboMeal(MenuItem, MenuItem)
        +getFirst() MenuItem
        +getSecond() MenuItem
        +contains(MenuItem) boolean
    }

    class HappyHour {
        -int startTurn
        -int duration
        -int discountPercent
        +isAvailable() boolean
        +isActiveOn(int) boolean
        +turnsRemaining(int) int
        +apply(Money, int) Money
        +getStartTurn() int
    }

    class Money {
        -long cents
        +ofCents(long)$ Money
        +ofDollars(long)$ Money
        +getCents() long
        +plus(Money) Money
        +percentage(int) Money
        +isAtLeast(Money) boolean
        +compareTo(Money) int
        +toString() String
    }

    class GameStats {
        -Money revenue
        -int paidCount
        -int paidSatisfactionTotal
        -int servedCount
        -int unhappyDepartures
        +getRevenue() Money
        +getPaidCount() int
        +getServedCount() int
        +getUnhappyDepartures() int
        +getAverageSatisfaction() OptionalDouble
    }

    class CustomerStatus {
        <<enumeration>>
        IN_QUEUE
        SEATED
        WAITING_FOR_FOOD
        EATING
        READY_TO_PAY
        PAID
        LEFT
    }

    class OrderStatus {
        <<enumeration>>
        PREPARING
        READY
        SERVED
        PAID
        CANCELLED
    }
}

namespace staff {
    class Staff {
        <<abstract>>
        -int tasksPerTurn
        -int tasksDoneThisTurn
        +getRole() String
        +describeAvailableTasks(Restaurant, int) List
        +startTurn() void
        +hasTaskLeft() boolean
        +waitThisTurn() ActionResult
    }

    class SeatingStaff {
        <<abstract>>
        +seat(Customer, Table, Restaurant) ActionResult
    }

    class Waiter {
        +Waiter()
        +getRole() String
        +takeOrder(Customer, Restaurant, int) ActionResult
        +serve(Customer, Restaurant, int) ActionResult
        +describeAvailableTasks(Restaurant, int) List
    }

    class Host {
        +Host()
        +getRole() String
        +describeAvailableTasks(Restaurant, int) List
    }

    class Chef {
        +Chef()
        +getRole() String
        +prepare(Customer, int) ActionResult
        +describeAvailableTasks(Restaurant, int) List
    }

    class Cashier {
        +Cashier()
        +getRole() String
        +collectNextPayment(Restaurant, int) Optional
        +describeAvailableTasks(Restaurant, int) List
    }

    class ActionResult {
        -boolean success
        -String message
        +success(String)$ ActionResult
        +failure(String)$ ActionResult
        +isSuccess() boolean
        +getMessage() String
    }
}

namespace control {
    class GameController {
        -GameConfig config
        -GameView view
        -CustomerFactory customerFactory
        -Restaurant restaurant
        -Host host
        -Waiter waiter
        -Chef chef
        -Cashier cashier
        +run() GameResult
    }

    class GameConfig {
        -int maxTurns
        -int tableCount
        -List~ScheduledArrival~ arrivals
        -Menu menu
        -Objective objective
        -long seed
        +base(long)$ GameConfig
        +getArrivalsAt(int) List
        +getArrivals() List
        +getMaxTurns() int
        +getTableCount() int
        +getMenu() Menu
        +getObjective() Objective
        +getSeed() long
    }

    class CustomerFactory {
        -Random seedSource
        -int nextId
        +CustomerFactory(long)
        +createArrival(int, CustomerType) Customer
    }

    class CustomerType {
        <<enumeration>>
        REGULAR
        VIP
        CRITIC
        +create(int, int, Random) Customer
        +getLabel() String
    }

    class ScheduledArrival {
        -int turn
        -CustomerType type
        +ScheduledArrival(int, CustomerType)
        +getTurn() int
        +getType() CustomerType
    }

    class ManagerCommand {
        -CommandType type
        -int customerId
        -int tableNumber
        +waitCommand()$ ManagerCommand
        +forCustomer(CommandType, int)$ ManagerCommand
        +seat(int, int)$ ManagerCommand
        +getType() CommandType
        +getCustomerId() int
        +hasTable() boolean
        +getTableNumber() int
    }

    class CommandType {
        <<enumeration>>
    }

    class Objective {
        -int minPaidCustomers
        -Money minRevenue
        -int minAverageSatisfaction
        +evaluate(GameStats) GameResult
    }

    class GameResult {
        -Objective objective
        -GameStats stats
        -boolean paidTargetMet
        -boolean revenueTargetMet
        -boolean satisfactionTargetMet
        +getOutcome() GameOutcome
        +getStats() GameStats
    }

    class GameOutcome {
        <<enumeration>>
        VICTORY
        DEFEAT
    }

    class GameView {
        <<interface>>
        +showWelcome(GameConfig) void
        +showTurnStart(int, int, Restaurant, List, List) void
        +askCommand(Staff, Restaurant, int) ManagerCommand
        +showActionResult(ActionResult) void
        +showFinalResult(GameResult, int) void
    }
}

namespace ui {
    class ConsoleView {
        -BufferedReader in
        -PrintStream out
        -boolean echoInput
        -CommandParser parser
        +showWelcome(GameConfig) void
        +showTurnStart(int, int, Restaurant, List, List) void
        +askCommand(Staff, Restaurant, int) ManagerCommand
        +showActionResult(ActionResult) void
        +showFinalResult(GameResult, int) void
    }

    class CommandParser {
        +parse(String) ManagerCommand
    }

    class InvalidCommandException {
        +InvalidCommandException(String)
    }
}

class Main {
    +main(String[])$ void
}

Customer <|-- RegularCustomer
Customer <|-- VIPCustomer
Customer <|-- CriticCustomer

Staff <|-- SeatingStaff
Staff <|-- Chef
Staff <|-- Cashier
SeatingStaff <|-- Waiter
SeatingStaff <|-- Host

GameView <|.. ConsoleView

Restaurant "1" *-- "1..*" Table : manages
Restaurant "1" *-- "1" WaitingQueue : owns
Restaurant "1" *-- "1" GameStats : maintains
Restaurant "1" --> "1" Menu : uses
Restaurant "1" --> "0..*" Customer : tracks
Restaurant "1" *-- "1" HappyHour : manages

WaitingQueue "1" --> "0..*" Customer : queues
Customer "0..*" --> "0..1" Table : occupies
Customer "1" --> "0..1" Order : places

Order "0..*" --> "1" Customer : belongs to
Order "0..*" --> "1" MenuItem : contains
Menu "1" o-- "1..*" MenuItem : offers
Menu "1" o-- "0..*" ComboMeal : offers
ComboMeal "1" --> "2" MenuItem : combines

MenuItem --> Money : has price
ComboMeal --> MenuItem : combines
Order --> Money : stores bill
GameStats --> Money : tracks revenue
HappyHour --> Money : discounts bill

Customer --> CustomerStatus : has status
Order --> OrderStatus : has status

GameController --> GameConfig : uses
GameController --> GameView : communicates through
GameController --> CustomerFactory : creates customers
GameController *-- Restaurant : manages
GameController *-- Host : coordinates
GameController *-- Waiter : coordinates
GameController *-- Chef : coordinates
GameController *-- Cashier : coordinates

GameConfig --> Menu : configures
GameConfig --> Objective : defines
GameConfig o-- ScheduledArrival : schedules

CustomerFactory ..> CustomerType : uses
CustomerFactory ..> Customer : creates
CustomerType ..> Customer : instantiates
ScheduledArrival --> CustomerType : specifies

Objective ..> GameStats : evaluates
Objective ..> GameResult : produces
GameResult --> GameStats : contains
GameResult --> GameOutcome : determines

SeatingStaff --> Restaurant : seats customers
Waiter --> Restaurant : takes orders and serves
Chef --> Restaurant : prepares food
Cashier --> Restaurant : processes payments

Staff --> ActionResult : returns

ConsoleView --> CommandParser : uses
ConsoleView ..> ManagerCommand : produces
CommandParser ..> ManagerCommand : creates
CommandParser ..> InvalidCommandException : throws
ManagerCommand --> CommandType : identifies command

Main ..> GameController : starts
Main ..> ConsoleView : creates
```
