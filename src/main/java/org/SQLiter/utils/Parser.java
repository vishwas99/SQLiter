package org.SQLiter.utils;

import org.SQLiter.config.Config;
import org.SQLiter.core.HeaderContent;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Parser {

    private static short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    private static short fileFormatVersion = Short.parseShort(Config.getProperty("fileFormatVersion"));
    private static short headerSize = Short.parseShort(Config.getProperty("headerSize"));
    private static HeaderContent headerContent = new HeaderContent();
    private static int nextPageNumberSize = Integer.parseInt(Config.getProperty("nextPageNumberSize"));
    private static Path dbFilePath = Paths.get("sqliter.db");

    public static short getNumberOfUsedBytes(byte[] bytes){
        if((short)bytes.length != pageSizeValue){
            throw new IllegalArgumentException("Only Full Page must be sent");
        }
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int getNextPage(byte[] bytes){
        if((short) bytes.length != pageSizeValue){
            throw new IllegalArgumentException("Only Full page must be sent");
        }
        byte[] nextPageArray = Arrays.copyOfRange(bytes, pageSizeValue-nextPageNumberSize, 4);
        return ByteBuffer.wrap(nextPageArray).getInt();
    }

}
