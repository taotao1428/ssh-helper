package com.hewutao.ssh.parse;

import com.hewutao.ssh.action.Action;
import com.hewutao.ssh.action.BatchAction;
import com.hewutao.ssh.action.ExitAction;
import com.hewutao.ssh.action.ExpContinueAction;
import com.hewutao.ssh.action.ExpectAction;
import com.hewutao.ssh.action.SendAction;
import com.hewutao.ssh.action.SetTimeoutAction;
import com.hewutao.ssh.action.SleepAction;
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

            reader.check(next, TokenType.KEYWORD);
            switch (next.getValue()) {
                case "set":
                    builder.add(parseSetTimeout(reader)); break;
                case "send":
                    builder.add(parseSend(reader)); break;
                case "expect":
                    builder.add(parseExpect(reader)); break;
                case "sleep":
                    builder.add(parseSleep(reader)); break;
                case "exit":
                    builder.add(parseExit(reader)); break;
                case "exp_continue":
                    builder.add(parseExpContinue(reader)); break;
                default:
                    throw new IllegalStateException("unknown keyword [" + next.getValue() + "], " + next.getStartPos().toPosString());
            }
        }

        return builder.build();
    }

    private SetTimeoutAction parseSetTimeout(TokenReader reader) {
        reader.expectNextKeyword("timeout");

        Token next2 = reader.expectNext(TokenType.KEYWORD);
        try {
            int value = Integer.parseInt(next2.getValue());
            reader.expectNext(TokenType.NEW_LINE);
            return new SetTimeoutAction(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("expect integer, but is [" + next2.getValue() + "], " + next2.getStartPos().toPosString());
        }
    }

    private ExpContinueAction parseExpContinue(TokenReader reader) {
        reader.expectNext(TokenType.NEW_LINE);
        return new ExpContinueAction();
    }

    private SleepAction parseSleep(TokenReader reader) {
        Token next = reader.expectNext(TokenType.KEYWORD);
        try {
            int value = Integer.parseInt(next.getValue());
            reader.expectNext(TokenType.NEW_LINE);
            return new SleepAction(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("expect integer, but is [" + next.getValue() + "], " + next.getStartPos().toPosString());
        }
    }

    private ExpectAction parseExpect(TokenReader reader) {
        Token next = reader.next();

        switch (next.getType()) {
            case STRING:
                return parseOneExpect(reader, next);
            case LEFT_BRACE:
                return parseMultiExpect(reader, next);
            case NEW_LINE:
                reader.expectNext(TokenType.LEFT_BRACE);
                return parseMultiExpect(reader, next);
            default:
                throw new IllegalStateException("expect string or '{', but is [" + next.getRawValue() + "], " + next.getStartPos().toPosString());
        }
    }

    private ExpectAction parseMultiExpect(TokenReader reader, Token next) {
        ExpectAction.ExpectActionBuilder builder = ExpectAction.builder();
        Token next2;
        while ((next2 = reader.next()).getType() != TokenType.RIGHT_BRACE) {
            if (next2.getType() == TokenType.NEW_LINE) {
                continue;
            }
            parseExpectCase(reader, next2, builder);
        }

        return builder.build();
    }

    private ExpectAction parseOneExpect(TokenReader reader, Token next) {
        ExpectAction.ExpectActionBuilder builder = ExpectAction.builder();
        parseExpectCase(reader, next, builder);
        return builder.build();
    }

    private void parseExpectCase(TokenReader reader, Token next, ExpectAction.ExpectActionBuilder builder) {
        Matcher matcher;
        switch (next.getType()) {
            case STRING:
                matcher = new RegexMatcher(next.getValue()); break;
            case KEYWORD:
                reader.checkKeywordValue(next, "timeout");
                // 一个expect只能包含一个timeout
                if (builder.containTimeoutCase()) {
                    throw new IllegalStateException("multi timeout case, " + next.getStartPos().toPosString());
                }
                matcher = new TimeoutMatcher(); break;
            default:
                throw new IllegalStateException("expect string or keyword, but is [" + next.getRawValue() + "], " + next.getStartPos().toPosString());
        }
        reader.expectNext(TokenType.LEFT_BRACE);
        Action action = parseBatch(reader);

        builder.add(matcher, action);
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
            reader.check(next, TokenType.KEYWORD);
            try {
                exitCode = Integer.parseInt(next.getValue());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("expect integer, but is [" + next.getValue() + "], " + next.getStartPos().toPosString());
            }
            reader.expectNext(TokenType.NEW_LINE);
        }
        return new ExitAction(exitCode);
    }
}
