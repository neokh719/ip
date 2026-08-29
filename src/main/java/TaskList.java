import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Owns Plana's ordered collection of tasks and provides the operations used by
 * the command loop.
 */
public class TaskList implements Iterable<Task> {
    private final ArrayList<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing a copy of the supplied tasks.
     *
     * @param tasks the initial tasks
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at the specified zero-based index.
     *
     * @param index the zero-based index
     * @return the task at that index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Removes and returns the task at the specified zero-based index.
     *
     * @param index the zero-based index
     * @return the removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Deletes the task selected by a user-facing task number.
     *
     * @param taskNumber the one-based task number entered by the user
     * @return the deleted task
     * @throws PlanaException if the task number is missing, invalid, or out of range
     */
    public Task delete(String taskNumber) throws PlanaException {
        return remove(getTaskIndex(TaskAction.DELETE, taskNumber));
    }

    /**
     * Marks the task selected by a user-facing task number as done.
     *
     * @param taskNumber the one-based task number entered by the user
     * @return the marked task
     * @throws PlanaException if the task number is missing, invalid, or out of range
     */
    public Task mark(String taskNumber) throws PlanaException {
        Task task = getSelectedTask(TaskAction.MARK, taskNumber);
        task.markAsDone();
        return task;
    }

    /**
     * Marks the task selected by a user-facing task number as not done.
     *
     * @param taskNumber the one-based task number entered by the user
     * @return the unmarked task
     * @throws PlanaException if the task number is missing, invalid, or out of range
     */
    public Task unmark(String taskNumber) throws PlanaException {
        Task task = getSelectedTask(TaskAction.UNMARK, taskNumber);
        task.markAsNotDone();
        return task;
    }

    /**
     * Retrieves a task after validating the user-facing task number.
     *
     * @param action the command being performed, used to tailor validation errors
     * @param taskNumber the one-based task number entered by the user
     * @return the selected task
     * @throws PlanaException if the task number is missing, invalid, or out of range
     */
    private Task getSelectedTask(TaskAction action, String taskNumber) throws PlanaException {
        return get(getTaskIndex(action, taskNumber));
    }

    /**
     * Converts a valid user-facing task number into a zero-based list index.
     *
     * @param action the command being performed, used to tailor validation errors
     * @param taskNumber the one-based task number entered by the user
     * @return the zero-based index of the selected task
     * @throws PlanaException if the task number is missing, invalid, or out of range
     */
    private int getTaskIndex(TaskAction action, String taskNumber) throws PlanaException {
        if (taskNumber.isBlank()) {
            throw new PlanaException("Oops, " + action.getCommandText() + " needs a task number."
                    + " Try: " + action.getCommandText() + " <number>.");
        }

        final int taskIndex;
        try {
            taskIndex = Integer.parseInt(taskNumber) - 1;
        } catch (NumberFormatException exception) {
            throw new PlanaException("Oops, '" + taskNumber + "' isn't a valid task number."
                    + " Use a positive whole number, like " + action.getCommandText() + " 1.");
        }

        if (taskIndex < 0) {
            throw new PlanaException("Oops, task numbers start at 1."
                    + " Try " + action.getCommandText() + " 1 or another number from list.");
        }
        if (taskIndex >= size()) {
            throw new PlanaException("Oops, task " + (taskIndex + 1) + " doesn't exist yet."
                    + " Type list to check the task numbers you have.");
        }
        return taskIndex;
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Checks whether the list contains no tasks.
     *
     * @return true when the list is empty
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns an iterator over the tasks in their display order.
     *
     * @return an iterator over this task list
     */
    @Override
    public Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
