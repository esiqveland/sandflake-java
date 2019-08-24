# sandflake-java

Implementation of Sandflake IDs inspired by Twitter's Snowflake IDs.

This is java port of the Go library https://github.com/celrenheit/sandflake

## Features
 - 128 bit
 - Lexicographically sortable
 - Sequential (not guaranteed for future ids)
 - 1.21e+24 unique ids per millisecond
 - 2.81e+14 unique ids per worker per millisecond

## Design

128bit IDs:

- 48bit: timestamp
- 32bit: worker id (defaults to random)
- 24bit: sequence number
- 24bit: random number

## Usage

For now refer to the [test](https://github.com/esiqveland/sandflake-java/blob/master/src/test/java/com/github/esiqveland/sandflake/TimeBasedGeneratorTest.java#L15):

```java
TimeBasedGenerator generator = new TimeBasedGenerator();
SandflakeID id = generator.next();
SandflakeID next = generator.next();

assertThat(id).isNotEqualTo(next);
assertThat(SandflakeID.encode(id)).isNotEqualTo(SandflakeID.encode(next));
```


## Acknowledgements

To be compatible with the base32 alphabet used in https://github.com/celrenheit/sandflake,
I have copied the excellent base32 encoders from Google Guava project to be able to change the alphabet used and
still be compatible with the Go library.

Thanks to https://github.com/celrenheit/sandflake for the implementation this port is based on.
