package restaurantrush.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import restaurantrush.control.CommandType;
import restaurantrush.control.ManagerCommand;

/**
 * Turns typed text such as {@code seat C1 T2}, {@code serve 3} or {@code wait}
 * into a {@link ManagerCommand}. It only checks the wording; whether the
 * command is allowed by the rules is decided later by the staff and domain objects.
 */
public class CommandParser {

    private static final Map<String, CommandType> VERBS = new HashMap<>();

    static {
        VERBS.put("seat", CommandType.SEAT);
        VERBS.put("order", CommandType.TAKE_ORDER);
        VERBS.put("take", CommandType.TAKE_ORDER);
        VERBS.put("serve", CommandType.SERVE);
        VERBS.put("cook", CommandType.COOK);
        VERBS.put("prep", CommandType.COOK);
        VERBS.put("prepare", CommandType.COOK);
        VERBS.put("wait", CommandType.WAIT);
        VERBS.put("w", CommandType.WAIT);
        VERBS.put("skip", CommandType.WAIT);
    }

    public ManagerCommand parse(String input) throws InvalidCommandException {
        String text = (input == null) ? "" : input.trim();
        if (text.isEmpty()) {
            throw new InvalidCommandException("Please type a command, e.g. 'seat C1', 'cook C1' or 'wait'. Type 'help' for the list.");
        }
        List<String> tokens = new ArrayList<>(Arrays.asList(text.split("\\s+")));
        String typedVerb = tokens.remove(0);
        String verb = typedVerb.toLowerCase(Locale.ROOT);
        CommandType type = VERBS.get(verb);
        if (type == null) {
            throw new InvalidCommandException("Unknown command '" + typedVerb + "'. Type 'help' for the list.");
        }
        if (verb.equals("take") && !tokens.isEmpty() && tokens.get(0).equalsIgnoreCase("order")) {
            tokens.remove(0);
        }
        if (type == CommandType.WAIT) {
            requireNoMore(tokens);
            return ManagerCommand.waitCommand();
        }
        if (tokens.isEmpty()) {
            throw new InvalidCommandException("'" + verb + "' needs a customer, e.g. '" + verb + " C1'.");
        }
        int customerId = parseNumber(tokens.remove(0), 'c', "customer");
        if (type != CommandType.SEAT) {
            requireNoMore(tokens);
            return ManagerCommand.forCustomer(type, customerId);
        }
        int tableNumber = 0;
        while (!tokens.isEmpty() && (tokens.get(0).equalsIgnoreCase("at") || tokens.get(0).equalsIgnoreCase("table"))) {
            tokens.remove(0);
        }
        if (!tokens.isEmpty()) {
            tableNumber = parseNumber(tokens.remove(0), 't', "table");
        }
        requireNoMore(tokens);
        return ManagerCommand.seat(customerId, tableNumber);
    }

    private int parseNumber(String token, char prefix, String what) throws InvalidCommandException {
        String digits = (Character.toLowerCase(token.charAt(0)) == prefix) ? token.substring(1) : token;
        try {
            int number = Integer.parseInt(digits);
            if (number >= 1) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // fall through to the explanation below
        }
        throw new InvalidCommandException("'" + token + "' is not a valid " + what + ". Use a form like "
                + Character.toUpperCase(prefix) + "1 or just 1.");
    }

    private void requireNoMore(List<String> tokens) throws InvalidCommandException {
        if (!tokens.isEmpty()) {
            throw new InvalidCommandException("Unexpected '" + String.join(" ", tokens) + "'. Type 'help' for the list.");
        }
    }
}
