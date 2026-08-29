import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Plana's command-line task manager and responds in Plana's friendly voice.
 */
public class Plana {
    private static final String EMPTY_COMMAND_ERROR = "Oops, I didn't catch a command."
            + " Type help to see what I can do.";
    private static final String ON_USAGE = "Try: on <date>.";
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
        Parser parser = new Parser();

        try (ui) {
            TaskList tasks = Storage.loadTasks();

            commandLoop:
            while (ui.hasNextCommand()) {
                String command = ui.readCommand();

                try {
                    Parser.ParsedCommand parsedCommand = parser.parse(command);
                    CommandType commandType = parsedCommand.type();
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
                        ui.showTaskList(tasks);
                    }
                    case ON -> {
                        String dateText = parsedCommand.arguments();
                        if (dateText.isBlank()) {
                            throw new PlanaException("Oops, on needs a date. " + ON_USAGE);
                        }
                        LocalDate date = parser.parseDate(dateText, "on");
                        String displayDate = date.format(DISPLAY_DATE_FORMAT);
                        boolean hasMatchingTask = false;
                        ui.showTasksOnDateHeader(displayDate);
                        for (int i = 0; i < tasks.size(); i++) {
                            Task task = tasks.get(i);
                            boolean matches = (task instanceof Deadline deadline && deadline.isOn(date))
                                    || (task instanceof Event event && event.occursOn(date));
                            if (matches) {
                                ui.showTask(i, task);
                                hasMatchingTask = true;
                            }
                        }
                        if (!hasMatchingTask) {
                            ui.showNoTasksOnDate(displayDate);
                        }
                        ui.showLine();
                    }
                    case DELETE -> {
                        String taskNumber = parsedCommand.arguments();
                        int taskIndex = getTaskIndex(TaskAction.DELETE, taskNumber, tasks.size());
                        Task deletedTask = tasks.remove(taskIndex);
                        Storage.saveTasks(tasks);
                        ui.showTaskDeleted(deletedTask, tasks.size());
                    }
                    case MARK -> {
                        String taskNumber = parsedCommand.arguments();
                        int taskIndex = getTaskIndex(TaskAction.MARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        Storage.saveTasks(tasks);
                        ui.showTaskMarkedDone(tasks.get(taskIndex));
                    }
                    case UNMARK -> {
                        String taskNumber = parsedCommand.arguments();
                        int taskIndex = getTaskIndex(TaskAction.UNMARK, taskNumber, tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        Storage.saveTasks(tasks);
                        ui.showTaskMarkedNotDone(tasks.get(taskIndex));
                    }
                    case DEADLINE -> {
                        Parser.TaskArguments taskArguments = parser.parseTaskArguments(parsedCommand);
                        tasks.add(new Deadline(taskArguments.description(), taskArguments.firstDate()));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        ui.showTaskAdded(tasks.get(taskCount - 1), taskCount);
                    }
                    case EVENT -> {
                        Parser.TaskArguments taskArguments = parser.parseTaskArguments(parsedCommand);
                        tasks.add(new Event(taskArguments.description(), taskArguments.firstDate(),
                                taskArguments.secondDate()));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        ui.showTaskAdded(tasks.get(taskCount - 1), taskCount);
                    }
                    case TODO -> {
                        Parser.TaskArguments taskArguments = parser.parseTaskArguments(parsedCommand);
                        tasks.add(new ToDo(taskArguments.description()));
                        Storage.saveTasks(tasks);
                        int taskCount = tasks.size();
                        ui.showTaskAdded(tasks.get(taskCount - 1), taskCount);
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
