package org.SQLiter.header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.SQLiter.config.Config;
import org.SQLiter.utils.FileManager;

public class Header {

    short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    short fileFormatVersion = Short.parseShort(Config.getProperty("fileFormatVersion"));
    Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));
    HeaderContent headerContent = new HeaderContent();
    Path dbFilePath = Paths.get("sqliter.db");

    /**
     *
     * @param dbName
     * Creates Database and Saves Bytecode to file
     */
    public void createDB(String dbName) {

        ByteBuffer dbHeader = ByteBuffer.allocate(pageSizeValue);

        byte[] dbHeaderBuffer = getDBHeader(); //16
        IO.println("DBHeader : " + new String(dbHeaderBuffer, StandardCharsets.UTF_8));

        byte[] pageSizeBuffer = ByteBuffer.allocate(2).putShort(pageSizeValue).array(); //18
        IO.println("Page size : " + ByteBuffer.wrap(pageSizeBuffer).getShort());

        byte[] fileFormatVersionBuffer = ByteBuffer.allocate(1).put((byte) fileFormatVersion).array(); // 19
        IO.println("File Format Version : " + ByteBuffer.wrap(fileFormatVersionBuffer).get());

        byte[] reservedBuffer = new byte[1]; // 20

        byte[] pageNumberBuffer = ByteBuffer.allocate(4).putInt(1).array(); // 24

        byte[] totalPageCount = ByteBuffer.allocate(4).putInt(1).array(); // For now only 1 page // 28

        byte[] reservedPaddingBuffer = new byte[12]; // 40

        dbHeader.put(ByteBuffer.allocate(4).putInt(44).array()); // Current 4 + 40 for header
        dbHeader.put(dbHeaderBuffer);
        dbHeader.put(pageSizeBuffer);
        dbHeader.put(fileFormatVersionBuffer);
        dbHeader.put(reservedBuffer);
        dbHeader.put(pageNumberBuffer);
        dbHeader.put(totalPageCount);
        dbHeader.put(reservedPaddingBuffer);

        IO.println(new String(dbHeader.array(), StandardCharsets.UTF_8));

        Path path = Paths.get("sqliter.db");

        // Create File
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {

            dbHeader.flip();
            channel.write(dbHeader);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        parseHeader(dbHeader.array());
    }

    /**
     * Parse Header and Print Header Value
     * @param db
     * @return
     */

    public Map<String, Object> parseHeader(byte[] db){
        IO.println("Header Length : " + db.length);
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("HEADER_BYTES_USED", ByteBuffer.wrap(readXBytes(db, 4, 0)).getInt());
        headerMap.put("SQL_NAME", new String(readXBytes(db, 16, 4), StandardCharsets.UTF_8));
        headerMap.put("PAGE_SIZE", ByteBuffer.wrap(readXBytes(db, 2, 20)).getShort());
        headerMap.put("FILE_FORMAT_VERSION", ByteBuffer.wrap(readXBytes(db, 1, 22)).get());
        headerMap.put("RESERVED_BUFFER", ByteBuffer.wrap(readXBytes(db, 1, 23)).get());
        headerMap.put("PAGE_NUMBER", ByteBuffer.wrap(readXBytes(db, 4, 24)).getShort());
        headerMap.put("TOTAL_PAGE_COUNT", ByteBuffer.wrap(readXBytes(db, 4, 28)).getInt());
        headerMap.put("RESERVED_PADDING_BUFFER", ByteBuffer.wrap(readXBytes(db, 12, 32)).getShort());
        IO.println(headerMap);
        return headerMap;
    }

    /**
     * Create a copy of X bytes from given Byte Array
     * @param byteArray
     * @param noOfBytes
     * @param startIdx
     * @return
     */
    byte[] readXBytes(byte[] byteArray, int noOfBytes, int startIdx){
        return Arrays.copyOfRange(byteArray, startIdx, startIdx+noOfBytes);
    }

    /**
     * Create DBHeader Content that contains SQLiter Version
     * @return
     */
    byte[] getDBHeader() {
        byte[] dbHeaderBuffer = "SQLiterUwU\0".getBytes(StandardCharsets.UTF_8);
        byte[] dbHeader = new byte[16];
        IO.println(dbHeaderBuffer.length);
        System.arraycopy(dbHeaderBuffer, 0, dbHeader, 0, Math.min(dbHeaderBuffer.length, dbHeader.length));
        return dbHeader;
    }

    /**
     * Creates Table, Reads DB Bytecode, Adds Table details to the header, Saves back to bytecode
     * @param tableName
     * @param tableDetails
     * @param headerBuffer
     */
    public void createTable(String tableName, String tableDetails){
        byte[] headerTableBuffer = headerContent.createTableForHeader(tableName, tableDetails);

//        Get ByteBuffer of DB File
        ByteBuffer dbFileBuffer = FileManager.readDBFile(dbFilePath);
        dbFileBuffer.position(0);

        int headerBytesFilled = ByteBuffer.wrap(readXBytes(dbFileBuffer.array(), 4, 0)).getInt();
        IO.println("Header Bytes : " + headerBytesFilled + " Buffer Size : " + dbFileBuffer.capacity());
        dbFileBuffer.position( headerBytesFilled-1);

//        Expand if needed
        dbFileBuffer = FileManager.expandBufferIfNeeded(dbFileBuffer, headerTableBuffer.length);

//        Write new table info
        dbFileBuffer.put(headerTableBuffer);

//        Update Filled Bytes Size
        updateBytesUsed(dbFileBuffer, 0, pageSizeValue, headerTableBuffer.length);

        FileManager.writeBufferToFile(dbFileBuffer, dbFilePath);

    }




    /**
     * For a given page update first 4 bytes representing bytes used. Needs to be called on each Write, Update
     * @param byteBuffer
     * @param pageNumber
     * @param pageSize
     * @param extraBytesUsed
     */
    public void updateBytesUsed(ByteBuffer byteBuffer, int pageNumber, int pageSize, int extraBytesUsed) {

        int position = pageNumber * pageSize;
        int curPageLen = ByteBuffer.wrap(readXBytes(byteBuffer.array(), 4, position)).getInt();
        IO.println("Current Page Len : " + curPageLen);

        int newPageLen = curPageLen + extraBytesUsed;

        // Update the same buffer
        updateByteBuffer(byteBuffer, position, ByteBuffer.allocate(4).putInt(newPageLen).array());

        // Read back updated value
        int updated = ByteBuffer.wrap(readXBytes(byteBuffer.array(), 4, position)).getInt();
        IO.println("Updated Len : " + updated);
    }


    /**
     * Update byteBuffer with new Byte Array value at a given index
     * @param byteBuffer
     * @param index
     * @param bytes
     */
    public void updateByteBuffer(ByteBuffer byteBuffer, int index, byte[] bytes){
        byteBuffer.position(index);
        byteBuffer.put(bytes);
    }



}
