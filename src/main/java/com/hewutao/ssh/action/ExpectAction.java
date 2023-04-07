package com.hewutao.ssh.action;

import com.hewutao.ssh.action.matcher.Matcher;
import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;
import com.hewutao.ssh.utils.ThreadUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpectAction implements Action {
    private final LinkedHashMap<Matcher, Action> cases;
    private Environment env;
    private final Action timeoutAction;
    private final Action eofAction;

    public ExpectAction(LinkedHashMap<Matcher, Action> cases, Action timeoutAction, Action eofAction) {
        this.cases = cases;
        this.timeoutAction = timeoutAction == null ? new NoneAction() : timeoutAction;
        this.eofAction = eofAction;
    }

    @Override
    public void init(Environment env) {
        this.env = env;
        for (Matcher matcher : cases.keySet()) {
            matcher.init(env);
        }
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        do {
            Action matchedAction = null;
            long endTime = System.currentTimeMillis() + env.getTimeout() * 1000L;
            while (true) {
                String data = client.receive();
                // 匹配结尾
                if (eofAction != null && data == null) {
                    matchedAction = eofAction;
                    break;
                }

                for (Map.Entry<Matcher, Action> entry : cases.entrySet()) {
                    Matcher matcher = entry.getKey();
                    Action action = entry.getValue();
                    if (matcher.match(data)) {
                        matchedAction = action;
                        break;
                    }
                }
                // 匹配超时
                if (matchedAction == null && System.currentTimeMillis() >= endTime) {
                    matchedAction = timeoutAction;
                }

                if (matchedAction != null) {
                    break;
                }

                ThreadUtils.sleep(100);
            }

            matchedAction.init(env);
            matchedAction.exec(client);
            client.clear();

        } while (env.getExpContinueAndClear());
    }

    public static ExpectActionBuilder builder() {
        return new ExpectActionBuilder();
    }

    public static class ExpectActionBuilder {
        private final LinkedHashMap<Matcher, Action> cases = new LinkedHashMap<>();
        private Action timeoutAction;
        private Action eofAction;

        public ExpectActionBuilder add(Matcher matcher, Action action) {
            this.cases.put(matcher, action);
            return this;
        }

        public ExpectActionBuilder setTimeoutAction(Action timeoutAction) {
            this.timeoutAction = timeoutAction;
            return this;
        }

        public ExpectActionBuilder setEofAction(Action eofAction) {
            this.eofAction = eofAction;
            return this;
        }

        public Action getEofAction() {
            return eofAction;
        }

        public Action getTimeoutAction() {
            return timeoutAction;
        }

        public ExpectAction build() {
            return new ExpectAction(cases, timeoutAction, eofAction);
        }
    }
}
