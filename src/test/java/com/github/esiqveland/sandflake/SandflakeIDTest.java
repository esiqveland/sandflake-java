package com.github.esiqveland.sandflake;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class SandflakeIDTest {

    private static final String MAGIC_VALUE = "05E4ECYW2GZ66B8AFZZZZMKFPR";

    @Test
    public void testMaxSequence() {
        assertThat(SandflakeID.maxSequenceNumber).isEqualTo(16777215);
    }

    @Test
    public void fromValue() {
        SandflakeID id = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        Instant ts = id.getTimestamp();
        byte[] raw = id.getBytes();

        assertThat(ts.atZone(ZoneOffset.UTC).toString()).isEqualTo("2017-05-27T00:00:00.020Z");
        assertThat(id.getWorkerID()).isEqualTo(new byte[]{62, 99, 45, 10});
        assertThat(id.getSequenceID()).isEqualTo(8388607);
        assertThat(id.getRandomBytes()).isEqualTo(new byte[]{(byte) 210, 111, (byte) 182});
        assertThat(raw).isEqualTo(new byte[]{1, 92, 71, 51, (byte) 220, 20, 62, 99, 45, 10, 127, (byte) 255, (byte) 255, (byte) 210, 111, (byte) 182});
    }

    @Test
    public void fromConstructor() {
        SandflakeID idKnown = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        byte[] magicBytes = new byte[]{1, 92, 71, 51, (byte) 220, 20, 62, 99, 45, 10, 127, (byte) 255, (byte) 255, (byte) 210, 111, (byte) 182};

        byte[] raw = idKnown.getBytes();
        assertThat(raw).isEqualTo(magicBytes);

        Instant ts = idKnown.getTimestamp();
        byte[] workerID = idKnown.getWorkerID();
        int seq = idKnown.getSequenceID();
        byte[] rand = idKnown.getRandomBytes();

        assertThat(ts.atZone(ZoneOffset.UTC).toString()).isEqualTo("2017-05-27T00:00:00.020Z");
        assertThat(workerID).isEqualTo(new byte[]{62, 99, 45, 10});
        assertThat(seq).isEqualTo(8388607);
        assertThat(rand).isEqualTo(new byte[]{(byte) 210, 111, (byte) 182});

        SandflakeID sandflakeID = new SandflakeID(
                ts,
                workerID,
                seq,
                rand
        );

        assertThat(sandflakeID.getBytes()).isEqualTo(magicBytes);
        assertThat(sandflakeID).isEqualTo(idKnown);
        assertThat(SandflakeID.encode(sandflakeID)).isEqualTo("05E4ECYW2GZ66B8AFZZZZMKFPR");
    }

    @Test
    public void testEncodedLength() {
        SandflakeID id = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        assertThat(SandflakeID.encode(id)).hasSize(26);
    }

    @Test
    public void testTotalSize() {
        SandflakeID id = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        assertThat(SandflakeID.totalSize).isEqualTo(16);
        assertThat(id.getBytes()).hasSize(16);
        assertThat(id.getRandomBytes()).hasSize(3);
        assertThat(id.getWorkerID()).hasSize(4);
    }

    @Test
    public void testParseIsSymmetric() {
        SandflakeID id = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        assertThat(SandflakeID.encode(SandflakeID.decode(SandflakeID.encode(id)))).isEqualTo(MAGIC_VALUE);
    }

    @Test
    public void testEquality() {
        SandflakeID id1 = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        SandflakeID id2 = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        SandflakeID id3 = SandflakeID.decode("05E4ECYW2HZ66B8AFZZZZMKFPR");

        assertThat(id1).isEqualTo(id2).isNotEqualTo(id3);
    }

    @Test
    public void testHashCode() {
        SandflakeID id1 = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        SandflakeID id2 = SandflakeID.decode("05E4ECYW2GZ66B8AFZZZZMKFPR");
        SandflakeID id3 = SandflakeID.decode("05E4ECYW2HZ66B8AFZZZZMKFPR");

        assertThat(id1.hashCode()).isEqualTo(id2.hashCode()).isNotEqualTo(id3.hashCode());
    }

}