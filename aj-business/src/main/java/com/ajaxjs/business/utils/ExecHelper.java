package com.ajaxjs.business.utils;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Convenience methods for executing non-Java processes.
 * 命令行工具类
 * <a href="https://www.cnblogs.com/yzuzhang/p/5799092.html">...</a>
 *
 * @since ostermillerutils 1.06.00
 */
public final class ExecHelper {
    public static void main(String[] args) {
        try {
            /*String file = ExecHelper.exec(
                new String[]{"XXXX"}, "GBK"
            ).getOutput();

            String hello = ExecHelper.execUsingShell(
                "echo 'Hello World'"
            ).getOutput();*/

            ExecHelper helper = ExecHelper.execUsingShell("java -version", "GBK");
            System.out.println(helper.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray), null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray, String[] envp) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray, envp), null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray, String[] envp, File dir) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray, envp), null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray, String charset) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray), charset);
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray, String[] envp, String charset) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray, envp), charset);
    }

    /**
     * Executes the specified command and arguments in a separate process, and waits for the
     * process to finish.
     */
    public static ExecHelper exec(String[] cmdArray, String[] envp, File dir, String charset) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdArray, envp), charset);
    }

    /**
     * Executes the specified command using a shell.  On windows uses cmd.exe or command.exe.
     * On other platforms it uses /bin/sh.
     */
    public static ExecHelper execUsingShell(String command) throws IOException {
        return execUsingShell(command, null);
    }

    /**
     * Executes the specified command using a shell.  On windows uses cmd.exe or command.exe.
     * On other platforms it uses /bin/sh.
     */
    public static ExecHelper execUsingShell(String command, String charset) throws IOException {
        if (command == null) throw new NullPointerException();
        String[] cmdArray;
        String os = System.getProperty("os.name");

        if (os.equals("Windows 95") || os.equals("Windows 98") || os.equals("Windows ME"))
            cmdArray = new String[]{"command.exe", "/C", command};
        else if (os.startsWith("Windows")) cmdArray = new String[]{"cmd.exe", "/C", command};
        else cmdArray = new String[]{"/bin/sh", "-c", command};

        return new ExecHelper(Runtime.getRuntime().exec(cmdArray), charset);
    }

    /**
     * Take a process, record its standard error and standard out streams, wait for it to finish
     *
     * @param process process to watch
     * @throws SecurityException         if a security manager exists and its checkExec method doesn't allow creation of a subprocess.
     * @throws IOException               - if an I/O error occurs
     * @throws NullPointerException      - if cmdArray is null
     * @throws IndexOutOfBoundsException - if cmdArray is an empty array (has length 0).
     */
    private ExecHelper(Process process, String charset) throws IOException {
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        Reader stdout;
        Reader stderr;

        if (charset == null) {
            // This is one time that the system charset is appropriate,
            // don't specify a character set.
            stdout = new InputStreamReader(process.getInputStream());
            stderr = new InputStreamReader(process.getErrorStream());
        } else {
            stdout = new InputStreamReader(process.getInputStream(), charset);
            stderr = new InputStreamReader(process.getErrorStream(), charset);
        }

        char[] buffer = new char[1024];

        boolean done = false;
        boolean stdoutclosed = false;
        boolean stderrclosed = false;

        while (!done) {
            boolean readSomething = false;
            // read from the process's standard output
            if (!stdoutclosed && stdout.ready()) {
                readSomething = true;
                int read = stdout.read(buffer, 0, buffer.length);

                if (read < 0) {
                    readSomething = true;
                    stdoutclosed = true;
                } else if (read > 0) {
                    readSomething = true;
                    output.append(buffer, 0, read);
                }
            }

            // read from the process's standard error
            if (!stderrclosed && stderr.ready()) {
                int read = stderr.read(buffer, 0, buffer.length);

                if (read < 0) {
                    readSomething = true;
                    stderrclosed = true;
                } else if (read > 0) {
                    readSomething = true;
                    error.append(buffer, 0, read);
                }
            }

            // Check the exit status only we haven't read anything,
            // if something has been read, the process is obviously not dead yet.
            if (!readSomething) {
                try {
                    this.status = process.exitValue();
                    done = true;
                } catch (IllegalThreadStateException itx) {
                    // Exit status not ready yet.
                    // Give the process a little breathing room.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ix) {
                        process.destroy();
                        throw new IOException("Interrupted - processes killed");
                    }
                }
            }
        }

        this.output = output.toString();
        this.error = error.toString();
    }

    /**
     * The output of the job that ran.
     */
    private final String output;

    /**
     * Get the output of the job that ran.
     *
     * @return Everything the executed process wrote to its standard output as a String.
     */
    public String getOutput() {
        return output;
    }

    /**
     * The error output of the job that ran.
     */
    private final String error;

    /**
     * Get the error output of the job that ran.
     *
     * @return Everything the executed process wrote to its standard error as a String.
     */
    public String getError() {
        return error;
    }

    /**
     * The status of the job that ran.
     */
    private int status;

    /**
     * Get the status of the job that ran.
     *
     * @return exit status of the executed process, by convention, the value 0 indicates normal termination.
     */
    public int getStatus() {
        return status;
    }

    public String getResult() {
        if (output != null && error != null)
            return output + "\n\n" + error;

        if (output != null)
            return output;

        return error;
    }
}