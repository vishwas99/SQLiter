package org.SQLiter.header;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.SQLiter.config.Config;

public class Header {

    short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    short fileFormatVersion = Short.parseShort(Config.getProperty("fileFormatVersion"));
    Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));

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

        dbHeader.put(dbHeaderBuffer);
        dbHeader.put(pageSizeBuffer);
        dbHeader.put(fileFormatVersionBuffer);
        dbHeader.put(reservedBuffer);
        dbHeader.put(pageNumberBuffer);
        dbHeader.put(totalPageCount);
        dbHeader.put(reservedPaddingBuffer);

        IO.println(new String(dbHeader.array(), StandardCharsets.UTF_8));

        parseHeader(dbHeader.array());
    }
    public Map<String, Object> parseHeader(byte[] db){
        IO.println("Header Length : " + db.length);
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("SQL_NAME", new String(readXBytes(db, 16, 0), StandardCharsets.UTF_8));
        headerMap.put("PAGE_SIZE", ByteBuffer.wrap(readXBytes(db, 2, 16)).getShort());
        headerMap.put("FILE_FORMAT_VERSION", ByteBuffer.wrap(readXBytes(db, 1, 18)).get());
        headerMap.put("RESERVED_BUFFER", ByteBuffer.wrap(readXBytes(db, 1, 19)).get());
        headerMap.put("PAGE_NUMBER", ByteBuffer.wrap(readXBytes(db, 4, 20)).getShort());
        headerMap.put("TOTAL_PAGE_COUNT", ByteBuffer.wrap(readXBytes(db, 4, 24)).getInt());
        headerMap.put("RESERVED_PADDING_BUFFER", ByteBuffer.wrap(readXBytes(db, 12, 28)).getShort());
        IO.println(headerMap);
        return headerMap;
    }

    byte[] readXBytes(byte[] byteArray, int noOfBytes, int startIdx){
        return Arrays.copyOfRange(byteArray, startIdx, startIdx+noOfBytes);
    }

    byte[] getDBHeader() {
        byte[] dbHeaderBuffer = "SQLiterUwU\0".getBytes(StandardCharsets.UTF_8);
        byte[] dbHeader = new byte[16];
        IO.println(dbHeaderBuffer.length);
        System.arraycopy(dbHeaderBuffer, 0, dbHeader, 0, Math.min(dbHeaderBuffer.length, dbHeader.length));
        return dbHeader;
    }

}
