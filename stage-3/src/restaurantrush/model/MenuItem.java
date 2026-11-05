package restaurantrush.model;

/**
 * Something a customer can order: a name, a price and the number of
 * preparation units the Chef must spend on it.
 */
public class MenuItem {

    private final String name;
    private final Money price;
    private final int preparationUnits;

    public MenuItem(String name, Money price, int preparationUnits) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A menu item needs a name");
        }
        if (price == null) {
            throw new IllegalArgumentException("A menu item needs a price");
        }
        if (preparationUnits < 1) {
            throw new IllegalArgumentException("Preparation units must be at least 1");
        }
        this.name = name;
        this.price = price;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    @Override
    public String toString() {
        return name;
    }
}
