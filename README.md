# SQLiter
Building SQLite from Scratch - Java. Lots of Bytecode!

First 4 Bytes in each page will tell how many bytes are occupied in that page

### Header Bytecode Format

| Offset | Hex Bytes                           | Description             |
|--------|-------------------------------------|-------------------------|
| 00–0F  | 53 51 4C 69 74 65 72 55 57 55 20 20 20 20 20 20 | "SQLiterUwU      "      |
| 10–11  | 10 00                               | Page size = 4096        |
| 12     | 01                                  | File format version     |
| 13     | 00                                  | Reserved buffer         |
| 14–17  | 00 00 00 00                         | Page number = 0         |
| 18–1B  | 00 00 00 01                         | Total page count = 1    |
| 1C–27  | 00 00 00 00 00 00 00 00 00 00 00 00 | Reserved padding buffer |

Total Size - 40 Bytes

### Handling Table Creation

When tables are created Bytecode array is create that contains

1. Table Name Length
2. Table Name
3. Reserved Bits (Will be used later)
4. Schema Index position
5. Page Address for data

## Handling Operation on .db file

Generally SQLite handling this byte using a complicated Page cache mechanism which FOR NOW is
out of scope. Instead we read db file in memory perform operation, Then Balance using B-Tree algorithm
and then do a write back. Not efficient but easier. Once the db size becomes large cache needs to be implemented