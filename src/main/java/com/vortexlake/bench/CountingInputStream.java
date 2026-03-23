package com.vortexlake.bench;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 읽힌 바이트 수를 추적하는 InputStream 래퍼.
 * 직렬화 후 크기(압축 전)를 측정하기 위해 사용.
 */
public class CountingInputStream extends FilterInputStream {

    private long bytesRead = 0;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) bytesRead++;
        return b;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int n = super.read(buf, off, len);
        if (n > 0) bytesRead += n;
        return n;
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
