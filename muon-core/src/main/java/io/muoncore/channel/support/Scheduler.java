package io.muoncore.channel.support;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Used for the creation and management of timed events. Useful in the construction
 * of protocol processes that require time based interrupts, such as wait timeouts.
 */
public class Scheduler {

    private ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(10);

    public TimerControl executeIn(long value, TimeUnit unit, Runnable exec) {
        ScheduledFuture future = threadPool.schedule(exec, value, unit);

        return new TimerControl(future, exec);
    }

    public void shutdown() {
      threadPool.shutdown();
    }

    public static class TimerControl {
        private ScheduledFuture future;
        private Runnable exec;

        public TimerControl(ScheduledFuture future, Runnable exec) {
            this.future = future;
            this.exec = exec;
        }

        public void cancel() {
            future.cancel(true);
        }
    }
}
