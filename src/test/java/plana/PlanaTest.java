package plana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plana.command.CommandType;
import plana.storage.ClientStorage;
import plana.storage.Storage;

/**
 * Tests Plana's reusable command-response API used by the JavaFX interface.
 */
class PlanaTest {
    @Test
    void welcomeBanner_artAppearsBeforeGreeting() {
        String banner = Plana.getWelcomeBanner();
        String greeting = Plana.getWelcomeGreeting();
        String introduction = banner + greeting;

        assertTrue(banner.contains("⣿⣿⣿⣿"));
        assertTrue(banner.contains(" ____  _"));
        assertTrue(introduction.indexOf(" ____  _") < introduction.indexOf("Hi hi!"));
    }

    @Test
    void getResponse_taskCommandsUseExistingBehaviorAndPersist(@TempDir Path temporaryDirectory) {
        Plana plana = new Plana(new Storage(temporaryDirectory.resolve("tasks.txt").toString()),
                new ClientStorage(temporaryDirectory.resolve("clients.txt").toString()));

        Plana.Response addResponse = plana.getResponse("todo read a book");
        Plana.Response listResponse = plana.getResponse("list");

        assertEquals(CommandType.TODO, addResponse.commandType());
        assertFalse(addResponse.exit());
        assertFalse(addResponse.error());
        assertTrue(addResponse.text().contains("Yay, I've added this task:"));
        assertTrue(listResponse.text().contains("1.[T][ ] read a book"));
        assertTrue(new Storage(temporaryDirectory.resolve("tasks.txt").toString())
                .loadTasks().get(0).toString().contains("read a book"));
    }

    @Test
    void getResponse_errorsAndByeReturnChatMetadata(@TempDir Path temporaryDirectory) {
        Plana plana = new Plana(new Storage(temporaryDirectory.resolve("tasks.txt").toString()),
                new ClientStorage(temporaryDirectory.resolve("clients.txt").toString()));

        Plana.Response errorResponse = plana.getResponse("not a command");
        Plana.Response malformedResponse = plana.getResponse("todo");
        Plana.Response byeResponse = plana.getResponse("bye");

        assertEquals(CommandType.UNKNOWN, errorResponse.commandType());
        assertFalse(errorResponse.exit());
        assertTrue(errorResponse.error());
        assertTrue(errorResponse.text().contains("I don't recognize 'not a command'"));
        assertEquals(CommandType.TODO, malformedResponse.commandType());
        assertTrue(malformedResponse.error());
        assertEquals(CommandType.BYE, byeResponse.commandType());
        assertTrue(byeResponse.exit());
        assertFalse(byeResponse.error());
        assertTrue(byeResponse.text().contains("Bye-bye! See you next time, okay?"));
    }

    @Test
    void getResponse_clientCommandsUseSeparateListAndStorage(@TempDir Path temporaryDirectory) {
        ClientStorage clientStorage = new ClientStorage(temporaryDirectory.resolve("clients.txt").toString());
        Plana plana = new Plana(new Storage(temporaryDirectory.resolve("tasks.txt").toString()), clientStorage);

        Plana.Response addResponse = plana.getResponse("client add Alice /email alice@example.com");
        Plana.Response viewResponse = plana.getResponse("client view C1");
        Plana.Response taskListResponse = plana.getResponse("list");

        assertEquals(CommandType.CLIENT, addResponse.commandType());
        assertTrue(addResponse.text().contains("I've added this client"));
        assertTrue(viewResponse.text().contains("Email: alice@example.com"));
        assertFalse(taskListResponse.text().contains("alice@example.com"));
        assertEquals("alice@example.com", clientStorage.loadClients().get(0).getEmail());
    }
}
