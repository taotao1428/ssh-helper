package com.hewutao.ssh.action;

import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.channel.SshChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BatchAction implements Action {
    private final List<Action> actions;

    private Environment env;

    public BatchAction(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public void init(Environment env) {
        this.env = env;
    }

    @Override
    public void exec(SshChannel client) throws Exception {
        for (Action action : actions) {
            action.init(env);
            action.exec(client);
            if (env.isExited()) {
                break;
            }
        }
    }

    public static BatchActionBuilder builder() {
        return new BatchActionBuilder();
    }

    public static class BatchActionBuilder {
        private final List<Action> actions = new ArrayList<>();

        public BatchActionBuilder add(Action action) {
            this.actions.add(action);
            return this;
        }

        public BatchActionBuilder addAll(List<Action> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public BatchActionBuilder addAll(Action ...actions) {
            this.actions.addAll(Arrays.asList(actions));
            return this;
        }

        public BatchAction build() {
            return new BatchAction(actions);
        }
    }
}
