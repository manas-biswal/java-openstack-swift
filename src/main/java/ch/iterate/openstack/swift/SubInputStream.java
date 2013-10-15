package ch.iterate.openstack.swift;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

/**
 * A SubInputStream class that provides a shorter sub-stream from a given InputStream.
 * This class can be used to create large object segments from a given InputStream
 * without reading each segment's complete contents into a byte[].
 *
 * @author jjw
 */
public class SubInputStream extends FilterInputStream {
    private long bytesRemaining;
    private boolean closeSource;

    public SubInputStream(InputStream inp, long maxLength, boolean closeSource) {
        super(inp);
        this.bytesRemaining = maxLength;
        this.closeSource = closeSource;
    }

    @Override
    public int read() throws IOException {
        // No more data to be read from this subStream
        if (bytesRemaining == 0) return -1;

        int data = super.read();
        if (data >= 0) bytesRemaining--;
        return data;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // No more data to return from this subStream
        if (bytesRemaining == 0) return -1;
        // test off and len to ensure safe cast later
        if (off < 0 || len < 0) throw (new IndexOutOfBoundsException());

        int bytesRead;
        if (bytesRemaining > len) {
            bytesRead = super.read(b, off, len);
        } else {
            // cast to int is ok for bytesRemaining because
            // (long) bytesRemaining < (int) len - off
            bytesRead = super.read(b, off, (int) bytesRemaining);
        }
        bytesRemaining -= bytesRead;
        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOException {
        // No reverse skips
        if (n <= 0) return 0;

        long bytesSkipped;
        if (bytesRemaining > n) {
            bytesSkipped = super.skip(n);
        } else {
            bytesSkipped = super.skip(bytesRemaining);
        }
        bytesRemaining -= bytesSkipped;
        return bytesSkipped;
    }

    @Override
    public void close() throws IOException {
        if (closeSource) {
            super.close();
        }
        bytesRemaining = 0;
    }
}
