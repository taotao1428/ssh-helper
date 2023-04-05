package com.hewutao.ssh.parse;

import com.hewutao.ssh.parse.token.Token;
import com.hewutao.ssh.parse.token.TokenType;

import java.util.LinkedList;

public class TokenReader {
    private final Tokenizer tokenizer;
    private final LinkedList<Token> tokenBuf = new LinkedList<>();
    private boolean hasNewLine = false;

    public TokenReader(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private Token nextToken() {
        Token token;
        // 过滤评论和多个换行
        while ((token = tokenizer.next()).getType() == TokenType.COMMENT
                || (hasNewLine && token.getType() == TokenType.NEW_LINE)) {
        }

        hasNewLine = token.getType() == TokenType.NEW_LINE;
        return token;
    }

    public Token peek() {
        return peek(1);
    }

    public Token peek(int i) {
        while (tokenBuf.size() < i) {
            tokenBuf.addLast(nextToken());
        }
        return tokenBuf.get(i - 1);
    }

    public Token expectPeek(TokenType type) {
        return expectPeek(1, type);
    }

    public Token expectKeyWordPeek(String value) {
        return expectKeyWordPeek(1, value);
    }

    public Token expectPeek(int i, TokenType type) {
        Token token = peek(i);
        check(token, type);
        return token;
    }

    public Token expectKeyWordPeek(int i, String value) {
        Token token = peek(i);
        check(token, TokenType.KEYWORD);
        checkKeywordValue(token, value);
        return token;
    }

    public Token next() {
        if (!tokenBuf.isEmpty()) {
            return tokenBuf.removeFirst();
        }
        return nextToken();
    }

    public Token expectNext(TokenType type) {
        Token token = next();
        check(token, type);
        return token;
    }

    public Token expectNextKeyword(String value) {
        Token token = next();
        check(token, TokenType.KEYWORD);
        checkKeywordValue(token, value);
        return token;
    }

    public void check(Token token, TokenType type) {
        if (token.getType() != type) {
            throw new IllegalStateException("expect type [" + type + "], but is [" + token.getType() + "], " + token.getStartPos().toPosString());
        }
    }

    public void checkKeywordValue(Token token, String value) {
        if (!token.getValue().equals(value)) {
            throw new IllegalStateException("expect keyword [" + value + "], but is [" + token.getValue() + "], " + token.getStartPos().toPosString());
        }
    }
}
