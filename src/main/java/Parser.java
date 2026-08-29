import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Converts raw console input into the command type and arguments used by
 * Plana's command loop.
 */
public class Parser {
    private static final String EMPTY_COMMAND_ERROR = "Oops, I didn't catch a command."
            + " Type help to see what I can do.";
    private static final String TODO_DESCRIPTION_ERROR = "Oops, a ToDo description can't be empty."
            + " Try: todo <description>.";
    private static final String DEADLINE_USAGE = "Try: deadline <description> /by <date>.";
    private static final String EVENT_USAGE = "Try: event <description> /from <start> /to <end>.";
    private static final String DATE_FORMAT_HINT = "Use the date format yyyy-MM-dd, like 2019-10-15.";

    /**
     * Parses one complete line entered by the user.
     *
     * @param input the complete command entered by the user
     * @return the recognized command and its trimmed arguments
     */
    public ParsedCommand parse(String input) {
        CommandType type = CommandType.fromInput(input);
        String arguments = extractArguments(input, type);
        return new ParsedCommand(type, arguments);
    }

    /**
     * Parses one input line into an executable command.
     *
     * <p>This is the boundary between command syntax and command behavior:
     * {@link Plana} only needs to execute the returned command.</p>
     *
     * @param input the complete command entered by the user
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
        case DELETE -> new DeleteCommand(parsedCommand.arguments());
        case MARK -> new MarkCommand(parsedCommand.arguments());
        case UNMARK -> new UnmarkCommand(parsedCommand.arguments());
        case DEADLINE, EVENT, TODO -> parseAddCommand(parsedCommand);
        case UNKNOWN -> parseInvalidCommand(input);
        };
    }

    /**
     * Parses the syntax and dates for a task-creation command.
     *
     * @param command the command and arguments returned by {@link #parse(String)}
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
     * @param dateText the date entered by the user
     * @param commandText the command using the date
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

    private Command parseOnCommand(String dateText) throws PlanaException {
        if (dateText.isBlank()) {
            throw new PlanaException("Oops, on needs a date. Try: on <date>.");
        }
        return new OnCommand(parseDate(dateText, "on"));
    }

    private Command parseAddCommand(ParsedCommand command) throws PlanaException {
        TaskArguments taskArguments = parseTaskArguments(command);
        return switch (command.type()) {
        case TODO -> new AddCommand(new ToDo(taskArguments.description()));
        case DEADLINE -> new AddCommand(new Deadline(taskArguments.description(), taskArguments.firstDate()));
        case EVENT -> new AddCommand(new Event(taskArguments.description(), taskArguments.firstDate(),
                taskArguments.secondDate()));
        default -> throw new IllegalArgumentException("Task arguments requested for a non-task command");
        };
    }

    private Command parseInvalidCommand(String input) {
        if (input.isBlank()) {
            return new InvalidCommand(EMPTY_COMMAND_ERROR);
        }
        return new InvalidCommand("Oops, I don't recognize '" + input + "'."
                + " Type help to see the commands I know.");
    }

    private TaskArguments parseTodo(String arguments) throws PlanaException {
        if (arguments.isBlank()) {
            throw new PlanaException(TODO_DESCRIPTION_ERROR);
        }
        return new TaskArguments(arguments, null, null);
    }

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

    private static PlanaException deadlineError(String problem) {
        return new PlanaException("Oops, " + problem + " " + DEADLINE_USAGE);
    }

    private static PlanaException eventError(String problem) {
        return new PlanaException("Oops, " + problem + " " + EVENT_USAGE);
    }

    /**
     * Extracts the portion of input after a command keyword.
     *
     * @param input the complete command entered by the user
     * @param type the command recognized from the input
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
     * @param type the command recognized from the input
     * @param arguments the trimmed text after the command keyword
     */
    public record ParsedCommand(CommandType type, String arguments) {
    }

    /**
     * Holds the validated values needed to construct a task.
     *
     * @param description the task description
     * @param firstDate the due date, or event start date
     * @param secondDate the event end date, or {@code null} for other task types
     */
    public record TaskArguments(String description, LocalDate firstDate, LocalDate secondDate) {
    }
}
