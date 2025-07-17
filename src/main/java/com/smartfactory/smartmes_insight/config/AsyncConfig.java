package com.smartfactory.smartmes_insight.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * 알림 이벤트 처리를 위한 별도 스레드 풀 구성
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {
    
    /**
     * 알림 처리용 스레드 풀
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 🎯 스레드 풀 설정
        executor.setCorePoolSize(2);           // 기본 스레드 수
        executor.setMaxPoolSize(5);            // 최대 스레드 수
        executor.setQueueCapacity(100);        // 대기 큐 크기
        executor.setThreadNamePrefix("Notification-"); // 스레드 이름 접두사
        
        // 🛡️ 거부 정책 (큐가 가득 찬 경우)
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("🚨 Notification task rejected! Queue is full. Task: {}", runnable.toString());
        });
        
        // 🔄 종료 시 대기 설정
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        
        log.info("🚀 Notification executor initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
    
    /**
     * 기본 비동기 처리용 스레드 풀
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Async-");
        
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("🚨 Async task rejected! Queue is full. Task: {}", runnable.toString());
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        
        executor.initialize();
        
        log.info("🚀 Task executor initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
