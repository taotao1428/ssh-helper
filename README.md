# ssh-helper
使用java实现解析ssh的输入输出，类似expect命令。

## expect格式
根据正则表示式匹配输出
```text
# 单行
expect <matcher> {
    <action1>
    <action2>
}

# 多行
expect {
    <matcher1> {
        <action1>
        <action2>
    }
    <matcher2> {
        <action1>
        <action2>
    }
}
```
支持3中matcher
1. 正则表达式。使用引号包裹正则表达式，例如: "(name|age)"
2. timeout。匹配超时。如果没有显示指定timeout执行的操作，超时时，将不会执行任何操作
3. eof。输出流关闭，表示ssh进程结束。

## 支持的命令

### set timeout
`set timeout <seconds>`: 设置expect的超时时间，默认超时时间为10s。

```text
set timeout 20
```

### send
`send <data>`：输入数据，data需要使用引号包裹

```text
send "sudo su -\n"
```

### exit
`exit [<exit_code>]` 表示终止ssh交互。

```text
exit
exit -1
```

### exp_continue
`exp_continue` 表示重试当前expect，超时会重新计算exp_continue

```text
exp_continue
```

### sleep
`sleep <seconds>` 睡眠

```text
sleep 5
```


## expect示例
```text
expect {
    timeout {
        exit -1
    }
    "parallels@" {
        send "sudo su -\n"
        expect "assword" {
            send "pwdpwdpwd\n"
        }
        exp_continue
    }
    "root@" {
        send "date\n"
    }
}
expect "root@" {}

send "exit\n"
expect "parallels@" {}

send "exit\n"
expect eof {}
```

## 代码示例

```java
    public void run() throws Exception {
        String data = "expect {\n" +
                "    timeout {\n" +
                "        exit -1\n" +
                "    }\n" +
                "    \"parallels@\" {\n" +
                "        send \"sudo su -\\n\"\n" +
                "        expect \"assword\" {\n" +
                "            send \"pwdpwdpwd\\n\"\n" +
                "        }\n" +
                "        exp_continue\n" +
                "    }\n" +
                "    \"root@\" {\n" +
                "        send \"date\\n\"\n" +
                "    }\n" +
                "}\n" +
                "expect \"root@\" {}\n" +
                "\n" +
                "send \"exit\\n\"\n" +
                "expect \"parallels@\" {}\n" +
                "\n" +
                "send \"exit\\n\"\n" +
                "expect eof {}";

        String user = "parallels";
        String host = "10.xxx.xxx.xxx";
        String password = "pwdpwdpwd";

        // 1. 解析expect的字符串
        Tokenizer tokenizer = new Tokenizer(data);
        TokenReader reader = new TokenReader(tokenizer);
        Parser parser = new Parser();
        Action action = parser.parse(reader);

        // 2. 创建sshChannel
        DefaultSshChannel channel = new DefaultSshChannel(host, user, password);
        channel.init();

        // 3. 与ssh进行交互
        Environment env = new Environment();
        action.init(env);
        action.exec(channel);
        channel.close();

        // 4. 输出交互过程中输出
        System.out.println(channel.allReceive());
    }
```