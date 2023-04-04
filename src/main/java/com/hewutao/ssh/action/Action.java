package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

public interface Action {
    void init(Environment env);
    void exec(SshChannel client) throws Exception;
}
