package com.github.esiqveland.sandflake;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.esiqveland.sandflake.SandflakeID.maxSequenceNumber;
import static com.github.esiqveland.sandflake.SandflakeID.workerIDLength;

public class TimeBasedGenerator implements Generator {
    private final Clock clock;
    private final byte[] workerId;
    private final RandomSource randomGenerator;
    private final AtomicInteger sequenceNumber = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();

    // references the previously stored timestamp
    private long lastTimestamp = 0L;

    public TimeBasedGenerator() {
        this(Instant::now, new SecureRandom());
    }

    public TimeBasedGenerator(Clock clock, Random randomGenerator) {
        this(clock, randomGenerator::nextBytes, makeWorkerId(randomGenerator::nextBytes));
    }

    public TimeBasedGenerator(Clock clock, RandomSource randomGenerator, byte[] workerId) {
        Objects.requireNonNull(clock);
        Objects.requireNonNull(randomGenerator);
        Objects.requireNonNull(workerId);
        if (workerId.length != workerIDLength) {
            throw new IllegalArgumentException("workerId must have length " + workerIDLength);
        }
        this.clock = clock;
        this.randomGenerator = randomGenerator;
        this.workerId = workerId;
    }

    public interface Clock {
        Instant now();
    }

    public interface RandomSource {
        void fill(byte[] buf);
    }

    @Override
    public SandflakeID next() {
        try {
            lock.lock();
            return nextInternal();
        } finally {
            lock.unlock();
        }
    }

    private SandflakeID nextInternal() {
        Instant now = clock.now();
        long millis = now.toEpochMilli();

        int seq = sequenceNumber.incrementAndGet() % maxSequenceNumber;
        if (millis == lastTimestamp) {
            if (seq == 0) {
                // we wrapped around sequence, wait until next millisecond:
                millis = waitForMillis(lastTimestamp);
            }
        } else {
            seq = 0;
            sequenceNumber.set(0);
        }
        lastTimestamp = millis;

        byte[] rand = new byte[3];
        randomGenerator.fill(rand);

        return new SandflakeID(
                now,
                workerId,
                seq,
                rand
        );
    }

    // waitForMillis blocks until we have a new timestamp that is after the given lastTimestamp
    private long waitForMillis(long lastTimestamp) {
        long timestamp = clock.now().toEpochMilli();
        while (timestamp <= lastTimestamp) {
            timestamp = clock.now().toEpochMilli();
        }

        return timestamp;
    }

    // helper to allow calling this(...) in constructor
    private static byte[] makeWorkerId(RandomSource random) {
        byte[] workerId = new byte[workerIDLength];
        random.fill(workerId);
        return workerId;
    }

}
