package plana.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import plana.command.AddCommand;
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

/**
 * Tests command classification and structured input validation.
 */
public class ParserTest {
    private final Parser parser = new Parser();

    /**
     * Verifies that raw input is classified correctly and that arguments are
     * extracted only for commands that accept them.
     */
    @Test
    public void parse_knownAndUnknownInputs_correctTypeAndArgumentsReturned() {
        assertEquals(new Parser.ParsedCommand(CommandType.TODO, "buy milk"),
                parser.parse("todo buy milk"));
        assertEquals(new Parser.ParsedCommand(CommandType.DELETE, "2"),
                parser.parse("delete 2"));
        assertEquals(new Parser.ParsedCommand(CommandType.HELP, ""),
                parser.parse("please help me"));
        assertEquals(new Parser.ParsedCommand(CommandType.UNKNOWN, ""),
                parser.parse("not a command"));
    }

    /**
     * Verifies that each command category is dispatched to the appropriate
     * executable command class.
     */
    @Test
    public void parseCommand_allCommandCategories_expectedCommandReturned() throws PlanaException {
        assertInstanceOf(ExitCommand.class, parser.parseCommand("bye"));
        assertTrue(parser.parseCommand("bye").isExit());
        assertInstanceOf(HelpCommand.class, parser.parseCommand("?"));
        assertInstanceOf(ListCommand.class, parser.parseCommand("list"));
        assertInstanceOf(OnCommand.class, parser.parseCommand("on 2026-08-31"));
        assertInstanceOf(FindCommand.class, parser.parseCommand("find book"));
        assertInstanceOf(DeleteCommand.class, parser.parseCommand("delete 1"));
        assertInstanceOf(MarkCommand.class, parser.parseCommand("mark 1"));
        assertInstanceOf(UnmarkCommand.class, parser.parseCommand("unmark 1"));
        assertInstanceOf(AddCommand.class, parser.parseCommand("todo buy milk"));
        assertInstanceOf(AddCommand.class, parser.parseCommand("deadline report /by 2026-08-31"));
        assertInstanceOf(AddCommand.class,
                parser.parseCommand("event meeting /from 2026-08-31 /to 2026-09-01"));
        assertInstanceOf(InvalidCommand.class, parser.parseCommand("not a command"));
    }

    /**
     * Verifies that valid ToDo, deadline, and event arguments are returned in
     * their typed representation.
     */
    @Test
    public void parseTaskArguments_validTaskInputs_typedArgumentsReturned() throws PlanaException {
        assertEquals(new Parser.TaskArguments("buy milk", null, null),
                parser.parseTaskArguments(parser.parse("todo buy milk")));
        assertEquals(new Parser.TaskArguments("submit report", LocalDate.of(2026, 8, 31), null),
                parser.parseTaskArguments(parser.parse("deadline submit report /by 2026-08-31")));
        assertEquals(new Parser.TaskArguments("team meeting", LocalDate.of(2026, 8, 31),
                        LocalDate.of(2026, 9, 1)),
                parser.parseTaskArguments(parser.parse(
                        "event team meeting /from 2026-08-31 /to 2026-09-01")));
    }

    /**
     * Verifies valid ISO dates and command-specific messages for invalid dates.
     */
    @Test
    public void parseDate_validAndInvalidDates_expectedResultOrMessage() throws PlanaException {
        assertEquals(LocalDate.of(2026, 8, 31), parser.parseDate("2026-08-31", "on"));

        PlanaException deadlineError = assertThrows(PlanaException.class,
                () -> parser.parseDate("31-08-2026", "deadline"));
        assertEquals("Oops, that deadline date isn't valid."
                + " Use the date format yyyy-MM-dd, like 2019-10-15."
                + " Try: deadline <description> /by <date>.", deadlineError.getMessage());

        PlanaException eventError = assertThrows(PlanaException.class,
                () -> parser.parseDate("2026-02-30", "event"));
        assertEquals("Oops, that event date isn't valid."
                + " Use the date format yyyy-MM-dd, like 2019-10-15."
                + " Try: event <description> /from <start> /to <end>.", eventError.getMessage());

        PlanaException queryError = assertThrows(PlanaException.class,
                () -> parser.parseDate("tomorrow", "on"));
        assertEquals("Oops, that query date isn't valid."
                + " Use the date format yyyy-MM-dd, like 2019-10-15. Try: on <date>.", queryError.getMessage());
    }

    /**
     * Verifies that empty or malformed ToDo and deadline inputs are rejected
     * with actionable messages.
     */
    @Test
    public void parseTaskArguments_invalidTodoAndDeadlineInputs_exceptionReturned() {
        assertParserException("todo", "Oops, a ToDo description can't be empty."
                + " Try: todo <description>.");
        assertParserException("deadline", "Oops, a deadline needs a description and a due date."
                + " Try: deadline <description> /by <date>.");
        assertParserException("deadline report", "Oops, I couldn't find /by and a due date is missing."
                + " Try: deadline <description> /by <date>.");
        assertParserException("deadline /by 2026-08-31", "Oops, that deadline is missing its description."
                + " Try: deadline <description> /by <date>.");
        assertParserException("deadline report /by", "Oops, that deadline is missing its due date."
                + " Try: deadline <description> /by <date>.");
    }

    /**
     * Verifies that a find command requires a non-blank keyword.
     */
    @Test
    public void parseCommand_findWithoutKeyword_exceptionReturned() {
        assertParserCommandException("find", "Oops, find needs a keyword. Try: find <keyword>.");
    }

    /**
     * Verifies that malformed event markers, fields, ordering, and dates are
     * rejected without producing a partially valid task.
     */
    @Test
    public void parseTaskArguments_invalidEventInputs_exceptionReturned() {
        assertParserException("event", "Oops, an event needs a description, a start, and an end."
                + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /to 2026-09-01", "Oops, that event is missing its start marker /from."
                + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /from 2026-08-31", "Oops, that event is missing its end marker /to."
                + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /to 2026-09-01 /from 2026-08-31",
                "Oops, use /from before /to in an event."
                        + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event /from 2026-08-31 /to 2026-09-01",
                "Oops, that event is missing its description."
                        + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /from /to 2026-09-01",
                "Oops, that event is missing its start time."
                        + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /from 2026-08-31 /to",
                "Oops, that event is missing its end time."
                        + " Try: event <description> /from <start> /to <end>.");
        assertParserException("event meeting /from invalid /to 2026-09-01",
                "Oops, that event date isn't valid."
                        + " Use the date format yyyy-MM-dd, like 2019-10-15."
                        + " Try: event <description> /from <start> /to <end>.");
    }

    /**
     * Verifies that task-argument parsing is only available for task commands.
     */
    @Test
    public void parseTaskArguments_nonTaskCommand_illegalArgumentExceptionThrown() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parseTaskArguments(new Parser.ParsedCommand(CommandType.LIST, "")));
    }

    private void assertParserException(String input, String expectedMessage) {
        PlanaException exception = assertThrows(PlanaException.class,
                () -> parser.parseTaskArguments(parser.parse(input)));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertParserCommandException(String input, String expectedMessage) {
        PlanaException exception = assertThrows(PlanaException.class, () -> parser.parseCommand(input));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
