package com.hewutao.ssh.parse.token;

public class PosDetail {
    private int line;
    private int column;
    private int pos;

    public PosDetail(int line, int column, int pos) {
        this.line = line;
        this.column = column;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public PosDetail copy() {
        return new PosDetail(line, column, pos);
    }

    public int incr() {
        return incr(1);
    }

    public int incr(int i) {
        column += i;
        pos += i;
        return pos;
    }

    public int line(int i) {
        column = 1;
        line += 1;
        pos += i;

        return pos;
    }


    public static PosDetail of(int line, int column, int pos) {
        return new PosDetail(line, column, pos);
    }

    @Override
    public String toString() {
        return "PosDetail{" +
                "line=" + line +
                ", column=" + column +
                ", pos=" + pos +
                '}';
    }

    public String toPosString() {
        return "line: "+ line + ", column: " + column;
    }
}
