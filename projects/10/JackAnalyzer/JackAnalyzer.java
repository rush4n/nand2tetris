import java.io.File;

public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length == 0) return;
        File input = new File(args[0]);
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            for (File f : files) {
                if (f.getName().endsWith(".jack")) {
                    analyze(f);
                }
            }
        } else {
            analyze(input);
        }
    }

    private static void analyze(File jackFile) {
        String outputFileName = jackFile.getAbsolutePath().replace(".jack", ".xml");
        JackTokenizer tokenizer = new JackTokenizer(jackFile);
        CompilationEngine engine = new CompilationEngine(tokenizer, new File(outputFileName));
        engine.compileClass();
    }
}