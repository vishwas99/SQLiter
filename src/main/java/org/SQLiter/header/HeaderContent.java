package org.SQLiter.header;

import org.SQLiter.config.Config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class HeaderContent {

    Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));
    Short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    int tableNumber = 1;

    public byte[] createTableForHeader(String tableName, String tableDetails){
//        Table Details and Page Links will be done later for now, PageNumber will be tableNumber
        short tableNameLength = (short) tableName.length();

        ByteBuffer headerContentBuffer = ByteBuffer.allocate(pageSizeValue - headerSize);
//        Table Name Length
        headerContentBuffer.put(ByteBuffer.allocate(2).putShort(tableNameLength).array());
//        Table Name
        headerContentBuffer.put(tableName.getBytes(StandardCharsets.UTF_8));
//        Should be pageNumber pointing to table details, For now tableNumber
        headerContentBuffer.put(ByteBuffer.allocate(2).putShort((short) tableNumber).array());

        return headerContentBuffer.array();
    }

}
