package restaurantrush.ui;

import java.util.Locale;

import restaurantrush.control.CommandType;
import restaurantrush.control.ManagerCommand;

/**
 * Turns typed text such as {@code seat C1}, {@code cook 2} or {@code wait} into a
 * {@link ManagerCommand}. It only checks the wording; whether the command is allowed
 * by the rules is decided later by the staff and domain objects.
 */
public class CommandParser {

    public ManagerCommand parse(String input) throws InvalidCommandException {
        String[] words = (input == null) ? new String[0] : input.trim().split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) {
            throw new InvalidCommandException("Please type a command, e.g. 'seat C1' or 'wait'.");
        }
        CommandType type = toCommandType(words[0]);
        if (type == CommandType.WAIT) {
            if (words.length > 1) {
                throw new InvalidCommandException("'wait' takes no customer.");
            }
            return ManagerCommand.waitCommand();
        }
        if (words.length != 2) {
            throw new InvalidCommandException("'" + words[0] + "' needs one customer, e.g. '" + words[0] + " C1'.");
        }
        return ManagerCommand.forCustomer(type, parseCustomerId(words[1]));
    }

    private CommandType toCommandType(String word) throws InvalidCommandException {
        switch (word.toLowerCase(Locale.ROOT)) {
            case "seat":
                return CommandType.SEAT;
            case "order":
                return CommandType.TAKE_ORDER;
            case "serve":
                return CommandType.SERVE;
            case "cook":
                return CommandType.COOK;
            case "wait":
                return CommandType.WAIT;
            default:
                throw new InvalidCommandException("Unknown command '" + word + "'. Use seat, order, serve, cook or wait.");
        }
    }

    /** Accepts C3, c3 or 3. */
    private int parseCustomerId(String word) throws InvalidCommandException {
        String digits = (word.charAt(0) == 'C' || word.charAt(0) == 'c') ? word.substring(1) : word;
        try {
            int id = Integer.parseInt(digits);
            if (id >= 1) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // explained below
        }
        throw new InvalidCommandException("'" + word + "' is not a customer. Use a form like C1.");
    }
}
