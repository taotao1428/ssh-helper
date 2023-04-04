package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

public class SetTimeoutAction implements Action {
    private final int timeout;
    private Environment env;

    public SetTimeoutAction(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void init(Environment env) {
        this.env = env;
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        env.setTimeout(timeout);
    }
}
