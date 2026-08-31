package plana.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommandTypeTest {

    @Test
    void fromInput_exactAndArgumentBearingCommands_correctTypesReturned() {
        assertEquals(CommandType.BYE, CommandType.fromInput("bye"));
        assertEquals(CommandType.LIST, CommandType.fromInput("list"));
        assertEquals(CommandType.TODO, CommandType.fromInput("todo buy milk"));
        assertEquals(CommandType.DEADLINE, CommandType.fromInput("deadline report /by 31/08/2026"));
        assertEquals(CommandType.EVENT, CommandType.fromInput("event meeting /from 31/08/2026 /to 01/09/2026"));
        assertEquals(CommandType.ON, CommandType.fromInput("on 31/08/2026"));
        assertEquals(CommandType.FIND, CommandType.fromInput("find book"));
        assertEquals(CommandType.DELETE, CommandType.fromInput("delete 1"));
        assertEquals(CommandType.MARK, CommandType.fromInput("mark 1"));
        assertEquals(CommandType.UNMARK, CommandType.fromInput("unmark 1"));
    }

    @Test
    void fromInput_helpAliasesAndCaseInsensitiveHelp_helpReturned() {
        assertEquals(CommandType.HELP, CommandType.fromInput("?"));
        assertEquals(CommandType.HELP, CommandType.fromInput("please HELP me"));
    }

    @Test
    void fromInput_unknownOrPartialCommand_unknownReturned() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput("todoist buy milk"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput("bye now"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput(""));
    }

    @Test
    void getCommandText_allCommandTypes_expectedTextReturned() {
        assertEquals("bye", CommandType.BYE.getCommandText());
        assertEquals("help", CommandType.HELP.getCommandText());
        assertEquals("list", CommandType.LIST.getCommandText());
        assertEquals("on", CommandType.ON.getCommandText());
        assertEquals("find", CommandType.FIND.getCommandText());
        assertEquals("delete", CommandType.DELETE.getCommandText());
        assertEquals("mark", CommandType.MARK.getCommandText());
        assertEquals("unmark", CommandType.UNMARK.getCommandText());
        assertEquals("deadline", CommandType.DEADLINE.getCommandText());
        assertEquals("event", CommandType.EVENT.getCommandText());
        assertEquals("todo", CommandType.TODO.getCommandText());
        assertEquals("", CommandType.UNKNOWN.getCommandText());
    }
}
