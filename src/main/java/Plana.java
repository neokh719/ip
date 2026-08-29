import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Runs Plana's command-line task manager and responds in Plana's friendly voice.
 */
public class Plana {
    private static final String TODO_DESCRIPTION_ERROR = "Oops, a ToDo description can't be empty."
            + " Try: todo <description>.";
    private static final String EMPTY_COMMAND_ERROR = "Oops, I didn't catch a command."
            + " Type help to see what I can do.";
    private static final String DEADLINE_USAGE = "Try: deadline <description> /by <date>.";
    private static final String EVENT_USAGE = "Try: event <description> /from <start> /to <end>.";
    private static final String ON_USAGE = "Try: on <date>.";
    private static final String DATE_FORMAT_HINT = "Use the date format yyyy-MM-dd, like 2019-10-15.";
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /**
     * Converts a user-entered task number into a zero-based list index.
     *
     * @param action the command being performed, such as mark or unmark
     * @param taskNumber the task number entered by the user
     * @param taskCount the number of tasks currently stored
     * @return the zero-based index of the selected task
     * @throws PlanaException if the task number is not a valid existing task
     */
    private static int getTaskIndex(TaskAction action, String taskNumber, int taskCount) throws PlanaException {
        if (taskNumber.isBlank()) {
            throw new PlanaException("Oops, " + action.getCommandText() + " needs a task number."
                    + " Try: " + action.getCommandText() + " <number>.");
        }

        final int taskIndex;
        try {
            taskIndex = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException exception) {
            throw new PlanaException("Oops, '" + taskNumber + "' isn't a valid task number."
                    + " Use a positive whole number, like " + action.getCommandText() + " 1.");
        }

        if (taskIndex < 0) {
            throw new PlanaException("Oops, task numbers start at 1."
                    + " Try " + action.getCommandText() + " 1 or another number from list.");
        }
        if (taskIndex >= taskCount) {
            throw new PlanaException("Oops, task " + (taskIndex + 1) + " doesn't exist yet."
                    + " Type list to check the task numbers you have.");
        }
        return taskIndex;
    }

    /**
     * Creates an error for a deadline whose syntax is missing one specific part.
     *
     * @param problem the part of the deadline that needs correcting
     * @return a user-friendly deadline error
     */
    private static PlanaException deadlineError(String problem) {
        return new PlanaException("Oops, " + problem + " " + DEADLINE_USAGE);
    }

    /**
     * Creates an error for an event whose syntax is missing one specific part.
     *
     * @param problem the part of the event that needs correcting
     * @return a user-friendly event error
     */
    private static PlanaException eventError(String problem) {
        return new PlanaException("Oops, " + problem + " " + EVENT_USAGE);
    }

    /**
     * Parses a date entered in ISO-8601 format and converts parsing failures into
     * a friendly command error.
     *
     * @param dateText the date entered by the user
     * @param taskType the task type used in the error message
     * @return the parsed date
     * @throws PlanaException if the date is not in {@code yyyy-MM-dd} format
     */
    private static LocalDate parseDate(String dateText, String taskType) throws PlanaException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            if (taskType.equals("deadline")) {
                throw deadlineError("that deadline date isn't valid. " + DATE_FORMAT_HINT);
            }
            if (taskType.equals("event")) {
                throw eventError("that event date isn't valid. " + DATE_FORMAT_HINT);
            }
            throw new PlanaException("Oops, that query date isn't valid. " + DATE_FORMAT_HINT + " " + ON_USAGE);
        }
    }

    public static void main(String[] args) {
        String banner = " ____  _                  \n"
                + "|  _ \\| | __ _ _ __   __ _ \n"
                + "| |_) | |/ _` | '_ \\ / _` |\n"
                + "|  __/| | (_| | | | | (_| |\n"
                + "|_|   |_|\\__,_|_| |_|\\__,_|\n";
        String banner_art = """
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠿⡛⡫⢋⠍⠍⢅⢍⢑⠩⡉⢍⠫⢛⠻⡻⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⡻⢿⠑⢌⢌⣔⣴⣵⢾⣾⣾⣾⣾⣾⡾⠶⡳⠵⢬⣢⢊⠌⠝⡛⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠫⠡⡑⡰⠴⢿⣿⣿⠟⡑⣌⣂⢊⢻⣿⣿⠏⠪⣨⣦⣥⡑⡸⣿⣷⣧⣊⠔⡨⢛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⡟⡣⠊⣌⢎⠎⠪⡘⣾⣿⠣⢨⣺⣿⣿⡢⠡⣿⡇⢅⢽⣿⣿⣿⣗⠌⣺⣿⣿⡇⢕⢐⠡⡘⠿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⡿⠋⢔⣰⣽⣿⡧⡊⢌⣺⣿⡇⢅⣽⣿⣿⣿⠪⢨⣿⡪⢐⣽⣿⣿⣿⠣⢊⢼⣿⡿⡟⠆⡢⢑⣴⡡⠂⠝⢿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⠏⡢⣱⣵⣿⣿⣿⠣⡊⢴⣿⣿⢇⢂⢿⣿⢟⠏⣊⣾⣿⣧⡅⢝⠩⢋⢂⢕⣼⣿⣿⣧⠪⡐⢌⢘⠍⡢⢑⠡⡑⢜⣿⣿⣿⣿
                ⣿⣿⣿⠣⢑⣴⣿⣿⣿⣿⡏⡊⡲⣿⣿⣿⣷⣐⢌⢂⡢⣱⣾⣿⣿⣿⣿⣷⣷⣶⣷⣿⣿⣿⣿⣷⣧⣶⣗⢐⠅⠢⣱⣮⡆⢅⠚⣿⣿⣿
                ⣿⣿⢃⠪⣸⣿⣿⣿⣿⣿⡐⢌⢼⣿⣿⣿⡿⠿⢟⢓⣛⣙⣭⡮⠾⡚⣛⣍⣝⣬⣵⣭⣮⡭⣏⣻⣟⣛⠿⢦⣾⣼⣾⣿⣿⡦⡑⡘⣿⣿
                ⣿⢇⢑⢸⣿⣿⣿⣿⣿⡿⢿⠻⡛⡙⣅⣥⣪⣮⣾⡿⠻⣙⣕⣼⣾⣾⣿⣿⣿⣿⣿⡿⡫⣸⣿⣿⣿⣿⣿⣾⣬⣙⢟⢿⣿⣿⡆⠪⡸⣿
                ⡿⢐⠡⣺⣿⣿⡟⢭⡑⣌⣦⣵⣾⣿⢿⡻⣛⣫⣵⣼⢞⣻⣿⣿⣿⣿⣿⣿⣿⢿⠫⢨⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣧⣙⠿⣯⠊⠌⣿
                ⡏⡢⢑⣿⣿⣿⣿⣿⢙⣙⣍⣵⣥⣾⣶⣿⣿⡿⡛⣼⣾⣿⣿⣿⣿⡿⡿⢋⠣⠡⣊⣴⣿⣿⣿⡿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣮⣪⠣⡑⡸
                ⡇⡊⢼⣿⣿⣿⣿⣿⣤⢍⢛⠻⡛⢟⠻⣫⡧⡱⣞⠻⢛⢛⠝⡩⢃⠣⡨⣂⣵⣷⣿⣿⣿⣿⣿⡿⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡌⢔⢘
                ⡌⡂⡊⡃⣻⣿⣿⣿⣿⣇⠢⡑⡐⡡⠿⡛⣴⣿⣿⣿⣿⠢⢗⢂⣓⣃⠪⣿⣿⣿⣿⠏⣽⣿⣿⠣⣾⣿⣿⣿⣿⣿⢹⣿⣿⣿⣿⡯⡐⠌
                ⣇⢊⠔⡁⢎⢛⢙⢽⣿⣿⣿⣯⣦⡦⡑⣴⣿⣿⣿⣿⢣⢳⣱⣿⣿⣿⡎⣽⣿⣿⣿⢨⣿⡿⢣⢡⠩⡙⡻⣿⣿⣿⠸⣿⣿⣿⣿⡗⡨⢨
                ⣿⡐⠬⡎⢔⢐⠡⣙⣿⣿⡏⡙⡣⠑⢬⣾⣿⣿⣿⡿⣨⢾⠻⢛⢛⠻⢿⡆⢿⣿⡯⢪⡟⢅⣿⣷⠡⣻⣦⣝⣿⣟⢬⣿⣿⣿⣿⢇⠺⣿
                ⣿⡆⠅⢍⠢⡘⢾⣿⣿⣿⣮⡐⠔⡡⣳⣿⡿⣻⣿⠂⢕⣠⣵⣶⣦⣅⢕⣿⣧⠹⡯⣂⣾⠟⢟⠻⡳⢨⢻⣿⣿⡇⣺⣿⣿⣿⡿⡂⢝⣿
                ⣿⣮⠊⠔⡁⣢⣿⣿⣿⣿⣿⣬⠎⢄⢿⣿⡗⣺⡇⣵⠿⡿⣻⢿⣿⣿⣿⣿⣿⣿⣬⣼⣧⣼⣴⣥⣌⢂⢊⢝⢿⠌⣾⣿⣿⣿⡓⢌⣺⣿
                ⣿⣿⡎⠌⠜⣾⣿⣿⣿⣿⣿⠇⠕⡁⣿⣿⡧⢺⡇⣺⣼⣇⣟⣼⣿⡿⣿⣿⣿⣿⣿⣿⣿⣿⢫⡿⢿⣷⣕⢐⠔⣹⣿⣿⣿⡿⡈⢦⣿⣿
                ⣿⣿⣿⣎⠌⢜⢻⣿⣿⣿⠣⡑⡡⠢⢹⣿⢑⣥⡢⣹⣿⣿⣿⣿⣗⠅⣂⣪⣬⣙⡝⡻⢿⣯⣢⣟⣼⡢⣿⡇⡢⣿⣿⣿⠫⡑⢼⣿⣿⣿
                ⣿⣿⣿⣿⣮⢐⠡⠻⣿⣿⣷⡟⡢⣱⣇⠣⡸⣿⣿⣿⣿⣿⣿⣿⣇⣺⣿⣿⣿⣿⣿⣷⡩⣿⣿⣿⣿⣿⢏⢢⡿⣏⡿⠡⡑⣼⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣷⡅⠕⡩⠻⡏⡢⡢⣿⣿⡇⣚⣿⣿⣿⣿⣿⣿⣿⡧⣺⣿⣿⣿⣿⣿⣿⢪⣿⣿⣿⡿⢫⡼⡛⡱⢑⢐⣱⣾⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣷⣔⠡⢒⠫⢸⣿⣿⢇⡢⡙⢿⣿⣿⣿⣿⣿⣯⢜⢿⣿⣿⣿⣿⠏⣼⣿⣯⣣⣪⣱⢔⠌⡂⣕⣾⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣥⡊⠔⡩⢙⢂⢿⠠⠡⡙⡻⣿⣿⣿⣿⣷⣍⠻⡻⡻⣱⣽⣿⣿⠿⡫⢃⢅⢢⣪⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣬⣂⢅⠢⣡⢑⢐⠔⡨⢋⠿⠿⠿⠿⠿⡻⡚⢏⠫⡑⡡⣊⣤⣷⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                ⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⣥⣢⣡⣂⣅⣅⣣⣑⣔⣬⣶⣿⣾⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿
                """;
        Ui ui = new Ui();
        ui.showWelcome(banner, banner_art);

        try (ui) {
            TaskList tasks = Storage.loadTasks();

            commandLoop:
            while (ui.hasNextCommand()) {
                String command = ui.readCommand();

                try {
                    CommandType commandType = CommandType.fromInput(command);
                    if (commandType == CommandType.BYE) {
                        ui.showGoodbye();
                        break commandLoop;
                    }
                    switch (commandType) {
                    case HELP -> {
                        ui.showHelp();
                        ui.showLine();
                    }
                    case LIST -> {
                        System.out.println(" Here are your tasks:");
                        for (int i = 0; i < tasks.size(); i++) {
                            System.out.println(" " + (i + 1) + "." + tasks.get(i));
                        }
                        ui.showLine();
                    }
                    case ON -> {
                        String dateText = command.substring(CommandType.ON.getCommandText().length()).trim();
                        if (dateText.isBlank()) {
                            throw new PlanaException("Oops, on needs a date. " + ON_USAGE);
                        }
                        LocalDate date = parseDate(dateText, "on");
                        String displayDate = date.format(DISPLAY_DATE_FORMAT);
                        boolean hasMatchingTask = false;
                        System.out.println(" Here are the deadlines and events on " + displayDate + ":");
                        for (int i = 0; i < tasks.size(); i++) {
                            Task task = tasks.get(i);
                            boolean matches = (task instanceof Deadline deadline && deadline.isOn(date))
                                    || (task instanceof Event event && event.occursOn(date));
                            if (matches) {
                                System.out.println(" " + (i + 1) + "." + task);
                                hasMatchingTask = true;
                            }
                        }
                        if (!hasMatchingTask) {
                            System.out.println(" No deadlines or events found on " + displayDate + ".");
                        }
                        ui.showLine();
                    }
                    case DELETE -> {
                        String taskNumber = command.substring(CommandType.DELETE.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.DELETE, taskNumber, tasks.size());
                        Task deletedTask = tasks.remove(taskIndex);
                        Storage.saveTasks(tasks);
                        System.out.println("Noted. I've removed this task:");
                        System.out.println("  " + deletedTask);
                        int taskCount = tasks.size();
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in the list.");
                        ui.showLine();
                    }
                    case MARK -> {
                        String taskNumber = command.substring(CommandType.MARK.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.MARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        Storage.saveTasks(tasks);
                        System.out.println("Yay! I've marked this task as done:");
                        System.out.println("  " + tasks.get(taskIndex));
                        ui.showLine();
                    }
                    case UNMARK -> {
                        String taskNumber = command.substring(CommandType.UNMARK.getCommandText().length()).trim();
                        int taskIndex = getTaskIndex(TaskAction.UNMARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        Storage.saveTasks(tasks);
                        System.out.println("No worries! I've marked this task as not done:");
                        System.out.println("  " + tasks.get(taskIndex));
                        ui.showLine();
                    }
                    case DEADLINE -> {
                        String arguments = command.substring(CommandType.DEADLINE.getCommandText().length()).trim();
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
                        tasks.add(new Deadline(description, parseDate(dueDate, "deadline")));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        ui.showLine();
                    }
                    case EVENT -> {
                        String arguments = command.substring(CommandType.EVENT.getCommandText().length()).trim();
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
                        tasks.add(new Event(description, parseDate(from, "event"), parseDate(to, "event")));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        ui.showLine();
                    }
                    case TODO -> {
                        String description = command.substring(CommandType.TODO.getCommandText().length()).trim();
                        if (description.isBlank()) {
                            throw new PlanaException(TODO_DESCRIPTION_ERROR);
                        }
                        tasks.add(new ToDo(description));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        System.out.println("Yay, I've added this task:");
                        System.out.println("  " + tasks.get(taskCount - 1));
                        System.out.println("Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list!");
                        ui.showLine();
                    }
                    case UNKNOWN -> {
                        if (command.isBlank()) {
                            throw new PlanaException(EMPTY_COMMAND_ERROR);
                        }
                        throw new PlanaException("Oops, I don't recognize '" + command + "'."
                                + " Type help to see the commands I know.");
                    }
                    }
                } catch (PlanaException exception) {
                    ui.showError(exception.getMessage());
                }
            }
        }
    }
}
