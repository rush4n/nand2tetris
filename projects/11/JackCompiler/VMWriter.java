import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    public enum Segment { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP }
    public enum Command { ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT }
    private BufferedWriter out;

    public VMWriter(File output) {
        try { out = new BufferedWriter(new FileWriter(output)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void writePush(Segment seg, int idx) { write("push " + segToString(seg) + " " + idx); }
    public void writePop(Segment seg, int idx) { write("pop " + segToString(seg) + " " + idx); }
    public void writeArithmetic(Command cmd) { write(cmd.toString().toLowerCase()); }
    public void writeLabel(String label) { write("label " + label); }
    public void writeGoto(String label) { write("goto " + label); }
    public void writeIf(String label) { write("if-goto " + label); }
    public void writeCall(String name, int nArgs) { write("call " + name + " " + nArgs); }
    public void writeFunction(String name, int nLocals) { write("function " + name + " " + nLocals); }
    public void writeReturn() { write("return"); }

    private String segToString(Segment seg) {
        if (seg == Segment.CONST) return "constant";
        if (seg == Segment.ARG) return "argument";
        return seg.toString().toLowerCase();
    }

    private void write(String s) {
        try { out.write(s + "\n"); } catch (IOException e) { e.printStackTrace(); }
    }
    public void close() { try { out.close(); } catch (IOException e) { e.printStackTrace(); } }
}