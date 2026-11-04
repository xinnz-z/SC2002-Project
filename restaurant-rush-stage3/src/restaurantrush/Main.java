package restaurantrush;

import restaurantrush.control.GameConfig;
import restaurantrush.control.GameController;
import restaurantrush.control.GameView;
import restaurantrush.ui.ConsoleView;

/**
 * Entry point. Wires the base setting, the console view and the controller together.
 * <pre>
 *   java -cp out restaurantrush.Main             (default seed 2002)
 *   java -cp out restaurantrush.Main --seed 7
 *   java -cp out restaurantrush.Main --echo &lt; test-runs/victory.txt
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        long seed = GameConfig.DEFAULT_SEED;
        boolean echo = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--seed") && i + 1 < args.length) {
                try {
                    seed = Long.parseLong(args[++i]);
                } catch (NumberFormatException e) {
                    exitWithUsage("The seed must be a whole number, not '" + args[i] + "'.");
                }
            } else if (arg.equals("--echo")) {
                echo = true;
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printUsage();
                return;
            } else {
                exitWithUsage("Unknown option '" + arg + "'.");
            }
        }
        GameView view = new ConsoleView(System.in, System.out, echo);
        new GameController(GameConfig.base(seed), view).run();
    }

    private static void exitWithUsage(String problem) {
        System.err.println(problem);
        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.out.println("Usage: java -cp out restaurantrush.Main [--seed N] [--echo]");
        System.out.println("  --seed N  seed for customers' food choices (default " + GameConfig.DEFAULT_SEED + ")");
        System.out.println("  --echo    print each command read, useful when input comes from a file");
    }
}
