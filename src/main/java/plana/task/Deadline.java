package plana.task;

import plana.storage.Storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that must be completed by a specified date.
 */
public class Deadline extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
    private final LocalDate dueDate;

    /**
     * Creates an incomplete Deadline.
     *
     * @param description the task description
     * @param dueDate the due date
     */
    public Deadline(String description, LocalDate dueDate) {
        super(description);
        this.dueDate = dueDate;
    }

    /**
     * Returns the Deadline in the format used by the storage file.
     *
     * @return the Deadline type, completion status, description, and due date
     */
    @Override
    public String toStorageString() {
        return "D | " + getStorageStatus() + " | " + Storage.escapeField(description)
                + " | " + Storage.escapeField(dueDate.toString());
    }

    /**
     * Returns the Deadline in the format used when displaying the task list.
     *
     * @return the Deadline type, completion status, description, and due date
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + dueDate.format(DISPLAY_FORMAT) + ")";
    }

    /**
     * Checks whether this deadline is due on the supplied date.
     *
     * @param date the date to check
     * @return {@code true} if this deadline is due on the date
     */
    public boolean isOn(LocalDate date) {
        return dueDate.equals(date);
    }
}
