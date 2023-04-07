package com.hewutao.ssh;

import com.hewutao.ssh.action.BatchAction;
import com.hewutao.ssh.action.ExitAction;
import com.hewutao.ssh.action.ExpectAction;
import com.hewutao.ssh.action.NoneAction;
import com.hewutao.ssh.action.SendAction;
import com.hewutao.ssh.action.matcher.RegexMatcher;
import com.hewutao.ssh.action.matcher.StringMatcher;
import com.hewutao.ssh.action.matcher.TimeoutMatcher;
import com.hewutao.ssh.channel.DefaultSshChannel;
import com.hewutao.ssh.channel.Environment;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainTest {
    @Test
    public void testSsh() throws Exception {
        String user = "parallels";
        String host = "10.211.55.14";
        String password = "hewutao12#$%";
        int port = 22;

        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        client.addPasswordIdentity(password);
        ConnectFuture connectFuture = client.connect(user, host, port);
        connectFuture.verify();
        System.out.println(connectFuture.isConnected());

        ClientSession clientSession = connectFuture.getClientSession();
        AuthFuture authFuture = clientSession.auth().verify();
        System.out.println(authFuture.isSuccess());


        ChannelShell shellChannel = clientSession.createShellChannel();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        shellChannel.setOut(byteOut);
        shellChannel.setErr(System.err);
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out);

        shellChannel.setIn(in);

        OpenFuture openFuture = shellChannel.open();
        openFuture.verify();




        out.write("ls -al\n".getBytes(StandardCharsets.UTF_8));

        out.write("exit\n".getBytes(StandardCharsets.UTF_8));

        shellChannel.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 5000);

        System.out.print(new String(byteOut.toByteArray()));

        System.out.println(shellChannel.getExitStatus());

//        ChannelExec execChannel = session.createExecChannel("ls -al");
//        execChannel.setErr(System.err);
//        execChannel.setOut(System.out);
//        execChannel.setIn(null);
//
//        OpenFuture openFuture = execChannel.open();
//        openFuture.verify();
    }

    @Test
    public void testClient() throws Exception {
        String user = "parallels";
        String host = "10.211.55.14";
        String password = "hewutao12#$%";
        int port = 22;

        DefaultSshChannel channel = new DefaultSshChannel(host, user, password);
        channel.init();
        for (int i = 0; i < 10; i++) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>");
            System.out.println(channel.receive());

            TimeUnit.MILLISECONDS.sleep(500);
        }

        channel.clear();

        channel.send("ls -al\n");

        for (int i = 0; i < 10; i++) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>");
            System.out.println(channel.receive());

            TimeUnit.MILLISECONDS.sleep(500);
        }

        channel.close();
    }

    @Test
    public void testAction() throws Exception {
        String user = "parallels";
        String host = "10.211.55.14";
        String password = "hewutao12#$%";
        String sudoPwd = password;
        int port = 22;


        DefaultSshChannel channel = new DefaultSshChannel(host, user, password);
        channel.init();

        ExpectAction initExpectAction = ExpectAction.builder()
                .add(new TimeoutMatcher(), new ExitAction(-1))
                .add(new RegexMatcher("parallels@"), new NoneAction())
                .build();

        SendAction suAction = new SendAction("sudo su -\n");

        ExpectAction suExpectAction = ExpectAction.builder()
                .add(new TimeoutMatcher(), new ExitAction(-1))
                .add(new StringMatcher("assword"), new SendAction(sudoPwd + "\n"))
                .build();

        ExpectAction pwdExpectAction = ExpectAction.builder()
                .add(new TimeoutMatcher(), new ExitAction(-1))
                .add(new RegexMatcher("root@"), new SendAction("ls -al\n"))
                .build();

        ExpectAction lsExpectAction = ExpectAction.builder()
                .add(new TimeoutMatcher(), new ExitAction(-1))
                .add(new RegexMatcher("root@"), new NoneAction())
                .build();

        BatchAction batchAction = BatchAction.builder()
                .add(initExpectAction)
                .add(suAction)
                .add(suExpectAction)
                .add(pwdExpectAction)
                .add(lsExpectAction)
                .build();

        Environment env = new Environment();
        batchAction.init(env);
        batchAction.exec(channel);

        channel.close();

        System.out.println(channel.allReceive());
    }

    @Test
    public void test() {
        System.out.println(Arrays.toString(System.lineSeparator().getBytes(StandardCharsets.UTF_8)));
        System.out.println((int) '\n');
        System.out.println((int) '\r');
    }

    @Test
    public void test2() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));

        byte[] bytes = "hello world!哈哈".getBytes(StandardCharsets.UTF_8);

        out.write(bytes);

        out.close();

        char[] buf = new char[1024];
        int read = reader.read(buf);

        System.out.println(new String(buf, 0, read));


        read = reader.read(buf);

        System.out.println(new String(buf, 0, read));
//        out.flush();



    }
}
