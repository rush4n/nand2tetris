import java.util.HashMap;

public class SymbolTable {
    public enum Kind { STATIC, FIELD, ARG, VAR, NONE }
    private class Symbol {
        String type; Kind kind; int index;
        Symbol(String t, Kind k, int i) { type = t; kind = k; index = i; }
    }

    private HashMap<String, Symbol> classScope = new HashMap<>();
    private HashMap<String, Symbol> subroutineScope = new HashMap<>();
    private HashMap<Kind, Integer> counts = new HashMap<>();

    public SymbolTable() {
        counts.put(Kind.STATIC, 0); counts.put(Kind.FIELD, 0);
        counts.put(Kind.ARG, 0); counts.put(Kind.VAR, 0);
    }

    public void startSubroutine() {
        subroutineScope.clear();
        counts.put(Kind.ARG, 0); counts.put(Kind.VAR, 0);
    }

    public void define(String name, String type, Kind kind) {
        int index = counts.get(kind);
        Symbol s = new Symbol(type, kind, index);
        if (kind == Kind.STATIC || kind == Kind.FIELD) classScope.put(name, s);
        else subroutineScope.put(name, s);
        counts.put(kind, index + 1);
    }

    public int varCount(Kind kind) { return counts.get(kind); }
    public Kind kindOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).kind;
        if (classScope.containsKey(name)) return classScope.get(name).kind;
        return Kind.NONE;
    }
    public String typeOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).type;
        return classScope.get(name).type;
    }
    public int indexOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).index;
        return classScope.get(name).index;
    }
}