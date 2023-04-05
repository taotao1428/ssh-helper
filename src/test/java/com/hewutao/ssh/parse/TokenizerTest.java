package com.hewutao.ssh.parse;

import com.hewutao.ssh.channel.DefaultSshChannel;
import com.hewutao.ssh.channel.Environment;
import com.hewutao.ssh.action.Action;
import com.hewutao.ssh.parse.token.Token;
import com.hewutao.ssh.parse.token.TokenType;
import org.junit.Test;

public class TokenizerTest {

    @Test
    public void next() {
        String data = "#/user/bin/expect\n" +
                "spawn scp -P 22 -r root@192.128.75.128:/etc/* /var/jenkins_home/\n" +
                "set timeout 20\n" +
                "expect {\n" +
                "    \"(yes/no)?\"{\n" +
                "        send \"yes\\n\"\n" +
                "        expect \"*password:\"{\n" +
                "            send \"admin@123\\n\"\n" +
                "\t    }\n" +
                "    }\n" +
                "    \"*password:\"{\n" +
                "        send \"admin@123\\n\"\n" +
                "    }\n" +
                "}\n" +
                "expect \"100%\"\n\n\n" +
                "expect eof";

        Tokenizer tokenizer = new Tokenizer(data);
        Token token;
        while ((token = tokenizer.next()).getType() != TokenType.EOF) {
            System.out.println(token);
        }
    }

    @Test
    public void parse() throws Exception {
        String user = "parallels";
        String host = "10.211.55.14";
        String password = "hewutao12#$%";
        String sudoPwd = password;

        String data = "expect {\n" +
                "    timeout {\n" +
                "        exit -1\n" +
                "    }\n" +
                "    \"parallels@\" {\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "send \"sudo su -\\n\"\n" +
                "\n" +
                "expect {\n" +
                "    timeout {\n" +
                "        exit -1\n" +
                "    }\n" +
                "    \n" +
                "    \"assword\" {\n" +
                "        send \"hewutao12#$%\\n\"\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "expect \"root@\" {\n" +
                "    send \"whoami\\n\"\n" +
                "}\n" +
                "\n" +
                "expect \n{\n" +
                "    timeout {\n" +
                "        exit -1\n" +
                "    }\n" +
                "    \"root@\" {\n" +
                "    \n" +
                "    }\n" +
                "}\n" +
                "\n";

        Tokenizer tokenizer = new Tokenizer(data);
        TokenReader reader = new TokenReader(tokenizer);

        Parser parser = new Parser();

        Action action = parser.parse(reader);

        DefaultSshChannel channel = new DefaultSshChannel(host, user, password);
        channel.init();

        Environment env = new Environment();
        action.init(env);
        action.exec(channel);

        channel.close();

        System.out.println(channel.allReceive());
    }
}