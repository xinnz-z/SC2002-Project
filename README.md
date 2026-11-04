# SC2002 – Restaurant Rush

A Java-based, turn-based restaurant management game developed for the SC2002 Software Engineering group project.

## 1. Project Overview

Restaurant Rush is a command-line restaurant management simulation where the player takes the role of a restaurant manager. Players must manage limited staff resources, serve customers efficiently, prepare food orders and maximise customer satisfaction while meeting the restaurant's revenue targets.

The game is developed incrementally across three stages, demonstrating the application of Object-Oriented Programming (OOP) principles and extensible software design.

**Technologies Used**
- Programming Language: Java
- Interface: Command-Line Interface (CLI)
- Version Control: GitHub
- Java Requirement: Java 11 or later

## 2. Project Development Stages

The project consists of three independently runnable stages.

| Stage | Description | GitHub Tag |
|---|---|---|
| Stage 1 | Basic restaurant operations with Regular customers, Waiter, Chef and Cashier. | `stage-1` |
| Stage 2 | Introduces VIP and Critic customers with different satisfaction and ordering behaviours. | `stage-2` |
| Stage 3 | Introduces Host, ComboMeal and Happy Hour features. | `stage-3` |

### Stage 1 – Basic Restaurant Operations
- Regular customers and three menu items: Salad, Burger and Pasta.
- Waiter, Chef and Cashier staff roles.
- Customer seating, ordering, food preparation and payment.
- Customer satisfaction, waiting penalties and abandonment.
- Revenue tracking and victory/defeat conditions.

[View Stage 1 Source Code](./restaurant-rush)

### Stage 2 – Customer Behaviour Extension
- Introduces VIP customers with a 10% discount.
- Introduces Critic customers with a preference for the most demanding menu item.
- Implements different satisfaction-loss rates for each customer type.
- Uses inheritance and polymorphism to support different customer behaviours.

[View Stage 2 Source Code](./restaurant-rush-stage2)

### Stage 3 – New Staff and Menu Policies
- Introduces a Host who can seat one additional customer per turn.
- Introduces ComboMeal, combining two existing menu items at a 10% discount.
- Introduces Happy Hour, providing a 20% discount on new orders and satisfaction recovery for eligible customers.
- Integrates VIP discounts with Happy Hour pricing.

[View Stage 3 Source Code](./restaurant-rush-stage3)

## 3. Game Objectives and Rules

The game runs for a maximum of 18 turns.

The base setting includes:
- 2 restaurant tables.
- 1 Waiter.
- 1 Chef.
- 1 Cashier.
- 6 scheduled customer arrivals.

To achieve victory at the end of turn 18, the manager must:
1. Have at least 4 paid customers.
2. Generate at least $45 in revenue.
3. Achieve an average satisfaction of at least 60 among paid customers.

All three objectives must be achieved to win.

## 4. How to Run the Game

**Prerequisites:** Java 11 or later.

Each stage can be compiled and executed independently.

### Stage 1

```bash
cd restaurant-rush
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

### Stage 2

```bash
cd restaurant-rush-stage2
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

### Stage 3

```bash
cd restaurant-rush-stage3
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

## 5. Testing and Validation

Each development stage includes automated rule checks, test notes and reproducible gameplay scenarios.

| Stage | Automated Rule Checks |
|---|---:|
| Stage 1 | 31 |
| Stage 2 | 43 |
| Stage 3 | 71 |

Test coverage includes:
- Customer arrivals and satisfaction.
- Staff task limitations.
- Food preparation and serving sequence.
- Payment processing and revenue calculation.
- Customer abandonment.
- VIP and Critic behaviour.
- ComboMeal pricing and selection.
- Happy Hour duration and discounts.
- Host seating behaviour.
- Victory and defeat scenarios.

Recorded victory and defeat runs are included in each stage's `test-runs` folder.

## 6. GitHub Development History

The project is maintained through three stage-specific commits and Git tags.

| Development Stage | Git Tag | Release |
|---|---|---|
| Stage 1 | `stage-1` | [Stage 1 Release](../../releases/tag/stage-1) |
| Stage 2 | `stage-2` | [Stage 2 Release](../../releases/tag/stage-2) |
| Stage 3 | `stage-3` | [Stage 3 Release](../../releases/tag/stage-3) |

### Stage Comparisons

- [Stage 1 → Stage 2](../../compare/stage-1...stage-2)
- [Stage 2 → Stage 3](../../compare/stage-2...stage-3)

These comparisons document the changes introduced during each development stage.

## 7. Project Documentation

The following documentation forms part of the group project submission:

- UML Class Diagrams – Stage 1, Stage 2 and Stage 3.
- UML Sequence Diagram – Selected complex gameplay scenario.
- Group Report – Design decisions, implementation, testing and design evolution.
- Individual Reflections – Contributions and learning experiences of each group member.
- Presentation Video – Gameplay demonstration and explanation of the project.

*Links to the final report, diagrams and presentation will be added when the deliverables are uploaded.*

## 8. Team and Contributions

This project was developed collaboratively by the SC2002 project group.

Each member contributes
