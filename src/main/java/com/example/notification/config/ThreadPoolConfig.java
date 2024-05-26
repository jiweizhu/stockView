package com.example.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // 允许使用异步方法
public class ThreadPoolConfig {

    @Bean("executorService")
    public ThreadPoolTaskExecutor executorService() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        // 设置最大线程数
        threadPoolTaskExecutor.setMaxPoolSize(30);
        // 设置工作队列大小
        threadPoolTaskExecutor.setQueueCapacity(100);
        //设置空闲线程死亡时间
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        // 设置拒绝策略.当工作队列已满,线程数为最大线程数的时候,接收新任务抛出RejectedExecutionException异常
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        // 初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}