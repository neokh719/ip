/**
 * Converts raw console input into the command type and arguments used by
 * Plana's command loop.
 */
public class Parser {
    /**
     * Parses one complete line entered by the user.
     *
     * @param input the complete command entered by the user
     * @return the recognized command and its trimmed arguments
     */
    public ParsedCommand parse(String input) {
        CommandType type = CommandType.fromInput(input);
        String arguments = extractArguments(input, type);
        return new ParsedCommand(type, arguments);
    }

    /**
     * Extracts the portion of input after a command keyword.
     *
     * @param input the complete command entered by the user
     * @param type the command recognized from the input
     * @return the command arguments, or an empty string for commands without arguments
     */
    private String extractArguments(String input, CommandType type) {
        if (type == CommandType.UNKNOWN || type == CommandType.HELP || type == CommandType.BYE) {
            return "";
        }
        return input.substring(type.getCommandText().length()).trim();
    }

    /**
     * Holds the result of parsing one user command.
     *
     * @param type the command recognized from the input
     * @param arguments the trimmed text after the command keyword
     */
    public record ParsedCommand(CommandType type, String arguments) {
    }
}
