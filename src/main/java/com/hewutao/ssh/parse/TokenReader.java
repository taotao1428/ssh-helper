package com.hewutao.ssh.parse;

import com.hewutao.ssh.parse.token.Token;
import com.hewutao.ssh.parse.token.TokenType;

import java.util.LinkedList;

public class TokenReader {
    private final Tokenizer tokenizer;
    private final LinkedList<Token> tokenBuf = new LinkedList<>();

    public TokenReader(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private Token nextNoComment() {
        Token token;
        while ((token = tokenizer.next()).getType() == TokenType.COMMENT) {
        }
        return token;
    }

    public Token peek() {
        return peek(1);
    }

    public Token peek(int i) {
        while (tokenBuf.size() < i) {
            tokenBuf.addLast(nextNoComment());
        }
        return tokenBuf.get(i - 1);
    }

    public Token expectPeek(TokenType type) {
        return expectPeek(1, type);
    }

    public Token expectPeek(TokenType type, String value) {
        return expectPeek(1, type, value);
    }

    public Token expectPeek(int i, TokenType type) {
        Token token = peek(i);
        check(token, type);
        return token;
    }

    public Token expectPeek(int i, TokenType type, String value) {
        Token token = peek(i);
        check(token, type, value);
        return token;
    }


    public Token next() {
        if (!tokenBuf.isEmpty()) {
            return tokenBuf.removeFirst();
        }
        return nextNoComment();
    }

    public Token expectNext(TokenType type) {
        Token token = next();
        check(token, type);
        return token;
    }

    public Token expectNext(TokenType type, String value) {
        Token token = next();
        check(token, type, value);
        return token;
    }

    private void check(Token token, TokenType type) {
        if (token.getType() != type) {
            throw new IllegalStateException();
        }
    }

    private void check(Token token, TokenType type, String value) {
        check(token, type);
        if (token.getValue().equals(value)) {
            throw new IllegalStateException();
        }
    }
}
