package com.hewutao.ssh.action;

import com.hewutao.ssh.action.matcher.TimeoutMatcher;
import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;
import com.hewutao.ssh.action.matcher.Matcher;
import com.hewutao.ssh.utils.ThreadUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpectAction implements Action {
    private final LinkedHashMap<Matcher, Action> cases;
    private Environment env;

    public ExpectAction(LinkedHashMap<Matcher, Action> cases) {
        this.cases = cases;
    }

    @Override
    public void init(Environment env) {
        this.env = env;

        boolean containTimeoutCase = false;

        for (Matcher matcher : cases.keySet()) {
            if (matcher instanceof TimeoutMatcher) {
                containTimeoutCase = true;
            }
            matcher.init(env);
        }

        if (!containTimeoutCase) {
            TimeoutMatcher matcher = new TimeoutMatcher();
            cases.put(matcher, new NoneAction());
            matcher.init(env);
        }
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        while (true) {
            String data = client.receive();
            for (Map.Entry<Matcher, Action> entry : cases.entrySet()) {
                Matcher matcher = entry.getKey();
                Action action = entry.getValue();
                if (matcher.match(data)) {
                    action.init(env);
                    action.exec(client);
                    client.clear();
                    return;
                }
            }
            ThreadUtils.sleep(100);
        }
    }

    public static ExpectActionBuilder builder() {
        return new ExpectActionBuilder();
    }

    public static class ExpectActionBuilder {
        private final LinkedHashMap<Matcher, Action> cases = new LinkedHashMap<>();

        public ExpectActionBuilder add(Matcher matcher, Action action) {
            this.cases.put(matcher, action);
            return this;
        }

        public boolean containTimeoutCase() {
            for (Matcher matcher : cases.keySet()) {
                if (matcher instanceof TimeoutMatcher) {
                    return true;
                }
            }
            return false;
        }

        public ExpectAction build() {
            return new ExpectAction(cases);
        }
    }
}
