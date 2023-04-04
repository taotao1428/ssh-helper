package com.hewutao.ssh.parse;

import com.hewutao.ssh.parse.token.Token;
import com.hewutao.ssh.parse.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Tokenizer {
    private static final Map<Character, Character> escapeMap = new HashMap<>();
    static {
        escapeMap.put('t', '\t');
        escapeMap.put('r', '\r');
        escapeMap.put('n', '\n');
        escapeMap.put('\\', '\\');
    }

    private final String data;
    private int pos = 0;
    public Tokenizer(String data) {
        this.data = data;
    }

    public Token next() {
        skipBlank();
        if (pos >= data.length()) {
            return new Token("", pos, pos, TokenType.EOF);
        }
        int startPos = pos;

        char ch = data.charAt(pos);
        switch (ch) {
            case '"':
            case '\'': return readString();
            case '{': return new Token("{", startPos, ++pos, TokenType.LEFT_BRACE);
            case '}': return new Token("}", startPos, ++pos, TokenType.RIGHT_BRACE);
            case '#': return readComment();
            case '\r':
            case '\n': return readNewLine();
            default: return readKeyword();
        }
    }

    private Token readString() {
        int startPos = pos;
        char startCh = data.charAt(startPos);
        pos++;
        boolean escaped = false;
        char ch;

        StringBuilder sb = new StringBuilder();
        while (pos < data.length()) {
            ch = data.charAt(pos++);
            if (escaped) {
                Character realCh = escapeMap.get(ch);
                if (realCh == null) {
                    throw new IllegalStateException("illegal escape to [" + ch + "]");
                }
                sb.append(realCh);
                escaped = false;
            } else {
                if (ch == startCh) {
                    return new Token(sb.toString(), startPos, pos, TokenType.STRING);
                } else if (ch == '\\') {
                    escaped = true;
                } else {
                    sb.append(ch);
                }
            }
        }

        throw new IllegalStateException("illegal end of token");
    }

    private Token readComment() {
        int startPos = pos;
        while (pos < data.length() && !isNewLine(data.charAt(pos))) {
            pos++;
        }

        return new Token(data.substring(startPos, pos), startPos, pos, TokenType.COMMENT);
    }

    private Token readKeyword() {
        int startPos = pos;
        while (pos < data.length() && !isBlank(data.charAt(pos))) {
            pos++;
        }

        return new Token(data.substring(startPos, pos), startPos, pos, TokenType.KEYWORD);
    }

    private Token readNewLine() {
        int startPos = pos;
        while (pos < data.length() && isNewLine(data.charAt(pos))) {
            pos++;
        }

        return new Token(data.substring(startPos, pos), startPos, pos, TokenType.NEW_LINE);
    }

    private void skipBlank() {
        while (pos < data.length() && isBlank2(data.charAt(pos))) {
            pos++;
        }
    }

    private static boolean isBlank2(char ch) {
        return ch == ' ' || ch == '\t';
    }

    private static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t';
    }

    private static boolean isNewLine(char ch) {
        return ch == '\r' || ch == '\n';
    }

}
