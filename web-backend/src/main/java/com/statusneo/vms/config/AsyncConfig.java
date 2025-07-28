/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
        executor.setCorePoolSize(2);
        // Set maximum number of threads that can be created
        executor.setMaxPoolSize(4);
        // Set maximum number of tasks that can be queued
        executor.setQueueCapacity(100);
        // Set thread name prefix for logging purposes
        executor.setThreadNamePrefix("EmailAsync-");
        // Initialize the executor
        executor.initialize();
        return executor;
    }
}