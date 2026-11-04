package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The fixed list of items customers can choose from. */
public final class Menu {

    private final List<MenuItem> items;

    public Menu(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public MenuItem get(int index) {
        return items.get(index);
    }
}
