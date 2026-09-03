package plana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plana.command.CommandType;
import plana.storage.Storage;

/**
 * Tests Plana's reusable command-response API used by the JavaFX interface.
 */
class PlanaTest {
    @Test
    void getResponse_taskCommandsUseExistingBehaviorAndPersist(@TempDir Path temporaryDirectory) {
        Plana plana = new Plana(new Storage(temporaryDirectory.resolve("tasks.txt").toString()));

        Plana.Response addResponse = plana.getResponse("todo read a book");
        Plana.Response listResponse = plana.getResponse("list");

        assertEquals(CommandType.TODO, addResponse.commandType());
        assertFalse(addResponse.exit());
        assertTrue(addResponse.text().contains("Yay, I've added this task:"));
        assertTrue(listResponse.text().contains("1.[T][ ] read a book"));
        assertTrue(new Storage(temporaryDirectory.resolve("tasks.txt").toString())
                .loadTasks().get(0).toString().contains("read a book"));
    }

    @Test
    void getResponse_errorsAndByeReturnChatMetadata(@TempDir Path temporaryDirectory) {
        Plana plana = new Plana(new Storage(temporaryDirectory.resolve("tasks.txt").toString()));

        Plana.Response errorResponse = plana.getResponse("not a command");
        Plana.Response byeResponse = plana.getResponse("bye");

        assertEquals(CommandType.UNKNOWN, errorResponse.commandType());
        assertFalse(errorResponse.exit());
        assertTrue(errorResponse.text().contains("I don't recognize 'not a command'"));
        assertEquals(CommandType.BYE, byeResponse.commandType());
        assertTrue(byeResponse.exit());
        assertTrue(byeResponse.text().contains("Bye-bye! See you next time, okay?"));
    }
}
