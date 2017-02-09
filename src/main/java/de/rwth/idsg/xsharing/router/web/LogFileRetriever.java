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
package de.rwth.idsg.xsharing.router.web;

import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.MemoryMappedFileAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 05.11.2015
 */
@Slf4j
public final class LogFileRetriever {
    public static final LogFileRetriever INSTANCE = new LogFileRetriever();

    private List<Path> logPathList;
    private Random random = new Random();

    private LogFileRetriever() {
        logPathList = getActiveLogFilePaths();
    }

    public Optional<Path> getPath() {
        Path p;
        if (logPathList.isEmpty()) {
            p = null;
        } else if (logPathList.size() == 1) {
            p = logPathList.get(0);
        } else {
            p = rollTheDice();
        }
        return Optional.fromNullable(p);
    }

    public String getLogFilePathOrErrorMessage() {
        Optional<Path> p = getPath();
        if (p.isPresent()) {
            return p.get().toAbsolutePath().toString();
        } else {
            return getErrorMessage();
        }
    }

    public String getErrorMessage() {
        return "Not available";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * If the user configured multiple file appenders, which log file should we choose?
     * Clearly, the only sane solution is rolling the dice.
     * Easter egg mode: On
     */
    private Path rollTheDice() {
        log.trace("Rolling the dice...");
        int index = random.nextInt(logPathList.size());
        return logPathList.get(index);
    }

    /**
     * We cannot presume that the default file name/location setting won't be changed by the user.
     * Therefore, we should be able to retrieve that info from the underlying logging mechanism
     * by iterating over appenders.
     */
    private List<Path> getActiveLogFilePaths() {
        LoggerContextFactory factory = LogManager.getFactory();
        ContextSelector selector = ((Log4jContextFactory) factory).getSelector();

        List<Path> fileNameList = new ArrayList<>();
        for (LoggerContext ctx : selector.getLoggerContexts()) {
            for (Appender appender : ctx.getConfiguration().getAppenders().values()) {
                String fileName = extractFileName(appender);
                if (fileName != null) {
                    fileNameList.add(Paths.get(fileName));
                }
            }
        }
        return fileNameList;
    }

    /**
     * File appender types do not share a "write-to-file" superclass.
     */
    private String extractFileName(Appender a) {
        if (a instanceof FileAppender) {
            return ((FileAppender) a).getFileName();

        } else if (a instanceof RollingFileAppender) {
            return ((RollingFileAppender) a).getFileName();

        } else if (a instanceof RollingRandomAccessFileAppender) {
            return ((RollingRandomAccessFileAppender) a).getFileName();

        } else if (a instanceof RandomAccessFileAppender) {
            return ((RandomAccessFileAppender) a).getFileName();

        } else if (a instanceof MemoryMappedFileAppender) {
            return ((MemoryMappedFileAppender) a).getFileName();

        } else {
            return null;
        }
    }
}
