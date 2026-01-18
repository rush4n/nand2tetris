import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        try {
            Parser parser = new Parser(args[0]);
            Code translator = new Code();
            SymbolTable table = new SymbolTable();

            BufferedWriter writer = new BufferedWriter(new FileWriter(args[0].replace(".asm", ".hack")));

            while (parser.hasMoreCommands()) {
                parser.advance();

                if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                    table.addEntry(parser.symbol(), String.valueOf(parser.getPointer() - parser.getLabelCount()));
                    parser.incrementLabel();
                }
            }

            parser.resetPointer();

            while (parser.hasMoreCommands()) {
                parser.advance();

                switch (parser.commandType()) {

                    case A_COMMAND:
                        String symbol = parser.symbol();

                        if (parser.isVariable(symbol)) {
                            if (!table.contains(symbol)) table.addVariable(symbol);

                            symbol = Code.buildAInstruction(table.getAddress(symbol));
                        } else symbol = Code.buildAInstruction(symbol);


                        writer.write(symbol);
                        break;

                    case C_COMMAND:
                        String dest = translator.getDestBits(parser.dest());
                        String comp = translator.getCompBits(parser.comp());
                        String jump = translator.getJumpBits(parser.jump());

                        writer.write("111" + comp + dest + jump);
                        break;

                    case L_COMMAND:
                        continue;

                    default:
                        throw new RuntimeException("Illegal instruction at line " + parser.getPointer());
                }

                writer.newLine();
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
