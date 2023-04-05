package com.hewutao.ssh.parse.token;

public class Token {
    private final String value;
    private final String rawValue;
    private final PosDetail startPosDetail;
    private final PosDetail endPosDetail;
    private final TokenType type;

    public Token(String value, String rawValue, PosDetail startPosDetail, PosDetail endPosDetail, TokenType type) {
        this.value = value;
        this.rawValue = rawValue;
        this.startPosDetail = startPosDetail;
        this.endPosDetail = endPosDetail;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getRawValue() {
        return rawValue;
    }

    public PosDetail getStartPos() {
        return startPosDetail;
    }

    public PosDetail getEndPos() {
        return endPosDetail;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + value + '\'' +
                ", startPos=" + startPosDetail +
                ", endPos=" + endPosDetail +
                ", type=" + type +
                '}';
    }
}
