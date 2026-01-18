import java.io.File;

public class CompilationEngine {
    private JackTokenizer jt;
    private VMWriter vmw;
    private SymbolTable st;
    private String className;
    private int labelIdx = 0;

    public CompilationEngine(JackTokenizer tokenizer, File jackFile) {
        jt = tokenizer;
        st = new SymbolTable();
        className = jackFile.getName().replace(".jack", "");
        vmw = new VMWriter(new File(jackFile.getAbsolutePath().replace(".jack", ".vm")));
    }

    public void compileClass() {
        jt.advance(); // class
        jt.advance(); className = jt.getToken();
        jt.advance(); // {
        while (jt.hasMoreTokens() && !jt.peek().equals("}")) {
            String p = jt.peek();
            if (p.equals("static") || p.equals("field")) compileClassVarDec();
            else compileSubroutine();
        }
        jt.advance(); // }
        vmw.close();
    }

    public void compileClassVarDec() {
        jt.advance(); String kindStr = jt.getToken();
        SymbolTable.Kind kind = kindStr.equals("static") ? SymbolTable.Kind.STATIC : SymbolTable.Kind.FIELD;
        jt.advance(); String type = jt.getToken();
        jt.advance(); String name = jt.getToken();
        st.define(name, type, kind);
        while (jt.peek().equals(",")) { jt.advance(); jt.advance(); st.define(jt.getToken(), type, kind); }
        jt.advance(); // ;
    }

    public void compileSubroutine() {
        st.startSubroutine();
        jt.advance(); String subType = jt.getToken();
        jt.advance(); String subRetType = jt.getToken();
        jt.advance(); String subName = jt.getToken();
        if (subType.equals("method")) st.define("this", className, SymbolTable.Kind.ARG);
        jt.advance(); // (
        compileParameterList();
        jt.advance(); // )
        jt.advance(); // {
        while (jt.peek().equals("var")) compileVarDec();
        vmw.writeFunction(className + "." + subName, st.varCount(SymbolTable.Kind.VAR));
        if (subType.equals("constructor")) {
            vmw.writePush(VMWriter.Segment.CONST, st.varCount(SymbolTable.Kind.FIELD));
            vmw.writeCall("Memory.alloc", 1);
            vmw.writePop(VMWriter.Segment.POINTER, 0);
        } else if (subType.equals("method")) {
            vmw.writePush(VMWriter.Segment.ARG, 0);
            vmw.writePop(VMWriter.Segment.POINTER, 0);
        }
        compileStatements();
        jt.advance(); // }
    }

    public void compileParameterList() {
        if (!jt.peek().equals(")")) {
            jt.advance(); String type = jt.getToken();
            jt.advance(); String name = jt.getToken();
            st.define(name, type, SymbolTable.Kind.ARG);
            while (jt.peek().equals(",")) {
                jt.advance(); jt.advance(); type = jt.getToken();
                jt.advance(); name = jt.getToken();
                st.define(name, type, SymbolTable.Kind.ARG);
            }
        }
    }

    public void compileVarDec() {
        jt.advance(); // var
        jt.advance(); String type = jt.getToken();
        jt.advance(); String name = jt.getToken();
        st.define(name, type, SymbolTable.Kind.VAR);
        while (jt.peek().equals(",")) {
            jt.advance(); jt.advance();
            st.define(jt.getToken(), type, SymbolTable.Kind.VAR);
        }
        jt.advance(); // ;
    }

    public void compileStatements() {
        while (jt.hasMoreTokens()) {
            String p = jt.peek();
            if (p.equals("let")) compileLet();
            else if (p.equals("if")) compileIf();
            else if (p.equals("while")) compileWhile();
            else if (p.equals("do")) compileDo();
            else if (p.equals("return")) compileReturn();
            else break;
        }
    }

    public void compileLet() {
        jt.advance(); // let
        jt.advance(); String varName = jt.getToken();
        boolean isArray = false;
        if (jt.peek().equals("[")) {
            isArray = true;
            pushIdentifier(varName);
            jt.advance(); // [
            compileExpression();
            jt.advance(); // ]
            vmw.writeArithmetic(VMWriter.Command.ADD);
        }
        jt.advance(); // =
        compileExpression();
        jt.advance(); // ;
        if (isArray) {
            vmw.writePop(VMWriter.Segment.TEMP, 0);
            vmw.writePop(VMWriter.Segment.POINTER, 1);
            vmw.writePush(VMWriter.Segment.TEMP, 0);
            vmw.writePop(VMWriter.Segment.THAT, 0);
        } else popIdentifier(varName);
    }

    public void compileIf() {
        String L1 = "IF_TRUE" + labelIdx, L2 = "IF_FALSE" + labelIdx, L3 = "IF_END" + labelIdx++;
        jt.advance(); // if
        jt.advance(); // (
        compileExpression();
        jt.advance(); // )
        vmw.writeIf(L1);
        vmw.writeGoto(L2);
        vmw.writeLabel(L1);
        jt.advance(); // {
        compileStatements();
        jt.advance(); // }
        if (jt.peek().equals("else")) {
            vmw.writeGoto(L3);
            vmw.writeLabel(L2);
            jt.advance(); // else
            jt.advance(); // {
            compileStatements();
            jt.advance(); // }
            vmw.writeLabel(L3);
        } else vmw.writeLabel(L2);
    }

    public void compileWhile() {
        String L1 = "WHILE_EXP" + labelIdx, L2 = "WHILE_END" + labelIdx++;
        vmw.writeLabel(L1);
        jt.advance(); // while
        jt.advance(); // (
        compileExpression();
        jt.advance(); // )
        vmw.writeArithmetic(VMWriter.Command.NOT);
        vmw.writeIf(L2);
        jt.advance(); // {
        compileStatements();
        jt.advance(); // }
        vmw.writeGoto(L1);
        vmw.writeLabel(L2);
    }

    public void compileDo() {
        jt.advance(); // do
        compileTerm();
        vmw.writePop(VMWriter.Segment.TEMP, 0);
        jt.advance(); // ;
    }

    public void compileReturn() {
        jt.advance(); // return
        if (!jt.peek().equals(";")) compileExpression();
        else vmw.writePush(VMWriter.Segment.CONST, 0);
        vmw.writeReturn();
        jt.advance(); // ;
    }

    public void compileExpression() {
        compileTerm();
        String ops = "+-*/&|<>=";
        while (jt.hasMoreTokens() && ops.contains(jt.peek())) {
            jt.advance(); String op = jt.getToken();
            compileTerm();
            if (op.equals("+")) vmw.writeArithmetic(VMWriter.Command.ADD);
            else if (op.equals("-")) vmw.writeArithmetic(VMWriter.Command.SUB);
            else if (op.equals("*")) vmw.writeCall("Math.multiply", 2);
            else if (op.equals("/")) vmw.writeCall("Math.divide", 2);
            else if (op.equals("&")) vmw.writeArithmetic(VMWriter.Command.AND);
            else if (op.equals("|")) vmw.writeArithmetic(VMWriter.Command.OR);
            else if (op.equals("<")) vmw.writeArithmetic(VMWriter.Command.LT);
            else if (op.equals(">")) vmw.writeArithmetic(VMWriter.Command.GT);
            else if (op.equals("=")) vmw.writeArithmetic(VMWriter.Command.EQ);
        }
    }

    public void compileTerm() {
        jt.advance();
        if (jt.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            vmw.writePush(VMWriter.Segment.CONST, Integer.parseInt(jt.getToken()));
        } else if (jt.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            String s = jt.getToken().substring(1, jt.getToken().length() - 1);
            vmw.writePush(VMWriter.Segment.CONST, s.length());
            vmw.writeCall("String.new", 1);
            for (char c : s.toCharArray()) {
                vmw.writePush(VMWriter.Segment.CONST, (int)c);
                vmw.writeCall("String.appendChar", 2);
            }
        } else if (jt.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            String k = jt.getToken();
            if (k.equals("this")) vmw.writePush(VMWriter.Segment.POINTER, 0);
            else if (k.equals("null") || k.equals("false")) vmw.writePush(VMWriter.Segment.CONST, 0);
            else if (k.equals("true")) { vmw.writePush(VMWriter.Segment.CONST, 1); vmw.writeArithmetic(VMWriter.Command.NEG); }
        } else if (jt.getToken().equals("(")) {
            compileExpression();
            jt.advance(); // )
        } else if (jt.getToken().equals("-") || jt.getToken().equals("~")) {
            String op = jt.getToken();
            compileTerm();
            vmw.writeArithmetic(op.equals("-") ? VMWriter.Command.NEG : VMWriter.Command.NOT);
        } else {
            String name = jt.getToken();
            if (jt.peek().equals("[")) {
                pushIdentifier(name);
                jt.advance(); // [
                compileExpression();
                jt.advance(); // ]
                vmw.writeArithmetic(VMWriter.Command.ADD);
                vmw.writePop(VMWriter.Segment.POINTER, 1);
                vmw.writePush(VMWriter.Segment.THAT, 0);
            } else if (jt.peek().equals("(") || jt.peek().equals(".")) {
                int nArgs = 0; String funcName = name;
                if (jt.peek().equals(".")) {
                    jt.advance(); // .
                    jt.advance(); String subName = jt.getToken();
                    if (st.kindOf(name) != SymbolTable.Kind.NONE) {
                        pushIdentifier(name);
                        funcName = st.typeOf(name) + "." + subName;
                        nArgs = 1;
                    } else funcName = name + "." + subName;
                } else {
                    vmw.writePush(VMWriter.Segment.POINTER, 0);
                    funcName = className + "." + name;
                    nArgs = 1;
                }
                jt.advance(); // (
                nArgs += compileExpressionList();
                jt.advance(); // )
                vmw.writeCall(funcName, nArgs);
            } else pushIdentifier(name);
        }
    }

    public int compileExpressionList() {
        int count = 0;
        if (!jt.peek().equals(")")) {
            compileExpression();
            count++;
            while (jt.peek().equals(",")) {
                jt.advance(); // ,
                compileExpression();
                count++;
            }
        }
        return count;
    }

    private void pushIdentifier(String name) {
        SymbolTable.Kind k = st.kindOf(name); int i = st.indexOf(name);
        if (k == SymbolTable.Kind.VAR) vmw.writePush(VMWriter.Segment.LOCAL, i);
        else if (k == SymbolTable.Kind.ARG) vmw.writePush(VMWriter.Segment.ARG, i);
        else if (k == SymbolTable.Kind.STATIC) vmw.writePush(VMWriter.Segment.STATIC, i);
        else if (k == SymbolTable.Kind.FIELD) vmw.writePush(VMWriter.Segment.THIS, i);
    }

    private void popIdentifier(String name) {
        SymbolTable.Kind k = st.kindOf(name); int i = st.indexOf(name);
        if (k == SymbolTable.Kind.VAR) vmw.writePop(VMWriter.Segment.LOCAL, i);
        else if (k == SymbolTable.Kind.ARG) vmw.writePop(VMWriter.Segment.ARG, i);
        else if (k == SymbolTable.Kind.STATIC) vmw.writePop(VMWriter.Segment.STATIC, i);
        else if (k == SymbolTable.Kind.FIELD) vmw.writePop(VMWriter.Segment.THIS, i);
    }
}