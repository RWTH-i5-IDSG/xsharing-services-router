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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 31.05.2016
 */
public class LogStreamingOutput implements StreamingOutput {

    @Override
    public void write(OutputStream os) throws IOException, WebApplicationException {

        try (OutputStreamWriter osw = new OutputStreamWriter(os);
             BufferedWriter writer = new BufferedWriter(osw)) {

            Optional<Path> path = LogFileRetriever.INSTANCE.getPath();

            if (path.isPresent()) {
                Files.lines(path.get(), StandardCharsets.UTF_8)
                     .forEach(line -> writeLine(writer, line));
            } else {
                writer.write(LogFileRetriever.INSTANCE.getErrorMessage());
            }

            writer.flush();
        }
    }

    private static void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line + "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
