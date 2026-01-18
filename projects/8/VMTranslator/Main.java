import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final List<Parser> parsers = new ArrayList<>();

    public static void main(String[] args) {
        File input = new File(args[0]);

        String outputPath;
        if (input.isDirectory()) {
            outputPath = input.getAbsolutePath() + "/" + input.getName() + ".asm";
            collectVMFiles(input);
        } else {
            outputPath = input.getAbsolutePath().replace(".vm", ".asm");
            parsers.add(new Parser(input.getAbsolutePath()));
        }

        CodeWriter codeWriter = new CodeWriter(outputPath);

        if (input.isDirectory()) {
            codeWriter.writeInit();
        }

        for (Parser parser : parsers) {
            codeWriter.setFileName(parser.getFileName());

            while (parser.hasMoreCommands()) {
                parser.advance();

                switch (parser.commandType()) {
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.arg1());
                        break;

                    case C_PUSH:
                        codeWriter.writePushPop(Parser.CommandType.C_PUSH, parser.arg1(), parser.arg2());
                        break;

                    case C_POP:
                        codeWriter.writePushPop(Parser.CommandType.C_POP, parser.arg1(), parser.arg2());
                        break;

                    case C_LABEL:
                        codeWriter.writeLabel(parser.arg1());
                        break;

                    case C_GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;

                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;

                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;

                    case C_CALL:
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;

                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;

                    default:
                        throw new RuntimeException("Unknown command: " + parser.getThisCommand());
                }
            }
        }

        codeWriter.close();
    }

    private static void collectVMFiles(File dir) {
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".vm")) {
                parsers.add(new Parser(file.getAbsolutePath()));
            }
        }
    }
}
