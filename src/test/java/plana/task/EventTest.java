package plana.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests whether an {@link Event} includes a supplied date.
 */
public class EventTest {
    private static final LocalDate EVENT_START = LocalDate.of(2026, 8, 31);
    private static final LocalDate EVENT_END = LocalDate.of(2026, 9, 2);

    /**
     * Verifies that an event includes its start date, dates within its range,
     * and its end date.
     */
    @Test
    public void occursOn_dateWithinOrAtBoundary_trueReturned() {
        Event event = new Event("team planning", EVENT_START, EVENT_END);

        assertTrue(event.occursOn(EVENT_START));
        assertTrue(event.occursOn(EVENT_START.plusDays(1)));
        assertTrue(event.occursOn(EVENT_END));
    }

    /**
     * Verifies that an event does not include dates immediately before or
     * immediately after its date range.
     */
    @Test
    public void occursOn_dateOutsideRange_falseReturned() {
        Event event = new Event("team planning", EVENT_START, EVENT_END);

        assertFalse(event.occursOn(EVENT_START.minusDays(1)));
        assertFalse(event.occursOn(EVENT_END.plusDays(1)));
    }

    /**
     * Verifies that a one-day event includes only its single event date.
     */
    @Test
    public void occursOn_oneDayEvent_onlyEventDateIncluded() {
        Event event = new Event("presentation", EVENT_START, EVENT_START);

        assertTrue(event.occursOn(EVENT_START));
        assertFalse(event.occursOn(EVENT_START.minusDays(1)));
        assertFalse(event.occursOn(EVENT_START.plusDays(1)));
    }
}
