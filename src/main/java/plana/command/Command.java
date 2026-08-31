package plana.command;

import plana.exception.PlanaException;
import plana.storage.Storage;
import plana.task.TaskList;
import plana.ui.Ui;

/**
 * Represents one executable command entered by the user.
 *
 * <p>Concrete command classes will gradually take over the command cases that
 * are currently handled directly by Plana.</p>
 */
public abstract class Command {
    /**
     * Executes this command using the application's collaborators.
     *
     * @param tasks the current task list.
     * @param ui the user interface used for responses.
     * @param storage the task storage used for persistence.
     * @throws PlanaException if the command cannot be completed
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws PlanaException;

    /**
     * Indicates whether executing this command should end the application.
     *
     * @return true when the command exits Plana
     */
    public boolean isExit() {
        return false;
    }
}
