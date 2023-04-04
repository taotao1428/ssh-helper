package com.hewutao.ssh.channel;

public interface SshChannel {
    void send(String data) throws Exception;
    String receive() throws Exception;
    String allReceive() throws Exception;
    void clear() throws Exception;
    void close() throws Exception;
}
