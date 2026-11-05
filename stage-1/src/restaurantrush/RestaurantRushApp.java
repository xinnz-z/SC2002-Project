package restaurantrush;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.GameView;
import restaurantrush.ui.ConsoleView;

/** Entry point: plays one game in the base setting on the console. */
public RestaurantRushApp {

    public static void main(String[] args) {
        GameView view = new ConsoleView(System.in, System.out);
        new GameController(GameConfig.base(GameConfig.DEFAULT_SEED), view).run();
    }
}
