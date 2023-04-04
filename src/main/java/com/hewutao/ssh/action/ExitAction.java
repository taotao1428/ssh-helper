package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

public class ExitAction implements Action {
    private final int exitCode;
    private Environment env;

    public ExitAction(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public void init(Environment env) {
        this.env = env;
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        env.setExitCode(exitCode);
        env.setExited(true);
    }
}
