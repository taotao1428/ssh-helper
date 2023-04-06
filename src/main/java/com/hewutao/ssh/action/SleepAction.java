package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;
import com.hewutao.ssh.utils.ThreadUtils;

public class SleepAction implements Action {
    private final int seconds;

    public SleepAction(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void init(Environment env) {

    }

    @Override
    public void exec(SshChannel client) throws Exception {
        ThreadUtils.sleep(seconds * 1000L);
    }
}
