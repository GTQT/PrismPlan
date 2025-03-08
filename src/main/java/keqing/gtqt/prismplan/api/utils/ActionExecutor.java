package keqing.gtqt.prismplan.api.utils;


import io.netty.util.internal.ThrowableUtil;

import javax.swing.*;

public class ActionExecutor implements Runnable, Comparable<ActionExecutor> {
    public final Action action;
    public final int priority;
    public volatile boolean isCompleted;
    public volatile int usedTime;

    public ActionExecutor(Action action) {
        this(action, 0);
    }

    public ActionExecutor(Action action, int priority) {
        this.isCompleted = false;
        this.usedTime = 0;
        this.action = action;
        this.priority = priority;
    }

    public void run() {
        long start = System.nanoTime() / 1000L;

        this.usedTime = (int)(System.nanoTime() / 1000L - start);
        this.isCompleted = true;
    }

    public int compareTo(ActionExecutor o) {
        return o.priority - this.priority;
    }
}
