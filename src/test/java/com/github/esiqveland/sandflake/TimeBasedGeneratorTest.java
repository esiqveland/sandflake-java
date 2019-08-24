package com.github.esiqveland.sandflake;

import org.junit.Test;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class TimeBasedGeneratorTest {

    @Test
    public void testDefaultConstructor() {
        TimeBasedGenerator generator = new TimeBasedGenerator();
        SandflakeID id = generator.next();
        SandflakeID next = generator.next();

        assertThat(id).isNotEqualTo(next);
        assertThat(SandflakeID.encode(id)).isNotEqualTo(SandflakeID.encode(next));
    }

    @Test
    public void testNext() {
        Instant now = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        TimeBasedGenerator gen = new TimeBasedGenerator(() -> now, new SecureRandom());
        SandflakeID id = gen.next();

        assertThat(id).isNotNull();
        assertThat(id.getTimestamp()).isEqualTo(now);
    }

    @Test
    public void test_generate_10000() {
        Instant now = Instant.ofEpochMilli(Instant.now().toEpochMilli());
        TimeBasedGenerator gen = new TimeBasedGenerator(() -> now, new SecureRandom());

        Set<SandflakeID> set = new HashSet<>();
        Set<String> setStrs = new HashSet<>();
        int count = 10_000;

        for (int i = 0; i < count; i++) {
            SandflakeID id = gen.next();
            assertThat(id).isNotNull();
            assertThat(id.getTimestamp()).isEqualTo(now);
            assertThat(id.getSequenceID()).isEqualTo(i);

            boolean added = set.add(id);
            assertThat(added).isTrue();

            boolean addedStr = setStrs.add(SandflakeID.encode(id));
            assertThat(addedStr).isTrue();
        }

    }

}