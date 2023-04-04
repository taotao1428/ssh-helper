package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

public class SendAction implements Action {
    private final String data;

    public SendAction(String data) {
        this.data = data;
    }

    @Override
    public void init(Environment env) {
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        client.send(data);
    }
}
