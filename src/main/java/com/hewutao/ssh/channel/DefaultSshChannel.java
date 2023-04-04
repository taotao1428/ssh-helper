package com.hewutao.ssh.channel;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

public class DefaultSshChannel implements SshChannel {
    private final static int DEFAULT_SSH_PORT = 22;
    private final String host;
    private final String user;
    private final String password;
    private final int port;

    private SshClient client;
    private ClientSession clientSession;
    private ChannelShell channelShell;

    private ReadableOutputStream byteOut = new ReadableOutputStream();
    private ReadableOutputStream byteErr = new ReadableOutputStream();
    private PipedOutputStream out;
    private PipedInputStream in;

    private String allReceived = "";
    private String received = "";

    public DefaultSshChannel(String host, String user, String password, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
    }

    public DefaultSshChannel(String host, String user, String password) {
        this(host, user, password, DEFAULT_SSH_PORT);
    }

    public void init() throws Exception {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        client.addPasswordIdentity(password);
        ConnectFuture connectFuture = client.connect(user, host, port);
        connectFuture.verify();

        ClientSession clientSession = connectFuture.getClientSession();
        clientSession.auth().verify();

        ChannelShell channelShell = clientSession.createShellChannel();

        ReadableOutputStream byteOut = new ReadableOutputStream();
        ReadableOutputStream byteErr = new ReadableOutputStream();

        channelShell.setOut(byteOut);
        channelShell.setErr(byteErr);

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out);
        channelShell.setIn(in);

        OpenFuture openFuture = channelShell.open();
        openFuture.verify();

        this.client = client;
        this.clientSession = clientSession;
        this.channelShell = channelShell;

        this.byteOut = byteOut;
        this.byteErr = byteErr;
        this.out = out;
    }

    @Override
    public void send(String data) throws Exception {
        this.out.write(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String receive() throws Exception {
        String data = new String(byteOut.read(), StandardCharsets.UTF_8);
        received += data;
        allReceived += data;
        return received;
    }

    @Override
    public String allReceive() throws Exception {
        return allReceived;
    }

    @Override
    public void clear() throws Exception {
        received = "";
    }

    @Override
    public void close() throws Exception {
        this.channelShell.close();
        this.clientSession.close();
        this.client.close();
    }
}
