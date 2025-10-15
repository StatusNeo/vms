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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for SQLite persistence layer.
 * This configuration is only active when the 'sqlite' profile is enabled.
 * 
 * To enable SQLite persistence:
 * - Set spring.profiles.active=sqlite in application.yml, or
 * - Use --spring.profiles.active=sqlite as a command line argument, or
 * - Set SPRING_PROFILES_ACTIVE=sqlite as an environment variable
 * 
 * Note: SQLite is intended for development and testing purposes.
 * For production, PostgreSQL with JPA should be used.
 */
@Configuration
@Profile("sqlite")
@ConditionalOnProperty(name = "vms.persistence.sqlite.enabled", havingValue = "true", matchIfMissing = false)
public class SQLiteConfig {
    
    /**
     * Default constructor.
     * This configuration class uses Spring Boot auto-configuration
     * with the settings from application-sqlite.yml.
     */
    public SQLiteConfig() {
        // SQLite configuration is driven by application-sqlite.yml
        // This class serves as a marker for the SQLite profile
    }
}
