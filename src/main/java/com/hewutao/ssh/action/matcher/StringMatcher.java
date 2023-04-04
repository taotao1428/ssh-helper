package com.hewutao.ssh.action.matcher;

import com.hewutao.ssh.channel.Environment;

public class StringMatcher implements Matcher {
    private final String keyword;

    public StringMatcher(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public void init(Environment env) {

    }

    @Override
    public boolean match(String data) {
        return data.contains(keyword);
    }
}
