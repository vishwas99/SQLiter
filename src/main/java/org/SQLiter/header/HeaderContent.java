package org.SQLiter.header;

import org.SQLiter.config.Config;

import java.nio.ByteBuffer;

public class HeaderContent {

    Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));
    Short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));

    public byte[] createTableForHeader(String tableName, String tableDetails){
//        Table Details and Page Links will be done later for now, PageNumber will be tableNumber

        short tableNameLength = (short) tableName.length();

        ByteBuffer headerContentBuffer = ByteBuffer.allocate(pageSizeValue - headerSize);
        headerContentBuffer.put(ByteBuffer.allocate(2).putShort(tableNameLength).array());

    }

}
