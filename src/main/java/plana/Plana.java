package plana;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import plana.command.Command;
import plana.command.CommandType;
import plana.exception.PlanaException;
import plana.parser.Parser;
import plana.storage.Storage;
import plana.task.TaskList;
import plana.ui.Ui;

/**
 * Runs Plana's command-line task manager and responds in Plana's friendly voice.
 */
public class Plana {
    private static final String BANNER = " ____  _                  \n"
            + "|  _ \\| | __ _ _ __   __ _ \n"
            + "| |_) | |/ _` | '_ \\ / _` |\n"
            + "|  __/| | (_| | | | | (_| |\n"
            + "|_|   |_|\\__,_|_| |_|\\__,_|\n";
    private static final String BANNER_ART = """
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
    private static final String WELCOME_GREETING = "Hi hi! I'm Plana.\n"
            + "What shall we get done today?";
    private final Parser parser;
    private final Storage storage;
    private final TaskList tasks;

    /**
     * Creates Plana with its default persistent task storage.
     */
    public Plana() {
        this(new Storage());
    }

    /**
     * Creates Plana with the supplied persistent task storage.
     *
     * @param storage storage used to load and save Plana's tasks.
     */
    public Plana(Storage storage) {
        this.parser = new Parser();
        this.storage = storage;
        this.tasks = storage.loadTasks();
    }

    /**
     * Returns the startup banner used by Plana's console and GUI interfaces.
     *
     * @return Plana's ASCII banner and decorative art.
     */
    public static String getWelcomeBanner() {
        return BANNER + BANNER_ART;
    }

    /**
     * Returns Plana's greeting shown after the startup banner.
     *
     * @return Plana's startup greeting.
     */
    public static String getWelcomeGreeting() {
        return WELCOME_GREETING;
    }

    /**
     * Starts Plana and processes commands until the user exits or input ends.
     *
     * @param args command-line arguments, currently unused.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome(BANNER, BANNER_ART);
        Parser parser = new Parser();
        Storage storage = new Storage();

        try (ui) {
            TaskList tasks = storage.loadTasks();

            while (ui.hasNextCommand()) {
                String command = ui.readCommand();

                try {
                    Command parsedCommand = parser.parseCommand(command);
                    parsedCommand.execute(tasks, ui, storage);
                    if (parsedCommand.isExit()) {
                        break;
                    }
                } catch (PlanaException exception) {
                    ui.showError(exception.getMessage());
                }
            }
        }
    }

    /**
     * Executes one command using the supplied user interface.
     *
     * @param input the complete command entered by the user.
     * @param ui interface used to display the command response.
     * @return metadata about the executed command.
     */
    public Response executeCommand(String input, Ui ui) {
        String safeInput = input == null ? "" : input;
        CommandType commandType = parser.parse(safeInput).type();
        try {
            Command command = parser.parseCommand(safeInput);
            command.execute(tasks, ui, storage);
            return new Response("", commandType, command.isExit(), false);
        } catch (PlanaException exception) {
            ui.showError(exception.getMessage());
            return new Response("", commandType, false, true);
        }
    }

    /**
     * Executes one command and returns the response text for the JavaFX interface.
     *
     * @param input the complete command entered by the user.
     * @return the response text and command metadata.
     */
    public Response getResponse(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            Ui responseUi = new Ui(printStream);
            Response commandResponse = executeCommand(input, responseUi);
            printStream.flush();
            String responseText = output.toString(StandardCharsets.UTF_8).stripTrailing();
            return new Response(responseText, commandResponse.commandType(), commandResponse.exit(),
                    commandResponse.error());
        }
    }

    /**
     * Holds the user-facing response and metadata for one executed command.
     *
     * @param text response text suitable for displaying in a chat bubble.
     * @param commandType type of the command that produced the response.
     * @param exit whether the command requests that the interface exits.
     * @param error whether Plana could not execute the command.
     */
    public record Response(String text, CommandType commandType, boolean exit, boolean error) {
    }
}
