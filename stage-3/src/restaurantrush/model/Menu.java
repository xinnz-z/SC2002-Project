package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** The fixed list of items customers can choose from: single dishes, then combo meals. */
public final class Menu {

    private final List<MenuItem> dishes;
    private final List<ComboMeal> combos;
    private final List<MenuItem> items;

    public Menu(List<MenuItem> dishes) {
        this(dishes, Collections.<ComboMeal>emptyList());
    }

    public Menu(List<MenuItem> dishes, List<ComboMeal> combos) {
        if (dishes == null || dishes.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one dish");
        }
        this.dishes = Collections.unmodifiableList(new ArrayList<>(dishes));
        this.combos = Collections.unmodifiableList(new ArrayList<>(combos));
        List<MenuItem> all = new ArrayList<>(dishes);
        all.addAll(combos);
        this.items = Collections.unmodifiableList(all);
    }

    /** Every item, dishes first, then combos. */
    public List<MenuItem> getItems() {
        return items;
    }

    public List<MenuItem> getDishes() {
        return dishes;
    }

    public List<ComboMeal> getCombos() {
        return combos;
    }

    /** The cheapest combo that includes the given dish; ties go to the combo listed first. */
    public Optional<ComboMeal> cheapestComboWith(MenuItem dish) {
        ComboMeal cheapest = null;
        for (ComboMeal combo : combos) {
            if (combo.contains(dish) && (cheapest == null || combo.getPrice().compareTo(cheapest.getPrice()) < 0)) {
                cheapest = combo;
            }
        }
        return Optional.ofNullable(cheapest);
    }
}
