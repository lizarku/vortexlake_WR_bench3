package com.vortexlake.bench;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TarMain {

    public static InputStream encode(List<Path> files) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(baos)) {
            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            for (Path file : files) {
                TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), file.getFileName().toString());
                tarOut.putArchiveEntry(entry);
                Files.copy(file, tarOut);
                tarOut.closeArchiveEntry();
            }
            tarOut.finish();
        }
        System.out.println("tar 완료: " + files.size() + "개 파일");
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
