package org.SQLiter.core;

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
import org.SQLiter.pojo.PageChain;
import org.SQLiter.utils.FileManager;

public class Header {

    short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    short fileFormatVersion = Short.parseShort(Config.getProperty("fileFormatVersion"));
    short headerSize = Short.parseShort(Config.getProperty("headerSize"));
    HeaderContent headerContent = new HeaderContent();
    Path dbFilePath = Paths.get("sqliter.db");
    short bytesUsedSize = Short.parseShort(Config.getProperty("bytesUsedSize"));
    int nextPageNumberSize = Integer.parseInt(Config.getProperty("nextPageNumberSize"));
    /**
     *
     * @param dbName
     * Creates Database and Saves Bytecode to file
     */
    public void createDB(String dbName) {


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

        ByteBuffer dbHeader = ByteBuffer.allocate(headerSize + bytesUsedSize);
        dbHeader.put(ByteBuffer.allocate(bytesUsedSize).putShort(headerSize).array()); // Current 2 + 40 for header
        dbHeader.put(dbHeaderBuffer);
        dbHeader.put(pageSizeBuffer);
        dbHeader.put(fileFormatVersionBuffer);
        dbHeader.put(reservedBuffer);
        dbHeader.put(pageNumberBuffer);
        dbHeader.put(totalPageCount);
        dbHeader.put(reservedPaddingBuffer);

        IO.println(new String(dbHeader.array(), StandardCharsets.UTF_8));
        byte[] pagifiedDbHeader = DatabaseManager.pagify(dbHeader.array(), true);
        IO.println(Arrays.toString(pagifiedDbHeader));

        PageChain pageChain = new PageChain(pagifiedDbHeader, true, pageSizeValue, bytesUsedSize, nextPageNumberSize);
        IO.println("Header PageChain : " + pageChain.toString());

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
        int index = 0;
        headerMap.put("HEADER_BYTES_USED", ByteBuffer.wrap(FileManager.readXBytes(db, bytesUsedSize, index)).getShort());
        index += bytesUsedSize;
        headerMap.put("SQL_NAME", new String(FileManager.readXBytes(db, 16, index), StandardCharsets.UTF_8));
        index += 16;
        headerMap.put("PAGE_SIZE", ByteBuffer.wrap(FileManager.readXBytes(db, 2, index)).getShort());
        index += 2;
        headerMap.put("FILE_FORMAT_VERSION", ByteBuffer.wrap(FileManager.readXBytes(db, 1, index)).get());
        index += 1;
        headerMap.put("RESERVED_BUFFER", ByteBuffer.wrap(FileManager.readXBytes(db, 1, index)).get());
        index += 1;
        headerMap.put("PAGE_NUMBER", ByteBuffer.wrap(FileManager.readXBytes(db, 4, index)).getShort());
        index += 4;
        headerMap.put("TOTAL_PAGE_COUNT", ByteBuffer.wrap(FileManager.readXBytes(db, 4, index)).getInt());
        index += 4;
        headerMap.put("RESERVED_PADDING_BUFFER", ByteBuffer.wrap(FileManager.readXBytes(db, 12, index)).getShort());
        IO.println(headerMap);
        return headerMap;
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
     */
    public void createTable(String tableName, String tableDetails){
        byte[] headerTableBuffer = headerContent.createTableForHeader(tableName, tableDetails);

//        Get ByteBuffer of DB File
        ByteBuffer dbFileBuffer = FileManager.readDBFile(dbFilePath);
        dbFileBuffer.position(0);

        int headerBytesFilled = ByteBuffer.wrap(FileManager.readXBytes(dbFileBuffer.array(), bytesUsedSize, 0)).getShort();
        IO.println("Header Bytes : " + headerBytesFilled + " Buffer Size : " + dbFileBuffer.capacity());
        dbFileBuffer.position( headerBytesFilled-1);

//        Expand if needed
        dbFileBuffer = FileManager.expandBufferIfNeeded(dbFileBuffer, headerTableBuffer.length);

//        Write new table info
        dbFileBuffer.put(headerTableBuffer);

        IO.println("TableContent Len : " + headerTableBuffer.length);

//        Update Filled Bytes Size
        updateBytesUsed(dbFileBuffer, 0, pageSizeValue, headerTableBuffer.length);

        FileManager.writeBufferToFile(dbFileBuffer, dbFilePath);

    }




    /**
     * For a given page update first 2 bytes representing bytes used. Needs to be called on each Write, Update
     * @param byteBuffer
     * @param pageNumber
     * @param pageSize
     * @param extraBytesUsed
     */
    public void updateBytesUsed(ByteBuffer byteBuffer, int pageNumber, int pageSize, int extraBytesUsed) {

        int position = pageNumber * pageSize;
        int curPageLen = ByteBuffer.wrap(FileManager.readXBytes(byteBuffer.array(), bytesUsedSize, position)).getShort();
        IO.println("Current Page Len : " + curPageLen);

        int newPageLen = curPageLen + extraBytesUsed;

        // Update the same buffer
        updateByteBuffer(byteBuffer, position, ByteBuffer.allocate(bytesUsedSize).putShort((short)newPageLen).array());

        // Read back updated value
        int updated = ByteBuffer.wrap(FileManager.readXBytes(byteBuffer.array(), bytesUsedSize, position)).getShort();
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

    public ByteBuffer getDbBuffer(){
        return FileManager.readDBFile(dbFilePath);
    }

}
