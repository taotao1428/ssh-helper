package com.hewutao.ssh.channel;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.locks.ReentrantLock;

public class ReadableOutputStream extends OutputStream {
    private final PipedInputStream pipedIn;
    private final PipedOutputStream pipedOut;
    private final BufferedReader reader;

    private final StringBuilder outputBuilder = new StringBuilder();
    private boolean finished = false;

    private final ReentrantLock lock = new ReentrantLock();

    private Exception readExp;

    public ReadableOutputStream() {
        pipedIn = new PipedInputStream();
        try {
            pipedOut = new PipedOutputStream(pipedIn);
        } catch (IOException e) {
            // cannot reach here!
            throw new IllegalStateException(e);
        }

        reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(pipedIn)));

        // 为了避免线程阻塞，开一个新线程读取数据
        new Thread(this::runRead).start();
    }

    private void runRead() {
        char[] buf = new char[1024];
        try {
            while (!finished) {
                int len = reader.read(buf);
                lock.lock();
                try {
                    if (len < 0) {
                        finished = true;
                    } else if (len > 0) {
                        outputBuilder.append(buf, 0, len);
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            readExp = e;
        }
    }

    @Override
    public void write(int b) throws IOException {
        pipedOut.write(b);
        pipedOut.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        pipedOut.write(b);
        pipedOut.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        pipedOut.write(b, off, len);
        pipedOut.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        pipedOut.close();
    }

    // 返回null表示读取结束
    public String readString() throws Exception {
        if (readExp != null) {
            throw readExp;
        }

        lock.lock();
        try {
            if (finished) {
                return null;
            }
            String output =  outputBuilder.toString();
            outputBuilder.setLength(0);
            return output;
        } finally {
            lock.unlock();
        }
    }
}
