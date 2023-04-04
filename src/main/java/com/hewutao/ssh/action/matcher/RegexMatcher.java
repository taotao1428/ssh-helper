package com.hewutao.ssh.action.matcher;

import com.hewutao.ssh.channel.Environment;

import java.util.regex.Pattern;

public class RegexMatcher implements Matcher {
    private final String pattern;
    public RegexMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void init(Environment env) {

    }

    @Override
    public boolean match(String data) {
        return Pattern.compile(pattern).matcher(data).find();
    }
}
