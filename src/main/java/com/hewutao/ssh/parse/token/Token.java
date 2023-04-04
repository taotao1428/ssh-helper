package com.hewutao.ssh.parse.token;

public class Token {
    private final String value;
    private final int startPos;
    private final int endPos;
    private final TokenType type;

    public Token(String value, int startPos, int endPos, TokenType type) {
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", type=" + type +
                '}';
    }
}
