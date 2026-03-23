package com.vortexlake.bench;

import com.github.luben.zstd.ZstdOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PressMain {

    private static final int ZSTD_LEVEL = 1;
    private static final int KRYO_HEADER_SIZE = 24;

    public static InputStream compress(InputStream encodedStream, String encodingType) throws Exception {
        Path tempFile = Files.createTempFile("zstd_", ".zst");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZstdOutputStream zstdOut = new ZstdOutputStream(bos, ZSTD_LEVEL)) {
            encodedStream.transferTo(zstdOut);
        }
        return new BufferedInputStream(new FileInputStream(tempFile.toFile()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.deleteIfExists(tempFile);
            }
        });
    }
}
