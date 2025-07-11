package com.ajaxjs.business.mysqlbinlog;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.Instant;

@Slf4j
public class BinLogPosition {
    private long lastLoggedSaveException = 0;
    private File persistFile;
    private String binlogFile;
    private long position;

    public BinLogPosition() {
        this(null, 0);
    }

    public BinLogPosition(String binlogFile, long position) {
        this.binlogFile = binlogFile;
        this.position = position;
        persistFile = null;
    }

    public BinLogPosition(File persistFile) {
        try (BufferedReader in = new BufferedReader(new FileReader(persistFile))) {
            binlogFile = in.readLine();
            position = Long.parseLong(in.readLine());
            this.persistFile = persistFile;
        } catch (Exception ex) {
            log.warn("Can't load binlog position from file: " + persistFile, ex);
            binlogFile = null;
            position = 0;
            this.persistFile = persistFile;
        }
    }

    public String getBinlogFile() {
        return binlogFile;
    }

    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
        save();
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
        save();
    }

    public void enablePersistance(File file) {
        this.persistFile = file;
    }

    public void disablePersistance() {
        persistFile = null;
    }

    public boolean isPersistenceEnabled() {
        return persistFile != null;
    }

    public void setBinLogFileAndPosition(String binlogFile, long position) {
        this.binlogFile = binlogFile;
        this.position = position;
        save();
    }

    private void save() {
        if (isPersistenceEnabled()) {
            try (PrintWriter out = new PrintWriter(persistFile)) {
                out.println(binlogFile);
                out.println(position);
            } catch (IOException ex) {
                Instant now = Instant.now();
                if (now.getEpochSecond() > lastLoggedSaveException + 10) {
                    log.warn("Can't save binlog position to file: " + persistFile, ex);
                    lastLoggedSaveException = now.getEpochSecond();
                }
            }
        }
    }

    @Override
    public String toString() {
        return binlogFile + ":" + position;
    }
}
