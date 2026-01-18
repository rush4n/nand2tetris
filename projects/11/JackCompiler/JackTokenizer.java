import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    public enum TokenType { KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST }

    private ArrayList<String> tokens = new ArrayList<>();
    private int cursor = 0;
    private String currentToken;

    private static final HashSet<String> keywords = new HashSet<>(Arrays.asList(
        "class", "constructor", "function", "method", "field", "static", "var",
        "int", "char", "boolean", "void", "true", "false", "null", "this",
        "let", "do", "if", "else", "while", "return"
    ));

    public JackTokenizer(File input) {
        try {
            String content = new String(Files.readAllBytes(input.toPath()));
            // Strip comments
            content = content.replaceAll("(?s)/\\*.*?\\*/", " ");
            content = content.replaceAll("//.*", "");
            
            // Regex handles strings, integers, symbols, and identifiers/keywords
            Pattern p = Pattern.compile("\"[^\"]*\"|\\d+|[\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*/&\\|<>=~]|[a-zA-Z_][a-zA-Z0-9_]*");
            Matcher m = p.matcher(content);
            while (m.find()) {
                tokens.add(m.group());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean hasMoreTokens() { return cursor < tokens.size(); }
    public void advance() { currentToken = tokens.get(cursor++); }
    public String peek() { return tokens.get(cursor); }
    public String getToken() { return currentToken; }
    public TokenType tokenType() {
        if (keywords.contains(currentToken)) return TokenType.KEYWORD;
        if ("{}()[].,;+-*/&|<> =~".contains(currentToken)) return TokenType.SYMBOL;
        if (currentToken.matches("\\d+")) return TokenType.INT_CONST;
        if (currentToken.startsWith("\"")) return TokenType.STRING_CONST;
        return TokenType.IDENTIFIER;
    }
}