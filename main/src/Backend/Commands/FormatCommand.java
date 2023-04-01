package Backend.Commands;

public class FormatCommand {
    public static String formatCommand(String command) {
        //torolni az elejerol a " "
        //torolni a vegerol a " "
        //torolni a vegerol a ";"
        //torolni a vegerol a ")"

        while (command.charAt(0) == ' ') {
            command = command.substring(1);
        }

        if (command.charAt(command.length() - 1) == ';') {
            command = command.substring(0, command.length() - 1);
        }

        if (command.charAt(command.length() - 1) == ')') {
            command = command.substring(0, command.length() - 1);
        }

        while (command.charAt(command.length() - 1) == ' ') {
            command = command.substring(0, command.length() - 1);

            if (command.charAt(command.length() - 1) == ';') {
                command = command.substring(0, command.length() - 1);
            }

            if (command.charAt(command.length() - 1) == ')') {
                command = command.substring(0, command.length() - 1);
            }
        }

        return command;
    }

    protected static String formatWords(String word) {
        String result = word;
        if (result.charAt(0) == '(') {
            result = result.substring(1);
        }
        if (result.charAt(result.length() - 1) == ';') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ')') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ',') {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
