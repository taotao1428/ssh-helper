package com.hewutao.ssh.parse;

import com.hewutao.ssh.parse.token.PosDetail;
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
    private final PosDetail curPosDetail = new PosDetail(1, 1, 0);
    public Tokenizer(String data) {
        this.data = data;
    }

    public Token next() {
        skipBlank();
        int pos = curPosDetail.getPos();
        if (pos >= data.length()) {
            return new Token("", "", curPosDetail.copy(), curPosDetail.copy(), TokenType.EOF);
        }

        PosDetail startPosDetail = curPosDetail.copy();
        char ch = data.charAt(pos);
        switch (ch) {
            case '"':
            case '\'':
                return readString();
            case '{':
                curPosDetail.incr();
                return new Token("{", "{", startPosDetail, curPosDetail.copy(), TokenType.LEFT_BRACE);
            case '}':
                curPosDetail.incr();
                return new Token("}", "{", startPosDetail, curPosDetail.copy(), TokenType.RIGHT_BRACE);
            case '#':
                return readComment();
            case '\r':
            case '\n':
                return readNewLine();
            default:
                return readKeyword();
        }
    }

    private Token readString() {
        PosDetail startPosDetail = curPosDetail.copy();
        int pos = startPosDetail.getPos();
        char startCh = data.charAt(pos);
        curPosDetail.incr();
        boolean escaped = false;
        char ch;

        StringBuilder sb = new StringBuilder();
        while ((pos = curPosDetail.getPos()) < data.length()) {
            ch = data.charAt(pos);
            if (escaped) {
                Character realCh = escapeMap.get(ch);
                if (realCh == null) {
                    throw new IllegalStateException("illegal escape to [" + ch + "], " + curPosDetail.toPosString());
                }
                sb.append(realCh);
                escaped = false;
            } else {
                if (ch == startCh) {
                    curPosDetail.incr();
                    return new Token(
                            sb.toString(),
                            data.substring(startPosDetail.getPos(), curPosDetail.getPos()),
                            startPosDetail,
                            curPosDetail.copy(),
                            TokenType.STRING);
                } else if (ch == '\\') {
                    escaped = true;
                } else {
                    sb.append(ch);
                }
            }
            curPosDetail.incr();
        }

        throw new IllegalStateException("illegal end of string [" + sb + "], " + startPosDetail.toPosString());
    }

    private Token readComment() {
        PosDetail startPosDetail = curPosDetail.copy();
        int pos;
        while ((pos = curPosDetail.getPos()) < data.length() && !isNewLine(data.charAt(pos))) {
            curPosDetail.incr();
        }

        return new Token(
                data.substring(startPosDetail.getPos(), pos),
                data.substring(startPosDetail.getPos(), pos),
                startPosDetail,
                curPosDetail.copy(),
                TokenType.COMMENT);
    }

    private Token readKeyword() {
        PosDetail startPosDetail = curPosDetail.copy();
        int pos;
        while ((pos = curPosDetail.getPos()) < data.length() && !isBlankOrNewLine(data.charAt(pos))) {
            curPosDetail.incr();
        }

        return new Token(
                data.substring(startPosDetail.getPos(), pos),
                data.substring(startPosDetail.getPos(), pos),
                startPosDetail,
                curPosDetail.copy(),
                TokenType.KEYWORD);
    }

    private Token readNewLine() {
        PosDetail startPosDetail = curPosDetail.copy();

        int pos = startPosDetail.getPos();
        char ch = data.charAt(pos);
        if (ch == '\r') {
            int nextPos = pos + 1;
            if (nextPos == data.length()) {
                throw new IllegalStateException("last char is \\r, " + startPosDetail.toPosString());
            }
            char nextCh = data.charAt(nextPos);
            if (nextCh != '\n') {
                throw new IllegalStateException("\\r is existed without \\n, " + startPosDetail.toPosString());
            }
            curPosDetail.line(2);
        } else if (ch == '\n') {
            curPosDetail.line(1);
        }

        return new Token(
                data.substring(startPosDetail.getPos(), curPosDetail.getPos()),
                data.substring(startPosDetail.getPos(), curPosDetail.getPos()),
                startPosDetail, curPosDetail.copy(), TokenType.NEW_LINE);
    }

    private void skipBlank() {
        int pos;
        while ((pos = curPosDetail.getPos()) < data.length() && isBlank(data.charAt(pos))) {
            curPosDetail.incr();
        }
    }

    private static boolean isBlank(char ch) {
        return ch == ' ' || ch == '\t';
    }

    private static boolean isBlankOrNewLine(char ch) {
        return ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t';
    }

    private static boolean isNewLine(char ch) {
        return ch == '\r' || ch == '\n';
    }

}
