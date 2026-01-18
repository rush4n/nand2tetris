import java.io.File;

public class JackCompiler {
    public static void main(String[] args) {
        if (args.length == 0) return;
        File input = new File(args[0]);
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File f : files) {
                if (f.getName().endsWith(".jack")) analyze(f);
            }
        } else {
            analyze(input);
        }
    }

    private static void analyze(File jackFile) {
        JackTokenizer tokenizer = new JackTokenizer(jackFile);
        CompilationEngine engine = new CompilationEngine(tokenizer, jackFile);
        engine.compileClass();
    }
}