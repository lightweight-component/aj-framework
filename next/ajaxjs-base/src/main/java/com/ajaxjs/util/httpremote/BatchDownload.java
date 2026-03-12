/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.RegExpUtils;
import com.ajaxjs.util.io.DataWriter;
import com.ajaxjs.util.io.FileHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class for batch downloading files from multiple URLs concurrently.
 * Uses multi-threading to perform parallel downloads with configurable timeout.
 */
@Slf4j
public class BatchDownload {
    /**
     * CountDownLatch to synchronize thread completion. Alternative: CyclicBarrier
     */
    private final CountDownLatch latch;

    /**
     * Array of URLs to download
     */
    private final String[] arr;

    /**
     * Directory to save downloaded files
     */
    private final String saveFolder;

    /**
     * Function to generate new file names. If null, original file names are used.
     */
    private final Supplier<String> newFileNameFn;

    /**
     * Creates a new BatchDownload instance for downloading files concurrently.
     *
     * @param arr           array of URLs to download
     * @param saveFolder    directory to save downloaded files
     * @param newFileNameFn function to generate new file names. If null, original file names are used.
     */
    public BatchDownload(String[] arr, String saveFolder, Supplier<String> newFileNameFn) {
        latch = new CountDownLatch(arr.length);

        this.arr = arr;
        this.saveFolder = saveFolder;
        this.newFileNameFn = newFileNameFn;
    }

    /**
     * Executes download for a single URL.
     *
     * @param url the URL to download from
     * @param i   the index in the array to update with the file name
     */
    private void exec(String url, int i) {
        String newFileName;

        try {
            if (newFileNameFn == null)
                newFileName = download(HttpConstant.HttpMethod.GET, url, null, saveFolder, null);
            else
                newFileName = download(HttpConstant.HttpMethod.GET, url, null, saveFolder, newFileNameFn.get());

            String[] _arr = newFileName.split("\\\\");
            String f = _arr[_arr.length - 1];
            arr[i] = f;
        } finally {
            latch.countDown(); // Decrement latch in all cases, regardless of success or exception
        }
    }

    /**
     * Starts concurrent downloads for all URLs in the array.
     * Spawns a new thread for each download and waits for completion (with timeout).
     */
    public void start() {
        for (int i = 0; i < arr.length; i++) {
            final int j = i;
            new Thread(() -> exec(arr[j], j)).start();
        }

        try {
            // Set the maximum waiting time of 20 seconds for the main thread
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads a file from the specified URL using the given HTTP method.
     *
     * @param method      HTTP method to use (GET, POST, etc.)
     * @param url         the URL to download from
     * @param fn          optional connection initializer function
     * @param saveDir     directory to save the downloaded file
     * @param newFileName optional new name for the downloaded file
     * @return the absolute path of the downloaded file
     */
    public static String download(HttpConstant.HttpMethod method, String url, Consumer<HttpURLConnection> fn, String saveDir, String newFileName) {
        Request get = new Request(method, url);

        Consumer<HttpURLConnection> init = conn -> {
            // Set up connection properties
            conn.setDoInput(true);
            conn.setDoOutput(true);
        };
        get.init(fn == null ? init : fn.andThen(init));

        String fileName;
        String oldFileName = getFileNameFromUrl(url);

        if (newFileName == null)
            fileName = oldFileName;
        else
            fileName = newFileName + RegExpUtils.regMatch("\\.\\w+$", oldFileName); // New filename + old extension

        assert fileName != null;
        File file = new File(saveDir, fileName);
        get.setInputStreamConsumer(in -> {
            // Create directory if it doesn't exist
            new FileHelper(saveDir).createDirectory();

            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                new DataWriter(out).write(in);
                log.info("File [{}] written successfully", file);

                in.close();
            } catch (IOException e) {
                log.warn("ERROR>>", e);
            }
        });
        get.connect();

        return file.toString();
    }

    /**
     * Extracts the file name from a URL.
     *
     * @param urlString the URL string to extract the file name from
     * @return the extracted file name, or null if extraction fails
     */
    public static String getFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            int lastSlashIndex = path.lastIndexOf('/');

            if (lastSlashIndex == -1)
                return path; // return the whole path if no slash is found

            return path.substring(lastSlashIndex + 1);
        } catch (MalformedURLException e) {
            log.warn("getFileNameFromUrl:", e);
            return null;
        }
    }
}