package plana.command;

import plana.exception.PlanaException;
import plana.storage.Storage;
import plana.task.Task;
import plana.task.TaskList;
import plana.ui.Ui;

/**
 * Marks a selected task as not done and persists the updated task list.
 */
public class UnmarkCommand extends Command {
    private final String taskNumber;

    /**
     * Creates an unmark command for a user-facing task number.
     *
     * @param taskNumber the one-based task number entered by the user.
     */
    public UnmarkCommand(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Marks the selected task as not done, saves the list, and displays the result.
     *
     * @param tasks the task list to update.
     * @param ui the user interface used for the response.
     * @param storage the storage collaborator used for persistence.
     * @throws PlanaException if the task number is invalid
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws PlanaException {
        Task unmarkedTask = tasks.unmark(taskNumber);
        storage.saveTasks(tasks);
        ui.showTaskMarkedNotDone(unmarkedTask);
    }
}
