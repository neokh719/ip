package plana.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import plana.client.Client;
import plana.client.ClientList;

/**
 * Tests client persistence, escaping, malformed records, and duplicate handling.
 */
class ClientStorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void saveAndLoadClients_roundTripPreservesFields() throws Exception {
        Client client = new Client("Alice | Tan", "ALICE@example.com", "91234567",
                "12 Baker Street", "no nuts", "birthday cake");
        ClientList clients = new ClientList(List.of(client));
        ClientStorage storage = new ClientStorage(temporaryDirectory.resolve("clients.txt").toString());

        storage.saveClients(clients);
        Client loadedClient = storage.loadClients().get(0);

        assertEquals(client.toStorageString(), loadedClient.toStorageString());
        assertEquals("alice@example.com", loadedClient.getEmail());
    }

    @Test
    void loadClients_malformedAndDuplicateRecordsValidRecordsPreserved() throws IOException {
        Path dataFile = temporaryDirectory.resolve("clients.txt");
        Files.writeString(dataFile, String.join(System.lineSeparator(),
                "C | alice@example.com | Alice | 91234567 |  |  | ",
                "C | ALICE@example.com | Duplicate | 91234567 |  |  | ",
                "C | invalid | Missing valid domain |  |  |  | ",
                "not a client record",
                "C | bob@example.com | Bob |  |  | vegan | "));

        ClientList clients = new ClientStorage(dataFile.toString()).loadClients();

        assertEquals(2, clients.size());
        assertEquals("alice@example.com", clients.get(0).getEmail());
        assertEquals("bob@example.com", clients.get(1).getEmail());
    }

    @Test
    void loadClients_missingFile_emptyListReturned() {
        ClientList clients = new ClientStorage(temporaryDirectory.resolve("missing.txt").toString()).loadClients();

        assertTrue(clients.size() == 0);
    }
}
