package com.hewutao.ssh.channel;

import java.util.Deque;
import java.util.LinkedList;

public class Environment {
    private int timeout = 3;

    private boolean exited;
    private int exitCode;

    private final Deque<SubEnv> subEnvDeque = new LinkedList<>();

    public boolean isExited() {
        return exited;
    }

    public void setExited(boolean exited) {
        this.exited = exited;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void pushSubEnv() {
        subEnvDeque.push(new SubEnv());
    }

    public SubEnv popSubEnv() {
        return subEnvDeque.pop();
    }

    public void setExpectContinue(boolean expectContinue) {
        SubEnv subEnv = subEnvDeque.peek();
        subEnv.expectContinue = expectContinue;
    }

    public boolean getExpectContinue() {
        SubEnv subEnv = subEnvDeque.peek();
        return subEnv.expectContinue;
    }


    public static class SubEnv {
        private boolean expectContinue;
    }
}
