import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private BufferedWriter bw;
    private String fileName;
    private int labelCounter;
    private String currentFunction = "";

    
    public CodeWriter(String outputPath) {
        try {
            labelCounter = 0;
            File file = new File(outputPath.replace(".vm", ".asm"));
            fileName = file.getName().replace(".asm", "");
            bw = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        try {
            bw.newLine();
            bw.write("// " + command);
            bw.newLine();
            switch (command) {
                case "add":
                    binaryOp("M=D+M");
                    break;
                case "sub":
                    binaryOp("M=M-D");
                    break;
                case "and":
                    binaryOp("M=D&M");
                    break;
                case "or":
                    binaryOp("M=D|M");
                    break;
                case "neg":
                    unaryOp("M=-M");
                    break;
                case "not":
                    unaryOp("M=!M");
                    break;
                case "eq":
                    compare("JEQ");
                    break;
                case "gt":
                    compare("JGT");
                    break;
                case "lt":
                    compare("JLT");
                    break;
                default:
                    throw new RuntimeException("Illegal arithmetic");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binaryOp(String expr) throws IOException {
        popToD();
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M-1");
        bw.newLine();
        bw.write(expr);
        bw.newLine();
    }

    private void unaryOp(String expr) throws IOException {
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M-1");
        bw.newLine();
        bw.write(expr);
        bw.newLine();
    }

    private void compare(String jump) throws IOException {
        String trueLabel = "TRUE_" + labelCounter;
        String endLabel = "END_" + labelCounter++;
        popToD();
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M-1");
        bw.newLine();
        bw.write("D=M-D");
        bw.newLine();
        bw.write("@" + trueLabel);
        bw.newLine();
        bw.write("D;" + jump);
        bw.newLine();
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M-1");
        bw.newLine();
        bw.write("M=0");
        bw.newLine();
        bw.write("@" + endLabel);
        bw.newLine();
        bw.write("0;JMP");
        bw.newLine();
        bw.write("(" + trueLabel + ")");
        bw.newLine();
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M-1");
        bw.newLine();
        bw.write("M=-1");
        bw.newLine();
        bw.write("(" + endLabel + ")");
        bw.newLine();
    }

    public void writePushPop(Parser.CommandType type, String segment, int index) {
        try {
            bw.newLine();
            bw.write("// " + (type == Parser.CommandType.C_PUSH ? "push " : "pop ") + segment + " " + index);
            bw.newLine();
            if (type == Parser.CommandType.C_PUSH) push(segment, index);
            else pop(segment, index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void push(String segment, int index) throws IOException {
        switch (segment) {
            case "constant":
                bw.write("@" + index);
                bw.newLine();
                bw.write("D=A");
                bw.newLine();
                break;
            case "local":
                segmentPush("LCL", index);
                return;
            case "argument":
                segmentPush("ARG", index);
                return;
            case "this":
                segmentPush("THIS", index);
                return;
            case "that":
                segmentPush("THAT", index);
                return;
            case "temp":
                bw.write("@R" + (5 + index));
                bw.newLine();
                bw.write("D=M");
                bw.newLine();
                break;
            case "pointer":
                bw.write(index == 0 ? "@THIS" : "@THAT");
                bw.newLine();
                bw.write("D=M");
                bw.newLine();
                break;
            case "static":
                bw.write("@" + fileName + "." + index);
                bw.newLine();
                bw.write("D=M");
                bw.newLine();
                break;
        }
        pushD();
    }

    private void pop(String segment, int index) throws IOException {
        switch (segment) {
            case "local":
                segmentPop("LCL", index);
                break;
            case "argument":
                segmentPop("ARG", index);
                break;
            case "this":
                segmentPop("THIS", index);
                break;
            case "that":
                segmentPop("THAT", index);
                break;
            case "temp":
                popToD();
                bw.write("@R" + (5 + index));
                bw.newLine();
                bw.write("M=D");
                bw.newLine();
                break;
            case "pointer":
                popToD();
                bw.write(index == 0 ? "@THIS" : "@THAT");
                bw.newLine();
                bw.write("M=D");
                bw.newLine();
                break;
            case "static":
                popToD();
                bw.write("@" + fileName + "." + index);
                bw.newLine();
                bw.write("M=D");
                bw.newLine();
                break;
        }
    }

    private void segmentPush(String base, int index) throws IOException {
        bw.write("@" + base);
        bw.newLine();
        bw.write("D=M");
        bw.newLine();
        bw.write("@" + index);
        bw.newLine();
        bw.write("A=D+A");
        bw.newLine();
        bw.write("D=M");
        bw.newLine();
        pushD();
    }

    private void segmentPop(String base, int index) throws IOException {
        bw.write("@" + base);
        bw.newLine();
        bw.write("D=M");
        bw.newLine();
        bw.write("@" + index);
        bw.newLine();
        bw.write("D=D+A");
        bw.newLine();
        bw.write("@R13");
        bw.newLine();
        bw.write("M=D");
        bw.newLine();
        popToD();
        bw.write("@R13");
        bw.newLine();
        bw.write("A=M");
        bw.newLine();
        bw.write("M=D");
        bw.newLine();
    }

    private void pushD() throws IOException {
        bw.write("@SP");
        bw.newLine();
        bw.write("A=M");
        bw.newLine();
        bw.write("M=D");
        bw.newLine();
        bw.write("@SP");
        bw.newLine();
        bw.write("M=M+1");
        bw.newLine();
    }

    private void popToD() throws IOException {
        bw.write("@SP");
        bw.newLine();
        bw.write("M=M-1");
        bw.newLine();
        bw.write("A=M");
        bw.newLine();
        bw.write("D=M");
        bw.newLine();
    }

    public void writeLabel(String label) {
        try {
            bw.write("(" + scoped(label) + ")");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeGoto(String label) {
        try {
            bw.write("@" + scoped(label));
            bw.newLine();
            bw.write("0;JMP");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIf(String label) {
        try {
            popToD();
            bw.write("@" + scoped(label));
            bw.newLine();
            bw.write("D;JNE");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFunction(String functionName, int numLocals) {
        try {
            currentFunction = functionName;
            bw.write("(" + functionName + ")");
            bw.newLine();

            for (int i = 0; i < numLocals; i++) {
                bw.write("@0"); bw.newLine();
                bw.write("D=A"); bw.newLine();
                pushD();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeReturn() {
        try {
            bw.write("@LCL"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            bw.write("@R13"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            bw.write("@5"); bw.newLine();
            bw.write("A=D-A"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            bw.write("@R14"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            popToD();
            bw.write("@ARG"); bw.newLine();
            bw.write("A=M"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            bw.write("@ARG"); bw.newLine();
            bw.write("D=M+1"); bw.newLine();
            bw.write("@SP"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            restore("THAT", 1);
            restore("THIS", 2);
            restore("ARG", 3);
            restore("LCL", 4);

            bw.write("@R14"); bw.newLine();
            bw.write("A=M"); bw.newLine();
            bw.write("0;JMP"); bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCall(String functionName, int numArgs) {
        try {
            String returnLabel = "RET_" + labelCounter++;

            bw.write("@" + returnLabel); bw.newLine();
            bw.write("D=A"); bw.newLine();
            pushD();

            bw.write("@LCL"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            pushD();

            bw.write("@ARG"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            pushD();

            bw.write("@THIS"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            pushD();

            bw.write("@THAT"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            pushD();

            bw.write("@SP"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            bw.write("@" + (numArgs + 5)); bw.newLine();
            bw.write("D=D-A"); bw.newLine();
            bw.write("@ARG"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            bw.write("@SP"); bw.newLine();
            bw.write("D=M"); bw.newLine();
            bw.write("@LCL"); bw.newLine();
            bw.write("M=D"); bw.newLine();

            bw.write("@" + functionName); bw.newLine();
            bw.write("0;JMP"); bw.newLine();

            bw.write("(" + returnLabel + ")"); bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeInit() {
        try {
            bw.write("@256"); bw.newLine();
            bw.write("D=A"); bw.newLine();
            bw.write("@SP"); bw.newLine();
            bw.write("M=D"); bw.newLine();
            writeCall("Sys.init", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void restore(String segment, int offset) throws IOException {
        bw.write("@R13"); bw.newLine();
        bw.write("D=M"); bw.newLine();
        bw.write("@" + offset); bw.newLine();
        bw.write("A=D-A"); bw.newLine();
        bw.write("D=M"); bw.newLine();
        bw.write("@" + segment); bw.newLine();
        bw.write("M=D"); bw.newLine();
    }

    private String scoped(String label) {
        return currentFunction.isEmpty() ? label : currentFunction + "$" + label;
    }

    public void close() {
        try {
            bw.write("(END)");
            bw.newLine();
            bw.write("@END");
            bw.newLine();
            bw.write("0;JMP");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}