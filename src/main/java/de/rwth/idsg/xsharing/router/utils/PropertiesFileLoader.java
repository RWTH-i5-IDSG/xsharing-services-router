/*
 * Copyright (C) 2015-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.xsharing.router.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates java.util.Properties and adds type specific convenience methods
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.10.2015
 */
@Slf4j
public class PropertiesFileLoader {

    private Properties prop;

    /**
     * The name parameter acts as
     * 1) the file name to load from classpath, and
     * 2) the system property which can be set to load from file system.
     */
    public PropertiesFileLoader(String name) {
        String externalFileName = System.getProperty(name);

        if (externalFileName == null) {
            log.info("Hint: The Java system property '{}' can be set to point to an external properties file, " +
                    "which will be prioritized over the bundled one", name);
            loadFromClasspath(name);

        } else {
            loadFromSystem(externalFileName);
        }
    }

    // -------------------------------------------------------------------------
    // Strict
    // -------------------------------------------------------------------------

    public String getString(String key) {
        String s = prop.getProperty(key);

        if (s == null) {
            throw new IllegalArgumentException("The property '" + key + "' is not found");
        }

        if (s.isEmpty()) {
            throw new IllegalArgumentException("The property '" + key + "' has no value set");
        }

        return trim(key, s);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }

    public long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    public String[] getStringArray(String key) {
        return getString(key).split(",");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void loadFromSystem(String fileName) {
        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            prop = new Properties();
            prop.load(inputStream);
            log.info("Loaded properties from {}", fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFromClasspath(String fileName) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new FileNotFoundException("Property file '" + fileName + "' is not found in classpath");
            }
            prop = new Properties();
            prop.load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String trim(String key, String value) {
        String trimmed = value.trim();
        if (!trimmed.equals(value)) {
            log.warn("The property '{}' has leading or trailing spaces which were removed!", key);
        }
        return trimmed;
    }
}