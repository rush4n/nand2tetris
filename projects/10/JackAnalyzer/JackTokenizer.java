import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    public enum TokenType { KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST }

    private ArrayList<String> tokens = new ArrayList<>();
    private int cursor = 0;
    private String currentToken;

    private static final String keywords = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
    private static final String symbols = "\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|\\;|\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\=|\\~";
    private static final Pattern tokenPattern = Pattern.compile("(" + keywords + ")|(" + symbols + ")|(\\d+)|(\"[^\"]*\")|([a-zA-Z_][a-zA-Z0-9_]*)");

    public JackTokenizer(File input) {
        try {
            Scanner scanner = new Scanner(input);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int commentIdx = line.indexOf("//");
                if (commentIdx != -1) line = line.substring(0, commentIdx);
                sb.append(line).append("\n");
            }
            scanner.close();
            String code = sb.toString().replaceAll("/\\*\\*?.*?\\*/", "");
            Matcher m = tokenPattern.matcher(code);
            while (m.find()) {
                tokens.add(m.group());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreTokens() { return cursor < tokens.size(); }

    public void advance() { currentToken = tokens.get(cursor++); }

    public void retreat() { cursor--; }

    public String peek() { return tokens.get(cursor); }

    public String getToken() { return currentToken; }

    public TokenType tokenType() {
        if (currentToken.matches(keywords)) return TokenType.KEYWORD;
        if (currentToken.matches(symbols)) return TokenType.SYMBOL;
        if (currentToken.matches("\\d+")) return TokenType.INT_CONST;
        if (currentToken.startsWith("\"")) return TokenType.STRING_CONST;
        return TokenType.IDENTIFIER;
    }
}