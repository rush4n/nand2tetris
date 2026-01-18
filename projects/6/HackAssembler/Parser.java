import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser {

    enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND
    }

    private List<String> instructionBuffer;
    private int currentPos;
    private int labelCount;
    private String activeCommand;

    public Parser() {}

    public Parser(String path) {

        labelCount = 0;
        resetPointer();
        instructionBuffer = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();

            while (line != null) {
                line = line.replaceAll(" ", "");

                if (line.equals("") || (line.charAt(0) == '/' && line.charAt(1) == '/')) {
                    line = reader.readLine();
                    continue;
                }

                instructionBuffer.add(line.split("//")[0]);
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean hasMoreCommands() { return currentPos < instructionBuffer.size() - 1; }

    public void advance() {
        currentPos++;
        activeCommand = instructionBuffer.get(currentPos);
    }

    public CommandType commandType() {
        if (activeCommand.startsWith("@")) return CommandType.A_COMMAND;
        else if (activeCommand.startsWith("(")) return CommandType.L_COMMAND;

        return CommandType.C_COMMAND;
    }

    public String symbol() {
        if (commandType() == CommandType.A_COMMAND) return activeCommand.substring(1);

        return activeCommand.replace("(", "").replace(")", "");
    }

    public static boolean isVariable(String value) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return !pattern.matcher(value).matches();
    }

    public String dest() {
        if (activeCommand.contains("=")) return activeCommand.split("=")[0];

        return "null";
    }

    public String comp() {
        if (activeCommand.contains("=")) return activeCommand.split("=")[1];
        else if (activeCommand.contains(";")) return activeCommand.split(";")[0];

        return activeCommand;
    }

    public String jump() {
        if (activeCommand.contains(";")) return activeCommand.split(";")[1];

        return "null";
    }

    public void resetPointer() { currentPos = -1; }

    public int getPointer() { return currentPos; }

    public void incrementLabel() { labelCount++; }

    public int getLabelCount() { return labelCount; }
}
