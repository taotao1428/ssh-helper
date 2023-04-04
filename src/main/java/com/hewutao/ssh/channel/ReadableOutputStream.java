package com.hewutao.ssh.channel;

import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReadableOutputStream extends OutputStream {
    private final byte[] buf = new byte[1024 * 100];
    private int pos = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();

    @Override
    public void write(int b) {
        lock.lock();
        try {
            while (buf.length == pos) {
                notFull.awaitUninterruptibly();
            }

            buf[pos++] = (byte) b;
        } finally {
            lock.unlock();
        }
    }

    public byte[] read() {
        lock.lock();
        try {
            byte[] data = new byte[pos];
            System.arraycopy(buf, 0, data, 0, pos);
            pos = 0;
            notFull.signal();
            return data;
        } finally {
            lock.unlock();
        }
    }
}
