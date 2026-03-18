package com.vortexlake.bench;

import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

public class PressMain {

    private static final int ZSTD_LEVEL = 1;
    private static final int KRYO_HEADER_SIZE = 24;

    public static InputStream compress(InputStream encodedStream, String encodingType) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZstdOutputStream zstdOut = new ZstdOutputStream(baos, ZSTD_LEVEL)) {
            encodedStream.transferTo(zstdOut);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
