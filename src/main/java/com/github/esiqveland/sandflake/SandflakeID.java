package com.github.esiqveland.sandflake;

import com.github.esiqveland.sandflake.encoding.BaseEncoding;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;

public class SandflakeID {
    final static String alphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    final static int encodedLen = 26;

    final static int timestampOffset = 0;
    final static int timestampLength = 6;
    final static int workerIDLength = 4;
    final static int sequenceLength = 3;
    final static int randomLength = 3;

    final static int workerIDOffset = timestampLength;
    final static int sequenceOffset = timestampLength + workerIDLength;
    final static int randomOffset = sequenceOffset + sequenceLength;
    final static int totalSize = timestampLength + workerIDLength + sequenceLength + randomLength;

    final static int maxSequenceNumber = 0x00FFFFFF;

    private final static BaseEncoding base32Sandflake = BaseEncoding.base32Sandflake();
    private final static BaseEncoding encoder = BaseEncoding.base32Sandflake().omitPadding();

    // 48bit: timestamp
    // 32bit: worker id (defaults to random)
    // 24bit: sequence number
    // 24bit: random number
    private final byte[] raw;

    SandflakeID(byte[] data) {
        if (data.length != totalSize) {
            throw new IllegalArgumentException("size must be " + totalSize + " but was " + data.length);
        }
        this.raw = data;
    }

    SandflakeID(Instant ts, byte[] workerId, int seq, byte[] randomBytes) {
        long now = ts.toEpochMilli();
        ByteBuffer buf = ByteBuffer.allocate(totalSize);
        buf.put((byte) ((now >> 40) & 0xff));
        buf.put((byte) ((now >> 32) & 0xff));
        buf.put((byte) ((now >> 24) & 0xff));
        buf.put((byte) ((now >> 16) & 0xff));
        buf.put((byte) ((now >> 8) & 0xff));
        buf.put((byte) ((now >> 0) & 0xff));

        buf.put(workerId);

        buf.put((byte) ((seq >> 16) & 0xff));
        buf.put((byte) ((seq >> 8) & 0xff));
        buf.put((byte) ((seq >> 0) & 0xff));

        buf.put(randomBytes);

        this.raw = buf.array();
    }

    public byte[] getRandomBytes() {
        byte[] buf = new byte[3];
        int offset = 0;
        for (int i = randomOffset; i < randomOffset + randomLength; i++) {
            buf[offset] = raw[i];
            offset++;
        }

        return buf;
    }

    public int getSequenceID() {
        ByteBuffer wrap = ByteBuffer.allocate(4).put((byte) 0);
        for (int i = sequenceOffset; i < sequenceOffset + sequenceLength; i++) {
            wrap.put(raw[i]);
        }
        int i = wrap.getInt(0);
        return i;
    }

    public byte[] getWorkerID() {
        ByteBuffer wrap = ByteBuffer.allocate(4);
        for (int i = workerIDOffset; i < workerIDOffset + workerIDLength; i++) {
            wrap.put(raw[i]);
        }
        return wrap.array();
    }

    public Instant getTimestamp() {
        // timestamp is 48-bit, but a long we parse it into is 64bit,
        // so skip first 16 bits.
        ByteBuffer wrap = ByteBuffer.allocate(8).put((byte) 0).put((byte) 0);
        for (int i = timestampOffset; i < timestampOffset + timestampLength; i++) {
            wrap.put(raw[i]);
        }
        long ts = wrap.getLong(0);

        return Instant.ofEpochMilli(ts);
    }

    public byte[] getBytes() {
        return ByteBuffer.allocate(raw.length).put(raw).array();
    }

    public static SandflakeID decode(String s) {
        byte[] decode = base32Sandflake.decode(s);
        return new SandflakeID(decode);
    }

    public static String encode(SandflakeID id) {
        return encoder.encode(id.raw);
    }

    @Override
    public String toString() {
        return "SandflakeID{" +
                "id=" + encode(this) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SandflakeID that = (SandflakeID) o;

        return Arrays.equals(raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(raw);
    }
}
