package com.hewutao.ssh.action.matcher;

import com.hewutao.ssh.channel.Environment;

public interface Matcher {
    void init(Environment env);
    boolean match(String data);
}
