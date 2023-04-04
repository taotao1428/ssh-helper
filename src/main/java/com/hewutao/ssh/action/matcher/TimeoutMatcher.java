package com.hewutao.ssh.action.matcher;

import com.hewutao.ssh.channel.Environment;

public class TimeoutMatcher implements Matcher {
    private long endTime;
    @Override
    public void init(Environment env) {
        int timeout = env.getTimeout();
        endTime = System.currentTimeMillis() + timeout * 1000L;
    }

    @Override
    public boolean match(String data) {
        return System.currentTimeMillis() >= endTime;
    }
}
