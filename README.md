# SQLiter
Building SQLite from Scratch - Java. Lots of Bytecode!

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