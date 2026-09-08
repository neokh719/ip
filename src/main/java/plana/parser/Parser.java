package plana.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plana.client.Client;
import plana.command.AddCommand;
import plana.command.ClientAction;
import plana.command.ClientCommand;
import plana.command.Command;
import plana.command.CommandType;
import plana.command.DeleteCommand;
import plana.command.ExitCommand;
import plana.command.FindCommand;
import plana.command.HelpCommand;
import plana.command.InvalidCommand;
import plana.command.ListCommand;
import plana.command.MarkCommand;
import plana.command.OnCommand;
import plana.command.UnmarkCommand;
import plana.exception.PlanaException;
import plana.task.Deadline;
import plana.task.Event;
import plana.task.ToDo;

/**
 * Converts raw console input into the command type and arguments used by
 * Plana's command loop.
 */
public class Parser {
    private static final String EMPTY_COMMAND_ERROR = "Oops, I didn't catch a command."
            + " Type 'help' to see what I can do :>";
    private static final String TODO_DESCRIPTION_ERROR = "Oops, a ToDo description can't be empty."
            + " Try: todo <description>.";
    private static final String DEADLINE_USAGE = "Try: deadline <description> /by <date>.";
    private static final String EVENT_USAGE = "Try: event <description> /from <start> /to <end>.";
    private static final String DATE_FORMAT_HINT = "Use the date format yyyy-MM-dd, like 2019-10-15.";
    private static final String CLIENT_ADD_USAGE = "Try: client add <name> /email <email>.";
    private static final String CLIENT_FIELD_USAGE = "Use /phone, /address, /preferences, or /notes.";
    private static final String CLIENT_EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final Pattern CLIENT_FIELD_PATTERN = Pattern.compile(
            "(?:^|\\s)/(name|email|phone|address|preferences|notes)(?=\\s|$)");
    private static final Pattern CLIENT_MARKER_PATTERN = Pattern.compile(
            "(?:^|\\s)/([A-Za-z][A-Za-z0-9_-]*)(?=\\s|$)");

    /**
     * Parses one complete line entered by the user.
     *
     * @param input the complete command entered by the user.
     * @return the recognized command and its trimmed arguments
     */
    public ParsedCommand parse(String input) {
        CommandType type = CommandType.parseInput(input);
        String arguments = extractArguments(input, type);
        return new ParsedCommand(type, arguments);
    }

    /**
     * Parses one input line into an executable command.
     *
     * <p>This is the boundary between command syntax and command behavior:
     * {@link Plana} only needs to execute the returned command.</p>
     *
     * @param input the complete command entered by the user.
     * @return the command represented by the input
     * @throws PlanaException if the command contains invalid structured data
     */
    public Command parseCommand(String input) throws PlanaException {
        ParsedCommand parsedCommand = parse(input);
        return switch (parsedCommand.type()) {
            case BYE -> new ExitCommand();
            case HELP -> new HelpCommand();
            case LIST -> new ListCommand();
            case ON -> parseOnCommand(parsedCommand.arguments());
            case FIND -> parseFindCommand(parsedCommand.arguments());
            case DELETE -> new DeleteCommand(parsedCommand.arguments());
            case MARK -> new MarkCommand(parsedCommand.arguments());
            case UNMARK -> new UnmarkCommand(parsedCommand.arguments());
            case CLIENT -> parseClientCommand(parsedCommand.arguments());
            case DEADLINE, EVENT, TODO -> parseAddCommand(parsedCommand);
            case UNKNOWN -> parseInvalidCommand(input);
        };
    }

    /**
     * Parses the syntax and dates for a task-creation command.
     *
     * @param command the command and arguments returned by {@link #parse(String)}.
     * @return the validated task description and parsed date values
     * @throws PlanaException if the command syntax or any date is invalid
     */
    public TaskArguments parseTaskArguments(ParsedCommand command) throws PlanaException {
        return switch (command.type()) {
            case TODO -> parseTodo(command.arguments());
            case DEADLINE -> parseDeadline(command.arguments());
            case EVENT -> parseEvent(command.arguments());
            default -> throw new IllegalArgumentException("Task arguments requested for a non-task command");
        };
    }

    /**
     * Parses a date used by a task command or the {@code on} query.
     *
     * @param dateText the date entered by the user.
     * @param commandText the command using the date.
     * @return the parsed date
     * @throws PlanaException if the date is not in {@code yyyy-MM-dd} format
     */
    public LocalDate parseDate(String dateText, String commandText) throws PlanaException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            if (commandText.equals("deadline")) {
                throw deadlineError("that deadline date isn't valid. " + DATE_FORMAT_HINT);
            }
            if (commandText.equals("event")) {
                throw eventError("that event date isn't valid. " + DATE_FORMAT_HINT);
            }
            throw new PlanaException("Oops, that query date isn't valid. " + DATE_FORMAT_HINT
                    + " Try: on <date>.");
        }
    }

    /**
     * Parses the date argument of an {@code on} command.
     *
     * @param dateText the date text supplied with the command.
     * @return the date-filtering command
     * @throws PlanaException if the date is missing or invalid
     */
    private Command parseOnCommand(String dateText) throws PlanaException {
        if (dateText.isBlank()) {
            throw new PlanaException("Oops, on needs a date. Try: on <date>.");
        }
        return new OnCommand(parseDate(dateText, "on"));
    }

    /**
     * Parses the keyword argument of a {@code find} command.
     *
     * @param keyword the keyword supplied with the command.
     * @return the command that searches for the keyword
     * @throws PlanaException if the keyword is missing
     */
    private Command parseFindCommand(String keyword) throws PlanaException {
        if (keyword.isBlank()) {
            throw new PlanaException("Oops, find needs a keyword. Try: find <keyword>.");
        }
        return new FindCommand(keyword);
    }

    /**
     * Parses a client command and its subcommand arguments.
     *
     * @param arguments the text after the {@code client} keyword.
     * @return the validated client command.
     * @throws PlanaException if the client command is malformed.
     */
    private Command parseClientCommand(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw new PlanaException("Oops, client needs an action. " + CLIENT_ADD_USAGE);
        }
        String[] actionAndArguments = arguments.split("\\s+", 2);
        String actionText = actionAndArguments[0];
        String actionArguments = actionAndArguments.length == 2 ? actionAndArguments[1].trim() : "";
        return switch (actionText) {
            case "add" -> parseClientAdd(actionArguments);
            case "list" -> parseClientList(actionArguments);
            case "view" -> new ClientCommand(ClientAction.VIEW,
                    parseClientReference(actionArguments, "view"));
            case "find" -> parseClientFind(actionArguments);
            case "edit" -> parseClientEdit(actionArguments);
            case "delete" -> new ClientCommand(ClientAction.DELETE,
                    parseClientReference(actionArguments, "delete"));
            default -> throw new PlanaException("Oops, I don't recognize client action '" + actionText + "'."
                    + " " + CLIENT_ADD_USAGE);
        };
    }

    private Command parseClientAdd(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw new PlanaException("Oops, a client needs a name and an email. " + CLIENT_ADD_USAGE);
        }
        ClientFields fields = parseClientFields(arguments, true);
        if (fields.name().isBlank()) {
            throw new PlanaException("Oops, that client is missing its name. " + CLIENT_ADD_USAGE);
        }
        if (!fields.values().containsKey(ClientCommand.Field.EMAIL)
                || fields.values().get(ClientCommand.Field.EMAIL).isBlank()) {
            throw new PlanaException("Oops, that client is missing its email. " + CLIENT_ADD_USAGE);
        }
        validateClientFields(fields.name(), fields.values(), false);
        return new ClientCommand(new Client(fields.name(), fields.values().get(ClientCommand.Field.EMAIL),
                fields.values().getOrDefault(ClientCommand.Field.PHONE, ""),
                fields.values().getOrDefault(ClientCommand.Field.ADDRESS, ""),
                fields.values().getOrDefault(ClientCommand.Field.PREFERENCES, ""),
                fields.values().getOrDefault(ClientCommand.Field.NOTES, "")));
    }

    private Command parseClientList(String arguments) throws PlanaException {
        if (!arguments.isBlank()) {
            throw new PlanaException("Oops, client list doesn't take any arguments. Try: client list.");
        }
        return new ClientCommand(ClientAction.LIST);
    }

    private Command parseClientFind(String keyword) throws PlanaException {
        if (keyword.isBlank()) {
            throw new PlanaException("Oops, client find needs a keyword. Try: client find <keyword>.");
        }
        return new ClientCommand(ClientAction.FIND, keyword, true);
    }

    private Command parseClientEdit(String arguments) throws PlanaException {
        String[] referenceAndFields = arguments.split("\\s+", 2);
        if (arguments.isBlank()) {
            throw new PlanaException("Oops, client edit needs a client position. Try: client edit C1 /phone <phone>.");
        }
        String reference = parseClientReference(referenceAndFields[0], "edit");
        if (referenceAndFields.length == 1 || referenceAndFields[1].isBlank()) {
            throw new PlanaException("Oops, client edit needs at least one field to change."
                    + " Try: client edit <position> /phone <phone>.");
        }
        ClientFields fields = parseClientFields(referenceAndFields[1].trim(), false);
        if (fields.values().isEmpty()) {
            throw new PlanaException("Oops, client edit needs at least one field to change."
                    + " Try: client edit <position> /phone <phone>.");
        }
        validateClientFields(fields.name(), fields.values(), true);
        return new ClientCommand(reference, fields.values());
    }

    private String parseClientReference(String reference, String action) throws PlanaException {
        if (reference.isBlank()) {
            throw new PlanaException("Oops, client " + action + " needs a client position."
                    + " Try: client " + action + " C1.");
        }
        if (!reference.matches("C[1-9][0-9]*")) {
            throw new PlanaException("Oops, '" + reference + "' isn't a valid client position."
                    + " Use a reference like C1.");
        }
        return reference;
    }

    private ClientFields parseClientFields(String arguments, boolean addCommand) throws PlanaException {
        Set<String> knownFieldNames = Set.of("name", "email", "phone", "address", "preferences", "notes");
        Matcher unknownMarkerMatcher = CLIENT_MARKER_PATTERN.matcher(arguments);
        while (unknownMarkerMatcher.find()) {
            if (!knownFieldNames.contains(unknownMarkerMatcher.group(1))) {
                throw new PlanaException("Oops, I don't recognize the client field /"
                        + unknownMarkerMatcher.group(1) + ". " + CLIENT_FIELD_USAGE);
            }
        }

        List<ClientMarker> markers = new ArrayList<>();
        Matcher fieldMatcher = CLIENT_FIELD_PATTERN.matcher(arguments);
        while (fieldMatcher.find()) {
            markers.add(new ClientMarker(fieldMatcher.group(1), fieldMatcher.start(), fieldMatcher.end()));
        }
        EnumMap<ClientCommand.Field, String> values = new EnumMap<>(ClientCommand.Field.class);
        String name = markers.isEmpty() ? arguments.trim()
                : arguments.substring(0, markers.get(0).start()).trim();
        for (int i = 0; i < markers.size(); i++) {
            ClientMarker marker = markers.get(i);
            String fieldName = marker.name();
            ClientCommand.Field field = ClientCommand.Field.valueOf(
                    fieldName.toUpperCase(Locale.ROOT));
            if (values.containsKey(field)) {
                throw new PlanaException("Oops, the client field /" + fieldName
                        + " was provided more than once.");
            }
            int valueStart = marker.end();
            int valueEnd = i + 1 < markers.size() ? markers.get(i + 1).start() : arguments.length();
            String value = arguments.substring(valueStart, valueEnd).trim();
            values.put(field, unquoteEmptyValue(value));
        }
        if (addCommand && values.containsKey(ClientCommand.Field.NAME)) {
            throw new PlanaException("Oops, put the client name before the field markers. " + CLIENT_ADD_USAGE);
        }
        if (!addCommand && !name.isBlank()) {
            throw new PlanaException("Oops, client edit fields must use markers such as /phone or /notes.");
        }
        return new ClientFields(name, values);
    }

    private String unquoteEmptyValue(String value) {
        return value.equals("\"\"") ? "" : value;
    }

    private void validateClientFields(String name, EnumMap<ClientCommand.Field, String> values,
                                      boolean editCommand) throws PlanaException {
        if (!name.isBlank() && (name.length() > 100 || containsControlCharacter(name))) {
            throw new PlanaException("Oops, that client name isn't valid. Use 100 characters or fewer.");
        }
        if (values.containsKey(ClientCommand.Field.NAME)) {
            String updatedName = values.get(ClientCommand.Field.NAME);
            if (updatedName.isBlank()) {
                throw new PlanaException("Oops, a client's name can't be empty.");
            }
            if (updatedName.length() > 100 || containsControlCharacter(updatedName)) {
                throw new PlanaException("Oops, that client name isn't valid. Use 100 characters or fewer.");
            }
        }
        if (values.containsKey(ClientCommand.Field.EMAIL)) {
            String email = values.get(ClientCommand.Field.EMAIL).toLowerCase(Locale.ROOT);
            if (email.length() > 254 || !email.matches(CLIENT_EMAIL_PATTERN)) {
                throw new PlanaException("Oops, that email isn't valid. Use an address like alice@example.com.");
            }
            values.put(ClientCommand.Field.EMAIL, email);
        }
        validateOptionalField(values, ClientCommand.Field.PHONE, 30, editCommand);
        validateOptionalField(values, ClientCommand.Field.ADDRESS, 200, editCommand);
        validateOptionalField(values, ClientCommand.Field.PREFERENCES, 300, editCommand);
        validateOptionalField(values, ClientCommand.Field.NOTES, 500, editCommand);
    }

    private void validateOptionalField(EnumMap<ClientCommand.Field, String> values, ClientCommand.Field field,
                                       int maximumLength, boolean editCommand) throws PlanaException {
        if (!values.containsKey(field)) {
            return;
        }
        String value = values.get(field);
        if (value.isBlank() && !editCommand) {
            throw new PlanaException("Oops, /" + field.name().toLowerCase()
                    + " needs a value or should be left out.");
        }
        if (value.length() > maximumLength || containsControlCharacter(value)) {
            throw new PlanaException("Oops, /" + field.name().toLowerCase()
                    + " is too long or contains invalid characters.");
        }
        if (field == ClientCommand.Field.PHONE && !isValidPhone(value)) {
            throw new PlanaException("Oops, that phone number isn't valid. Use 7 to 15 digits.");
        }
    }

    private boolean isValidPhone(String phone) {
        if (phone.isBlank()) {
            return true;
        }
        if (!phone.matches("[0-9+(). -]+")) {
            return false;
        }
        long digitCount = phone.chars().filter(Character::isDigit).count();
        return digitCount >= 7 && digitCount <= 15;
    }

    private boolean containsControlCharacter(String value) {
        return value.chars().anyMatch(character -> Character.isISOControl((char) character));
    }

    /**
     * Converts a parsed task-creation command into a concrete add command.
     *
     * @param command the parsed task-creation command.
     * @return the command that creates the requested task
     * @throws PlanaException if the task arguments are invalid
     */
    private Command parseAddCommand(ParsedCommand command) throws PlanaException {
        TaskArguments taskArguments = parseTaskArguments(command);
        // Each task parser validates its description, and date-bearing task
        // parsers populate exactly the date fields required by that task type.
        assert taskArguments.description() != null && !taskArguments.description().isBlank()
                : "Validated task arguments must contain a description.";
        return switch (command.type()) {
            case TODO -> {
                assert taskArguments.firstDate() == null && taskArguments.secondDate() == null
                        : "ToDo arguments must not contain dates.";
                yield new AddCommand(new ToDo(taskArguments.description()));
            }
            case DEADLINE -> {
                assert taskArguments.firstDate() != null && taskArguments.secondDate() == null
                        : "Deadline arguments must contain only a due date.";
                yield new AddCommand(new Deadline(taskArguments.description(), taskArguments.firstDate()));
            }
            case EVENT -> {
                assert taskArguments.firstDate() != null && taskArguments.secondDate() != null
                        : "Event arguments must contain both boundary dates.";
                yield new AddCommand(new Event(taskArguments.description(), taskArguments.firstDate(),
                        taskArguments.secondDate()));
            }
            default -> throw new IllegalArgumentException("Task arguments requested for a non-task command");
        };
    }

    /**
     * Creates a command that reports why an unrecognized input cannot run.
     *
     * @param input the raw input entered by the user.
     * @return an invalid command containing the appropriate error message
     */
    private Command parseInvalidCommand(String input) {
        if (input.isBlank()) {
            return new InvalidCommand(EMPTY_COMMAND_ERROR);
        }
        return new InvalidCommand("Oops, I don't recognize '" + input + "'."
                + " Type help to see the commands I know.");
    }

    /**
     * Validates the description of a ToDo command.
     *
     * @param arguments the text after the {@code todo} keyword.
     * @return the validated ToDo arguments
     * @throws PlanaException if the description is empty
     */
    private TaskArguments parseTodo(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw new PlanaException(TODO_DESCRIPTION_ERROR);
        }
        return new TaskArguments(arguments, null, null);
    }

    /**
     * Validates and parses the description and due date of a deadline command.
     *
     * @param arguments the text after the {@code deadline} keyword.
     * @return the validated deadline arguments
     * @throws PlanaException if the description, marker, or due date is missing
     *         or invalid
     */
    private TaskArguments parseDeadline(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw deadlineError("a deadline needs a description and a due date.");
        }
        int bySeparatorIndex = arguments.indexOf("/by");
        if (bySeparatorIndex < 0) {
            throw deadlineError("I couldn't find /by and a due date is missing.");
        }
        String description = arguments.substring(0, bySeparatorIndex).trim();
        String dueDate = arguments.substring(bySeparatorIndex + "/by".length()).trim();
        if (description.isBlank()) {
            throw deadlineError("that deadline is missing its description.");
        }
        if (dueDate.isBlank()) {
            throw deadlineError("that deadline is missing its due date.");
        }
        return new TaskArguments(description, parseDate(dueDate, "deadline"), null);
    }

    /**
     * Validates and parses the description, start date, and end date of an event.
     *
     * @param arguments the text after the {@code event} keyword.
     * @return the validated event arguments
     * @throws PlanaException if an event component is missing or invalid
     */
    private TaskArguments parseEvent(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw eventError("an event needs a description, a start, and an end.");
        }
        int fromSeparatorIndex = arguments.indexOf("/from");
        int toSeparatorIndex = arguments.indexOf("/to");
        if (fromSeparatorIndex < 0) {
            throw eventError("that event is missing its start marker /from.");
        }
        if (toSeparatorIndex < 0) {
            throw eventError("that event is missing its end marker /to.");
        }
        if (toSeparatorIndex < fromSeparatorIndex) {
            throw eventError("use /from before /to in an event.");
        }
        String description = arguments.substring(0, fromSeparatorIndex).trim();
        String from = arguments.substring(fromSeparatorIndex + "/from".length(), toSeparatorIndex).trim();
        String to = arguments.substring(toSeparatorIndex + "/to".length()).trim();
        if (description.isBlank()) {
            throw eventError("that event is missing its description.");
        }
        if (from.isBlank()) {
            throw eventError("that event is missing its start time.");
        }
        if (to.isBlank()) {
            throw eventError("that event is missing its end time.");
        }
        return new TaskArguments(description, parseDate(from, "event"), parseDate(to, "event"));
    }

    /**
     * Creates a consistently formatted deadline parsing error.
     *
     * @param problem the specific problem found in the command.
     * @return an exception containing the problem and deadline usage guidance
     */
    private static PlanaException deadlineError(String problem) {
        return new PlanaException("Oops, " + problem + " " + DEADLINE_USAGE);
    }

    /**
     * Creates a consistently formatted event parsing error.
     *
     * @param problem the specific problem found in the command.
     * @return an exception containing the problem and event usage guidance
     */
    private static PlanaException eventError(String problem) {
        return new PlanaException("Oops, " + problem + " " + EVENT_USAGE);
    }

    /**
     * Extracts the portion of input after a command keyword.
     *
     * @param input the complete command entered by the user.
     * @param type the command recognized from the input.
     * @return the command arguments, or an empty string for commands without arguments
     */
    private String extractArguments(String input, CommandType type) {
        if (type == CommandType.UNKNOWN || type == CommandType.HELP || type == CommandType.BYE) {
            return "";
        }
        return input.substring(type.getCommandText().length()).trim();
    }

    /**
     * Holds the result of parsing one user command.
     *
     * @param type the command recognized from the input.
     * @param arguments the trimmed text after the command keyword.
     */
    public record ParsedCommand(CommandType type, String arguments) {
    }

    /**
     * Holds the validated values needed to construct a task.
     *
     * @param description the task description.
     * @param firstDate the due date, or event start date.
     * @param secondDate the event end date, or {@code null} for other task types.
     */
    public record TaskArguments(String description, LocalDate firstDate, LocalDate secondDate) {
    }

    /**
     * Holds parsed client field values before a client command is constructed.
     *
     * @param name the positional client name for add, or an empty string for edit.
     * @param values the supplied client fields.
     */
    private record ClientFields(String name, EnumMap<ClientCommand.Field, String> values) {
    }

    /**
     * Records the location of a client field marker in the original arguments.
     *
     * @param name the marker name without its slash.
     * @param start the marker's start index.
     * @param end the marker's end index.
     */
    private record ClientMarker(String name, int start, int end) {
    }
}
