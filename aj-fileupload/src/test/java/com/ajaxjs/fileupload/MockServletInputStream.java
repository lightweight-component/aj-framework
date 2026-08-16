/**
 * Copyright sp42 frank@ajaxjs.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ajaxjs.fileupload;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simulates a Servlet input stream object.
 * Source: <a href="https://stackoverflow.com/questions/4466770/how-to-write-unit-tests-with-commons-fileupload">...</a>
 */
public class MockServletInputStream extends ServletInputStream {
    /**
     * Input stream
     */
    private final InputStream delegate;

    /**
     * Creates a Servlet input stream object.
     *
     * @param b Data
     */
    public MockServletInputStream(byte[] b) {
        delegate = new ByteArrayInputStream(b);
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    /**
     * Returns whether all data has been read from the stream.
     *
     * @return {@code false} (this mock never indicates completion)
     */
    @Override
    public boolean isFinished() {
        return false;
    }

    /**
     * Returns whether the stream is ready to be read.
     *
     * @return {@code false} (this mock never indicates readiness)
     */
    @Override
    public boolean isReady() {
        return false;
    }

    /**
     * Sets the {@link ReadListener} for non-blocking I/O.
     * This mock implementation does nothing.
     *
     * @param readListener the read listener to register
     */
    @Override
    public void setReadListener(ReadListener readListener) {
    }
}