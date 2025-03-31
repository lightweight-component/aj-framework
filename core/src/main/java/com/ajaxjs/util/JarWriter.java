package com.ajaxjs.util;


import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Utility to write to a jar/war file.
 * 对某个已有的jar/war包写入新的文件、或覆写已有文件
 * <a href="https://segmentfault.com/a/1190000003097129">...</a>
 *
 * @author <a href="mailto:miles.wy.1@gmail.com">pf_miles</a>
 */
public class JarWriter {
    // the war file to write at last
    private final File warFile;
    // the temp directory to pre-write...
    private final File tempDir;

    private static final byte[] buf = new byte[1048576];// the writing buffer

    /**
     * create a war writer upon a war file... should also works for a jar file
     *
     * @param warPath the absolute path of the underlying war file
     */
    public JarWriter(String warPath) {
        File f = new File(warPath);

        if (!f.exists())
            throw new RuntimeException("War file does not exist: " + warPath);

        // test if zip format
        try (JarInputStream i = new JarInputStream(new FileInputStream(f))) {
            if (i.getNextEntry() == null)
                throw new RuntimeException("Not jar/war format: " + warPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Not jar/war format: " + warPath, e);
        }

        this.warFile = f;
        this.tempDir = createTempDirectory(f.getName());   // create temp directory
    }

    private static File createTempDirectory(String warName) {
        File temp;

        try {
            temp = File.createTempFile(warName, Long.toString(System.currentTimeMillis()));
            temp.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (!(temp.delete()))
            throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());

        if (!(temp.mkdir()))
            throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());

        return (temp);
    }

    /**
     * Complete writing, rebuild the final result jar/war file and do cleaning.
     *
     * @throws IOException
     */
    public void done() throws IOException {
        // really writing to the war file, in fact a merging from the temp dir
        // writing to war listing temp dir files in jar entry naming style
        Map<String, File> tempDirFiles = listFilesInJarEntryNamingStyle(this.tempDir, this.tempDir.getAbsolutePath());
        File tempWar = File.createTempFile(warFile.getName(), null);    // create temp war
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempWar)); // merging write to the temp war

        try (JarFile jf = new JarFile(warFile)) {
            Enumeration<JarEntry> iter = jf.entries();

            while (iter.hasMoreElements()) {
                JarEntry e = iter.nextElement();
                String name = e.getName();

                if (!e.isDirectory() && name.endsWith(".jar")) {
                    writeJarEntry(e, filterByDirName(tempDirFiles, name), jf, jos);
                } else {
                    // prefer file in dir to war
                    InputStream fin = null;
                    if (tempDirFiles.containsKey(name)) {
                        File f = tempDirFiles.get(name);

                        if (!e.isDirectory())
                            fin = new FileInputStream(f);

                        addEntry(name, fin, f.lastModified(), jos);
                        tempDirFiles.remove(name);
                    } else {
                        if (!e.isDirectory())
                            fin = jf.getInputStream(e);

                        addEntry(name, fin, e.getTime(), jos);
                    }
                }
            }
        }

        // // writing remained files in dir
        for (Map.Entry<String, File> remain : tempDirFiles.entrySet()) {
            String dirFileName = remain.getKey();
            File dirFile = remain.getValue();
            InputStream in = null;

            if (!dirFile.isDirectory())
                in = new FileInputStream(dirFile);

            addEntry(dirFileName, in, dirFile.lastModified(), jos);
        }
        // // replace the target war using the temp war
        jos.close();
        moveTo(tempWar, warFile);
        // clean
        // // cleaning temp dir
        recurDel(this.tempDir);
    }

    // move from to files
    private void moveTo(File from, File to) throws IOException {
        // try rename directly
        if (!from.renameTo(to)) {
            // renameTo failed, fallback to file flowing...
            System.out.println("File.renameTo failed, fallback to file streaming...");

            if (!from.exists())
                throw new IOException("From file does not exist: " + from.getAbsolutePath());

            if (!to.exists() && !to.createNewFile())
                throw new IOException("To from does not exist and cannot be created: " + to.getAbsolutePath());

            try (OutputStream o = new FileOutputStream(to)) {
                flowTo(new FileInputStream(from), o);
            } finally {
                from.delete();
            }

            System.out.println("File stream flowing moving done!");
        }
    }

    /*
     * list the files&dirs in the specified dir, in a jar entry naming style: 1)
     * all file names come with no preceding '/' 2) all file names of
     * directories must be suffixed by a '/'
     */
    private static Map<String, File> listFilesInJarEntryNamingStyle(File f, String basePath) {
        Map<String, File> ret = new HashMap<>();
        String name = f.getAbsolutePath().substring(basePath.length());

        if (name.startsWith("/"))
            name = name.substring(1);

        if (f.isDirectory()) {
            if (!name.endsWith("/"))
                name += "/";
            for (File sub : Objects.requireNonNull(f.listFiles()))
                ret.putAll(listFilesInJarEntryNamingStyle(sub, basePath));
        }

        // add the current level directory itself except for the root dir
        if (!"/".equals(name))
            ret.put(name, f);

        return ret;
    }

    private static void recurDel(File file) {
        if (file.isDirectory()) {
            for (File item : Objects.requireNonNull(file.listFiles()))
                recurDel(item);
        }
        file.delete();
    }

    // merging write jar entry
    private void writeJarEntry(JarEntry origJarEntry, Map<String, File> mergingFiles, JarFile origWar, JarOutputStream targetWarStream)
            throws IOException {
        // if there's no merging file for this jar entry, write the original jar
        // data directly
        if (mergingFiles == null || mergingFiles.isEmpty()) {
            JarEntry je = new JarEntry(origJarEntry.getName());
            je.setTime(origJarEntry.getTime());
            targetWarStream.putNextEntry(je);
            flowTo(origWar.getInputStream(origJarEntry), targetWarStream);
            targetWarStream.closeEntry();
        } else {
            String origJarEntryName = origJarEntry.getName();
            long modTime;
            String mergingDirName = origJarEntryName + "/";

            if (mergingFiles.containsKey(mergingDirName))
                modTime = mergingFiles.get(mergingDirName).lastModified();
            else
                modTime = origJarEntry.getTime();

            JarEntry je = new JarEntry(origJarEntryName);
            je.setTime(modTime);
            targetWarStream.putNextEntry(je);

            mergingFiles.remove(mergingDirName);

            // build the jar data
            String jarSimpleName = origJarEntryName.contains("/") ? origJarEntryName.substring(origJarEntryName.lastIndexOf("/") + 1)
                    : origJarEntryName;
            // // build the tmp jar file to write to
            File tmpOutputJarFile = File.createTempFile(jarSimpleName, null);
            JarOutputStream tmpOutputJar = new JarOutputStream(new FileOutputStream(tmpOutputJarFile));

            // // dump the original jar file to iterate over
            File tmpOrigJarFile = buildTempOrigJarFile(jarSimpleName + "_orig", origWar.getInputStream(origJarEntry));
            JarFile tmpOrigJar = new JarFile(tmpOrigJarFile);

            for (Enumeration<JarEntry> e = tmpOrigJar.entries(); e.hasMoreElements(); ) {
                JarEntry origJarItemEntry = e.nextElement();
                String origJarItemEntryName = origJarItemEntry.getName();
                String mergingFileName = mergingDirName + origJarItemEntryName;
                InputStream itemIn = null;
                long itemModTime = -1;
                // prefer dir files to origJar entries
                if (mergingFiles.containsKey(mergingFileName)) {
                    File f = mergingFiles.get(mergingFileName);
                    if (!origJarItemEntry.isDirectory())
                        itemIn = new FileInputStream(f);
                    itemModTime = f.lastModified();
                    mergingFiles.remove(mergingFileName);
                } else {
                    if (!origJarItemEntry.isDirectory())
                        itemIn = tmpOrigJar.getInputStream(origJarItemEntry);
                    itemModTime = origJarItemEntry.getTime();
                }
                addEntry(origJarItemEntryName, itemIn, itemModTime, tmpOutputJar);
            }
            tmpOrigJar.close();
            tmpOrigJarFile.delete();

            // check&write remained dir files
            for (Map.Entry<String, File> remain : mergingFiles.entrySet()) {
                String dirFileName = remain.getKey();
                File dirFile = remain.getValue();
                InputStream in = null;
                if (!dirFile.isDirectory())
                    in = new FileInputStream(dirFile);

                addEntry(dirFileName.substring(mergingDirName.length()), in, dirFile.lastModified(), tmpOutputJar);
            }

            tmpOutputJar.close();

            // write to war
            InputStream jarData = new FileInputStream(tmpOutputJarFile);
            flowTo(jarData, targetWarStream);
            jarData.close();
            tmpOutputJarFile.delete();

            targetWarStream.closeEntry();
        }
    }

    // build a temp file containing the given inputStream data
    private File buildTempOrigJarFile(String name, InputStream in) throws IOException {
        File f = File.createTempFile(name, null);
        try (OutputStream out = new FileOutputStream(f)) {
            flowTo(in, out);
        }

        return f;
    }

    // data stream 'flow' from in to out, pseudo-zero-copy
    private static void flowTo(InputStream in, OutputStream out) throws IOException {
        try {
            for (int count = in.read(buf); count != -1; count = in.read(buf))
                out.write(buf, 0, count);
        } finally {
            in.close();
        }
    }

    // collect entries which contain the specified dir path segment, and also
    // delete from the original map
    private Map<String, File> filterByDirName(Map<String, File> nameFileMapping, String pathSegment) {
        if (nameFileMapping == null || nameFileMapping.isEmpty())
            return Collections.emptyMap();

        Map<String, File> ret = new HashMap<>();
        if (!pathSegment.endsWith("/"))
            pathSegment += "/";

        for (Iterator<Map.Entry<String, File>> iter = nameFileMapping.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, File> e = iter.next();

            if (e.getKey().contains(pathSegment)) {
                ret.put(e.getKey(), e.getValue());
                iter.remove();
            }
        }

        return ret;
    }

    private static void addEntry(String entryName, InputStream in, long modTime, JarOutputStream target) throws IOException {
        JarEntry e = new JarEntry(entryName);
        e.setTime(modTime);
        target.putNextEntry(e);

        if (in != null)
            flowTo(in, target);

        target.closeEntry();
    }

    /**
     * create outputStream writing to the specified war/jar file, all paths
     * specified here are relative to the root of the war/jar.
     */
    public OutputStream getFileOutputStream(String relPath) throws IOException {
        if (relPath.startsWith("/"))
            relPath = relPath.substring(1);
        if (relPath.endsWith("/"))
            relPath = relPath.substring(0, relPath.length() - 1);

        File f = new File(this.tempDir.getAbsolutePath() + "/" + relPath);
        File p = f.getParentFile();

        if (p != null && !p.exists())
            p.mkdirs();

        if (!f.exists())
            f.createNewFile();

        return new FileOutputStream(f);
    }

    /**
     * get the temporarily pre-writing directory
     */
    public String getTempPrewriteDir() {
        return this.tempDir.getAbsolutePath();
    }

    /**
     * return the current writing war file path
     */
    public String getWarFilePath() {
        return this.warFile.getAbsolutePath();
    }

}