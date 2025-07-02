package com.statusneo.vms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for asynchronous operations in the Visitor Management System.
 * Sets up thread pools and executors for handling concurrent tasks like email sending.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * Creates a thread pool executor specifically for email-related tasks.
     * Configuration:
     * - Core pool size: 5 threads (minimum number of threads)
     * - Max pool size: 10 threads (maximum number of threads)
     * - Queue capacity: 100 tasks (tasks waiting to be executed)
     * - Thread name prefix: "EmailAsync-" (for easy identification in logs)
     *
     * @return Configured thread pool executor for email tasks
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Set minimum number of threads that will be maintained
        executor.setCorePoolSize(5);
        // Set maximum number of threads that can be created
        executor.setMaxPoolSize(10);
        // Set maximum number of tasks that can be queued
        executor.setQueueCapacity(100);
        // Set thread name prefix for logging purposes
        executor.setThreadNamePrefix("EmailAsync-");
        // Initialize the executor
        executor.initialize();
        return executor;
    }
}