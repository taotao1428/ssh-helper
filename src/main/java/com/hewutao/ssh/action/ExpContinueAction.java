package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

public class ExpContinueAction implements Action {
    private Environment env;

    @Override
    public void init(Environment env) {
        this.env = env;
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        env.setExpContinue(true);
    }
}
