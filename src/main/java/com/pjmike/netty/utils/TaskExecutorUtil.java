package com.pjmike.netty.utils;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TaskExecutorUtil {

    private static ThreadPoolTaskExecutor taskExecutor = null;

    public static ThreadPoolTaskExecutor getTaskExecutor() {
        if (taskExecutor == null) {
            taskExecutor = new ThreadPoolTaskExecutor();
            //线程池所使用的缓冲队列
            taskExecutor.setQueueCapacity(32);
            //线程池维护线程的最少数量
            taskExecutor.setCorePoolSize(8);
            //线程池维护线程的最大数量
            taskExecutor.setMaxPoolSize(16);
            taskExecutor.initialize();
        }
        return taskExecutor;
    }

}
