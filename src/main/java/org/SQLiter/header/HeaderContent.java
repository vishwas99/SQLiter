package org.SQLiter.header;

import org.SQLiter.config.Config;
import org.SQLiter.utils.FileManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HeaderContent {

    Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));
    Short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    int tableNumber = 1;
    Path dbFilePath = Paths.get("sqliter.db");


    public byte[] createTableForHeader(String tableName, String tableDetails) {
        // Table name length (in characters)
        short tableNameLength = (short) tableName.length();

        // --- Build individual byte arrays ---
        byte[] tableNameLengthBytes = ByteBuffer.allocate(2)
                .putShort(tableNameLength)
                .array();

        byte[] tableNameBytes = tableName.getBytes(StandardCharsets.UTF_8);

        // Should be pageNumber pointing to table details, for now using tableNumber
        byte[] tableNumberBytes = ByteBuffer.allocate(2)
                .putShort((short) tableNumber)
                .array();

        // --- Combine all parts ---
        int totalSize = tableNameLengthBytes.length + tableNameBytes.length + tableNumberBytes.length;
        ByteBuffer finalBuffer = ByteBuffer.allocate(totalSize);

        finalBuffer.put(tableNameLengthBytes);
        finalBuffer.put(tableNameBytes);
        finalBuffer.put(tableNumberBytes);

        IO.println(tableName + " " + totalSize);

        return finalBuffer.array();
    }


    public void parseAndGetAllTables(){
        ByteBuffer headerBuffer = getDbBuffer();
        IO.println(headerBuffer.capacity() + " " + headerBuffer.array().length);
        int totalHeaderBytesUsed = ByteBuffer.wrap(FileManager.readXBytes(headerBuffer.array(), 4, 0)).getInt();
//        Skip first 44 bytes in header -- Bytes Used + Header
        int pos = 43; // Ignore 44 items
        IO.println("Current Index : " + pos + " totalUsedBytes : " + totalHeaderBytesUsed);
        IO.println("_________________TABLES_____________");
        while(pos < totalHeaderBytesUsed-1){ // -1 since we read till x-1 index position for x elements
            short curTableLen = ByteBuffer.wrap(FileManager.readXBytes(headerBuffer.array(), 2, pos)).getShort();
            String curTableName = new String(FileManager.readXBytes(headerBuffer.array(), curTableLen, pos + 2), StandardCharsets.UTF_8);
            short curTablePageNumber =  ByteBuffer.wrap(FileManager.readXBytes(headerBuffer.array(), 2, pos + 2 + curTableLen)).getShort();
            IO.println(curTableLen + " | " + curTableName + " | " + curTablePageNumber);
            pos += 2 + curTableLen + 2; // 2 + tableName + 2 bytes read
        }
    }

    public ByteBuffer getDbBuffer(){
        return FileManager.readDBFile(dbFilePath);
    }

}
