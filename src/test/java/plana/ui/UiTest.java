package plana.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import plana.task.Deadline;
import plana.task.TaskList;
import plana.task.ToDo;

/**
 * Tests console input handling and user-facing output formatting.
 */
class UiTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    void redirectConsole() {
        originalOut = System.out;
        originalIn = System.in;
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void restoreConsole() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    void showWelcome_bannerAndGreetingDisplayed() {
        Ui ui = newUi("");

        ui.showWelcome("BANNER\n", "ART\n");

        assertTrue(outputText().contains("BANNER\nART\n"));
        assertTrue(outputText().contains("Hi hi! I'm Plana."));
        assertTrue(outputText().contains("What shall we get done today?"));
        ui.close();
    }

    @Test
    void hasNextCommand_inputAvailabilityCorrectlyReported() {
        Ui ui = newUi("next command\n");

        assertTrue(ui.hasNextCommand());
        assertEquals("next command", ui.readCommand());
        assertFalse(ui.hasNextCommand());
        ui.close();
    }

    @Test
    void readCommand_commandEchoedAndSeparatorDisplayed() {
        Ui ui = newUi("list\n");

        assertEquals("list", ui.readCommand());

        assertTrue(outputText().contains("list"));
        assertTrue(outputText().contains("____________________________________________________________"));
        ui.close();
    }

    @Test
    void showHelp_allSupportedCommandsDisplayed() {
        Ui ui = newUi("");

        ui.showHelp();

        String response = outputText();
        assertTrue(response.contains("todo <description>"));
        assertTrue(response.contains("deadline <description> /by <date>"));
        assertTrue(response.contains("event <description> /from <start> /to <end>"));
        assertTrue(response.contains("on <date>"));
        assertTrue(response.contains("find <keyword>"));
        assertTrue(response.contains("delete <number>"));
        assertTrue(response.contains("mark <number>"));
        assertTrue(response.contains("unmark <number>"));
        assertTrue(response.contains("bye"));
        String firstHelpItem = "  todo <description>" + System.lineSeparator()
                + "    add a task";
        assertTrue(response.contains(firstHelpItem));
        assertFalse(response.contains("                          "));
        ui.close();
    }

    @Test
    void matchingTaskMessages_displayExpectedText() {
        Ui ui = newUi("");

        ui.showMatchingTasksHeader();
        ui.showNoMatchingTasks();

        String response = outputText();
        assertTrue(response.contains("Here are the matching tasks in your list:"));
        assertTrue(response.contains("No matching tasks found."));
        ui.close();
    }

    @Test
    void showTaskList_emptyAndNonEmptyLists_displayExpectedRows() {
        Ui ui = newUi("");
        TaskList emptyTasks = new TaskList();
        TaskList tasks = new TaskList(List.of(new ToDo("first"), new ToDo("second")));

        ui.showTaskList(emptyTasks);
        ui.showTaskList(tasks);

        String response = outputText();
        assertTrue(response.contains("Here are your tasks:"));
        assertTrue(response.contains("1.[T][ ] first"));
        assertTrue(response.contains("2.[T][ ] second"));
        ui.close();
    }

    @Test
    void showTask_andDateMessages_displayExpectedText() {
        Ui ui = newUi("");
        Deadline deadline = new Deadline("submit report", LocalDate.of(2026, 8, 31));

        ui.showTask(2, deadline);
        ui.showTasksOnDateHeader("Aug 31 2026");
        ui.showNoTasksOnDate("Aug 31 2026");

        String response = outputText();
        assertTrue(response.contains("3.[D][ ] submit report"));
        assertTrue(response.contains("Here are the deadlines and events on Aug 31 2026:"));
        assertTrue(response.contains("No deadlines or events found on Aug 31 2026."));
        ui.close();
    }

    @Test
    void taskAddedAndDeletedMessages_singularAndPluralCounts_areFormattedCorrectly() {
        Ui ui = newUi("");
        ToDo task = new ToDo("buy milk");

        ui.showTaskAdded(task, 1);
        ui.showTaskAdded(task, 2);
        ui.showTaskDeleted(task, 1);
        ui.showTaskDeleted(task, 0);

        String response = outputText();
        assertTrue(response.contains("Now you have 1 task in your list!"));
        assertTrue(response.contains("Now you have 2 tasks in your list!"));
        assertTrue(response.contains("Now you have 1 task in the list."));
        assertTrue(response.contains("Now you have 0 tasks in the list."));
        ui.close();
    }

    @Test
    void statusGoodbyeErrorAndLineMessages_expectedResponsesDisplayed() {
        Ui ui = newUi("");
        ToDo task = new ToDo("finish tests");

        ui.showTaskMarkedDone(task);
        ui.showTaskMarkedNotDone(task);
        ui.showGoodbye();
        ui.showError("Oops, something went wrong.");
        ui.showLine();

        String response = outputText();
        assertTrue(response.contains("marked this task as done"));
        assertTrue(response.contains("marked this task as not done"));
        assertTrue(response.contains("Bye-bye! See you next time, okay?"));
        assertTrue(response.contains("Oops, something went wrong."));
        assertTrue(response.contains("____________________________________________________________"));
        ui.close();
    }

    private Ui newUi(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return new Ui();
    }

    private String outputText() {
        return output.toString();
    }
}
