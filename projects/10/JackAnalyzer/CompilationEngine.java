import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private JackTokenizer jt;
    private BufferedWriter out;

    public CompilationEngine(JackTokenizer tokenizer, File output) {
        this.jt = tokenizer;
        try {
            this.out = new BufferedWriter(new FileWriter(output));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(String s) {
        try {
            out.write(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eat(String expected) {
        jt.advance();
        String t = jt.getToken();
        String tag = "";
        switch (jt.tokenType()) {
            case KEYWORD: tag = "keyword"; break;
            case SYMBOL: tag = "symbol"; t = escape(t); break;
            case IDENTIFIER: tag = "identifier"; break;
            case INT_CONST: tag = "integerConstant"; break;
            case STRING_CONST: tag = "stringConstant"; t = t.substring(1, t.length() - 1); break;
        }
        write("<" + tag + "> " + t + " </" + tag + ">");
    }

    private String escape(String s) {
        if (s.equals("<")) return "&lt;";
        if (s.equals(">")) return "&gt;";
        if (s.equals("&")) return "&amp;";
        return s;
    }

    public void compileClass() {
        write("<class>");
        eat("class");
        eat("className");
        eat("{");
        while (jt.hasMoreTokens()) {
            String p = jt.peek();
            if (p.equals("static") || p.equals("field")) compileClassVarDec();
            else if (p.equals("constructor") || p.equals("function") || p.equals("method")) compileSubroutine();
            else break;
        }
        eat("}");
        write("</class>");
        try { out.close(); } catch (IOException e) {}
    }

    public void compileClassVarDec() {
        write("<classVarDec>");
        eat(null); eat(null); eat(null);
        while (jt.peek().equals(",")) { eat(","); eat(null); }
        eat(";");
        write("</classVarDec>");
    }

    public void compileSubroutine() {
        write("<subroutineDec>");
        eat(null); eat(null); eat(null); eat("(");
        compileParameterList();
        eat(")");
        write("<subroutineBody>");
        eat("{");
        while (jt.peek().equals("var")) compileVarDec();
        compileStatements();
        eat("}");
        write("</subroutineBody>");
        write("</subroutineDec>");
    }

    public void compileParameterList() {
        write("<parameterList>");
        if (!jt.peek().equals(")")) {
            eat(null); eat(null);
            while (jt.peek().equals(",")) { eat(","); eat(null); eat(null); }
        }
        write("</parameterList>");
    }

    public void compileVarDec() {
        write("<varDec>");
        eat("var"); eat(null); eat(null);
        while (jt.peek().equals(",")) { eat(","); eat(null); }
        eat(";");
        write("</varDec>");
    }

    public void compileStatements() {
        write("<statements>");
        while (true) {
            String p = jt.peek();
            if (p.equals("let")) compileLet();
            else if (p.equals("if")) compileIf();
            else if (p.equals("while")) compileWhile();
            else if (p.equals("do")) compileDo();
            else if (p.equals("return")) compileReturn();
            else break;
        }
        write("</statements>");
    }

    public void compileLet() {
        write("<letStatement>");
        eat("let"); eat(null);
        if (jt.peek().equals("[")) { eat("["); compileExpression(); eat("]"); }
        eat("="); compileExpression(); eat(";");
        write("</letStatement>");
    }

    public void compileIf() {
        write("<ifStatement>");
        eat("if"); eat("("); compileExpression(); eat(")"); eat("{"); compileStatements(); eat("}");
        if (jt.peek().equals("else")) { eat("else"); eat("{"); compileStatements(); eat("}"); }
        write("</ifStatement>");
    }

    public void compileWhile() {
        write("<whileStatement>");
        eat("while"); eat("("); compileExpression(); eat(")"); eat("{"); compileStatements(); eat("}");
        write("</whileStatement>");
    }

    public void compileDo() {
        write("<doStatement>");
        eat("do"); eat(null);
        if (jt.peek().equals(".")) { eat("."); eat(null); }
        eat("("); compileExpressionList(); eat(")"); eat(";");
        write("</doStatement>");
    }

    public void compileReturn() {
        write("<returnStatement>");
        eat("return");
        if (!jt.peek().equals(";")) compileExpression();
        eat(";");
        write("</returnStatement>");
    }

    public void compileExpression() {
        write("<expression>");
        compileTerm();
        String ops = "+-*/&|<>=";
        while (jt.hasMoreTokens() && ops.contains(jt.peek())) {
            eat(null); compileTerm();
        }
        write("</expression>");
    }

    public void compileTerm() {
        write("<term>");
        String p = jt.peek();
        if (p.equals("(") ) {
            eat("("); compileExpression(); eat(")");
        } else if (p.equals("-") || p.equals("~")) {
            eat(null); compileTerm();
        } else {
            eat(null);
            String next = jt.peek();
            if (next.equals("[")) {
                eat("["); compileExpression(); eat("]");
            } else if (next.equals("(")) {
                eat("("); compileExpressionList(); eat(")");
            } else if (next.equals(".")) {
                eat("."); eat(null); eat("("); compileExpressionList(); eat(")");
            }
        }
        write("</term>");
    }

    public void compileExpressionList() {
        write("<expressionList>");
        if (!jt.peek().equals(")")) {
            compileExpression();
            while (jt.peek().equals(",")) { eat(","); compileExpression(); }
        }
        write("</expressionList>");
    }
}