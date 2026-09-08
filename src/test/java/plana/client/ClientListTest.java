package plana.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import plana.exception.PlanaException;

/**
 * Tests client ordering, references, uniqueness, and replacement.
 */
class ClientListTest {
    private final Client alice = new Client("Alice", "alice@example.com", "", "", "", "");
    private final Client bob = new Client("Bob", "bob@example.com", "", "", "", "");

    @Test
    void add_duplicateEmailRejectedCaseInsensitively() throws PlanaException {
        ClientList clients = new ClientList();
        clients.add(alice);

        PlanaException exception = assertThrows(PlanaException.class, () ->
                clients.add(new Client("Other", "ALICE@example.com", "", "", "", "")));

        assertEquals("Oops, a client with email 'alice@example.com' already exists.", exception.getMessage());
    }

    @Test
    void referencesAndDeletion_followCurrentListPositions() throws PlanaException {
        ClientList clients = new ClientList(List.of(alice, bob));

        assertEquals(alice, clients.get("C1", "view"));
        assertEquals(bob, clients.delete("C2"));
        assertEquals(1, clients.size());
    }

    @Test
    void matchingIndexes_preserveFullListPositions() {
        ClientList clients = new ClientList(List.of(alice, bob));

        assertEquals(List.of(1), clients.matchingIndexes("bob"));
    }
}
