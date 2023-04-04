package com.hewutao.ssh.utils;

import java.util.concurrent.TimeUnit;

public abstract class ThreadUtils {
    public static void sleep(long mills) {
        long endTime = System.currentTimeMillis() + mills;
        long diff ;
        while ((diff = endTime - System.currentTimeMillis()) > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(diff);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
