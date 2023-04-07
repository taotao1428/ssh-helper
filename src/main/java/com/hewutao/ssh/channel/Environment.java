package com.hewutao.ssh.channel;

public class Environment {
    private int timeout = 10;

    private boolean exited;
    private int exitCode;

    private boolean expContinue;

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

    public void setExpContinue(boolean expContinue) {
        this.expContinue = expContinue;
    }

    public boolean getExpContinueAndClear() {
        boolean val = expContinue;
        expContinue = false;
        return val;
    }
}
