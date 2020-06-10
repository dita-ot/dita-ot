package org.dita.dost.ant;

import org.dita.dost.log.DITAOTAntLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static org.apache.commons.io.FileUtils.listFiles;

public class DumpTempDirectory {

    private static final String DUMP_TEMP = "dumpTemp";

    private DITAOTAntLogger logger;

    private File tempDir;

    private String taskName;

    public DumpTempDirectory(DITAOTAntLogger logger, String taskName, File tempDir) {
        this.logger = logger;
        this.tempDir = tempDir;
        this.taskName = taskName;
    }

    public void dump() {
        if (getProperty(DUMP_TEMP) == null || !"true".equalsIgnoreCase(getProperty(DUMP_TEMP).trim()) || !tempDirectoryExists() || "reg-tbl-ext".equals(taskName)) {
            return;
        }

        try {
            long ref = System.currentTimeMillis();
            Path dump = tempDir.toPath().resolveSibling("dump");
            Path zip = dump.resolve(format("%d-%s.zip", System.currentTimeMillis(), taskName));
            createDirectories(zip.getParent());
            createFile(zip);
            try (FileOutputStream fileStream = new FileOutputStream(zip.toFile());
                 ZipOutputStream zipStream = new ZipOutputStream(fileStream, UTF_8);) {
                for (File file : listFiles(tempDir, null, true)) {
                    writeZipEntry(tempDir, zipStream, file);
                }
            }
            logger.info("Dumped temp directory in {0}ms, filename {1}", System.currentTimeMillis()-ref, zip.getFileName());
        } catch (IOException e) {
            // ignore and continue with dita-ot processing
        }
    }

    private boolean tempDirectoryExists() {
        return tempDir != null && exists(get(tempDir.toURI()));
    }

    private void writeZipEntry(File folder, ZipOutputStream zipStream, File file) throws IOException {
        ZipEntry zipEntry = new ZipEntry(toName(file, folder));
        zipStream.putNextEntry(zipEntry);
        byte[] buffer = new byte[1024];
        try (FileInputStream fileStream = new FileInputStream(file);) {
            int len;
            while ((len = fileStream.read(buffer)) > 0) {
                zipStream.write(buffer, 0, len);
            }
        }
        zipStream.closeEntry();
    }

    private String toName(File file, File folder) {
        return file.toURI().toASCIIString().replaceAll(folder.toURI().toASCIIString(), "");
    }
}
