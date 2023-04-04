package com.hewutao.ssh.parse;

import com.hewutao.ssh.action.Action;
import com.hewutao.ssh.action.BatchAction;
import com.hewutao.ssh.action.ExitAction;
import com.hewutao.ssh.action.ExpectAction;
import com.hewutao.ssh.action.NoneAction;
import com.hewutao.ssh.action.SendAction;
import com.hewutao.ssh.action.SetTimeoutAction;
import com.hewutao.ssh.action.matcher.Matcher;
import com.hewutao.ssh.action.matcher.RegexMatcher;
import com.hewutao.ssh.action.matcher.TimeoutMatcher;
import com.hewutao.ssh.parse.token.Token;
import com.hewutao.ssh.parse.token.TokenType;

public class Parser {
    public Action parse(TokenReader reader) {
        return parseBatch(reader, TokenType.EOF);
    }

    private BatchAction parseBatch(TokenReader reader) {
        return parseBatch(reader, TokenType.RIGHT_BRACE);
    }

    private BatchAction parseBatch(TokenReader reader, TokenType endType) {
        BatchAction.BatchActionBuilder builder = BatchAction.builder();
        Token next;
        while ((next = reader.next()).getType() != endType) {
            if (next.getType() == TokenType.NEW_LINE) {
                continue;
            }
            if (next.getType() != TokenType.KEYWORD) {
                throw new IllegalStateException();
            }
            switch (next.getValue()) {
                case "set":
                    builder.add(parseSetTimeout(reader)); break;
                case "send":
                    builder.add(parseSend(reader)); break;
                case "expect":
                    builder.add(parseExpect(reader)); break;
                case "exit":
                    builder.add(parseExit(reader)); break;
                default:
                    throw new IllegalStateException();
            }
        }

        return builder.build();
    }

    private SetTimeoutAction parseSetTimeout(TokenReader reader) {
        reader.expectNext(TokenType.KEYWORD, "timeout");

        Token next2 = reader.expectNext(TokenType.KEYWORD);
        try {
            int value = Integer.parseInt(next2.getValue());
            reader.expectNext(TokenType.NEW_LINE);
            return new SetTimeoutAction(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(e);
        }
    }

    private ExpectAction parseExpect(TokenReader reader) {
        Token next = reader.next();
        switch (next.getType()) {
            case STRING:
                return parseOneExpect(reader, next);
            case LEFT_BRACE:
                return parseMultiExpect(reader, next);
            default:
                throw new IllegalStateException();
        }
    }

    private ExpectAction parseMultiExpect(TokenReader reader, Token next) {
        ExpectAction.ExpectActionBuilder builder = ExpectAction.builder();
        Token next2;
        while ((next2 = reader.next()).getType() != TokenType.RIGHT_BRACE) {
            if (next2.getType() == TokenType.NEW_LINE) {
                continue;
            }
            Matcher matcher;
            switch (next2.getType()) {
                case STRING:
                    matcher = new RegexMatcher(next2.getValue()); break;
                case KEYWORD:
                    if (!next2.getValue().equals("timeout")) {
                        throw new IllegalStateException();
                    }
                    matcher = new TimeoutMatcher(); break;
                default:
                    throw new IllegalStateException();
            }
            reader.expectNext(TokenType.LEFT_BRACE);
            Action action = parseBatch(reader);
            builder.add(matcher, action);
        }

        return builder.build();
    }

    private ExpectAction parseOneExpect(TokenReader reader, Token next) {
        Token next2 = reader.next();
        Action action;
        switch (next2.getType()) {
            case NEW_LINE:
                action = new NoneAction();
                break;
            case LEFT_BRACE:
                action = parseBatch(reader);
                break;
            default:
                throw new IllegalStateException();
        }
        return ExpectAction.builder()
                .add(new RegexMatcher(next.getValue()), action)
                .add(new TimeoutMatcher(), new NoneAction())
                .build();
    }

    private SendAction parseSend(TokenReader reader) {
        Token next = reader.expectNext(TokenType.STRING);
        reader.expectNext(TokenType.NEW_LINE);
        return new SendAction(next.getValue());
    }

    private ExitAction parseExit(TokenReader reader) {
        Token next = reader.next();
        int exitCode = 0;
        if (next.getType() != TokenType.NEW_LINE) {
            if (next.getType() != TokenType.KEYWORD) {
                throw new IllegalStateException();
            }
            try {
                exitCode = Integer.parseInt(next.getValue());
            } catch (NumberFormatException e) {
                throw new IllegalStateException(e);
            }
            reader.expectNext(TokenType.NEW_LINE);
        }
        return new ExitAction(exitCode);
    }
}
