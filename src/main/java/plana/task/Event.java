package plana.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import plana.storage.Storage;

/**
 * Represents a task that starts and ends on specified dates.
 */
public class Event extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
    private final LocalDate from;
    private final LocalDate to;

    /**
     * Creates an incomplete Event.
     *
     * @param description the task description.
     * @param from the start date.
     * @param to the end date.
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the Event in the format used by the storage file.
     *
     * @return the Event type, completion status, description, start, and end
     */
    @Override
    public String toStorageString() {
        return "E | " + getStorageStatus() + " | " + Storage.escapeField(description)
                + " | " + Storage.escapeField(from.toString()) + " | " + Storage.escapeField(to.toString());
    }

    /**
     * Returns the Event in the format used when displaying the task list.
     *
     * @return the Event type, completion status, description, start, and end
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DISPLAY_FORMAT)
                + " to: " + to.format(DISPLAY_FORMAT) + ")";
    }

    /**
     * Checks whether this event is happening on the supplied date, including
     * either endpoint of the event's date range.
     *
     * @param date the date to check.
     * @return {@code true} if the event includes the date
     */
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
