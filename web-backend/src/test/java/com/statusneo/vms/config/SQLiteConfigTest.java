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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify SQLite JDBC driver is available and working.
 * This test does not require the full Spring context.
 */
class SQLiteConfigTest {

    @Test
    void testSQLiteDriverIsAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("org.sqlite.JDBC");
        }, "SQLite JDBC driver should be available on classpath");
    }

    @Test
    void testCanConnectToSQLite(@TempDir Path tempDir) throws Exception {
        String dbPath = tempDir.resolve("test.db").toString();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            assertNotNull(connection, "Should be able to create SQLite connection");
            
            DatabaseMetaData metaData = connection.getMetaData();
            assertEquals("SQLite", metaData.getDatabaseProductName(),
                    "Database product should be SQLite");
        }
    }

    @Test
    void testSQLiteDialectIsAvailable() {
        assertDoesNotThrow(() -> {
            Class.forName("org.hibernate.community.dialect.SQLiteDialect");
        }, "Hibernate SQLite dialect should be available on classpath");
    }

    @Test
    void testSQLiteConfigClassExists() {
        assertDoesNotThrow(() -> {
            Class.forName("com.statusneo.vms.config.SQLiteConfig");
        }, "SQLiteConfig class should exist");
    }
}
