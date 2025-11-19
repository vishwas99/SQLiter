package org.SQLiter.core;

import org.SQLiter.config.Config;
import org.SQLiter.utils.SequenceManager;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DatabaseManager {

    static final short pageSizeValue = Short.parseShort(Config.getProperty("pageSizeValue"));
    static final short fileFormatVersion = Short.parseShort(Config.getProperty("fileFormatVersion"));
    static final Integer headerSize = Integer.parseInt(Config.getProperty("headerSize"));
    static final Path dbFilePath = Paths.get("sqliter.db");
    static final int bytesUsedSize = Integer.parseInt(Config.getProperty("bytesUsedSize"));
    static final int nextPageSize = Integer.parseInt(Config.getProperty("nextPageNumberSize"));

    public static byte[] pagify(byte[] array, boolean isHeader){
        short curBytesUsed = ByteBuffer.wrap(array).getShort();

        IO.println("Raw Array : " + Arrays.toString(array));

        byte[] finalArray = new byte[0];

         if (curBytesUsed > pageSizeValue){
//            Slice byteArray from bytesUsedSize - 1 to pageSizeValue-2
             IO.println("Slice One : " + bytesUsedSize + " " + pageSizeValue);
            byte[] sliceOne = Arrays.copyOfRange(array, bytesUsedSize, pageSizeValue);
            IO.println("Slice One : " + Arrays.toString(sliceOne));
//            Call Pagify Helper to add BytesUsed and nextPage to the byteBuffer
            byte[] pagifiedSliceOne = pagifyHelper(sliceOne, pageSizeValue, true);
//            get Remaining array from pageSizeValue till endOfArray
            short remArrayBytesUsed = (short) (curBytesUsed - pageSizeValue);
            byte[] remArraysBytesUsedArr = ByteBuffer.allocate(bytesUsedSize).putShort(remArrayBytesUsed).array();
            byte[] remArray = Arrays.copyOfRange(array, pageSizeValue, array.length);
            ByteBuffer remArrayBuffer = ByteBuffer.allocate(remArray.length + bytesUsedSize + nextPageSize);
            remArrayBuffer.put(remArraysBytesUsedArr);
            remArrayBuffer.put(remArray);
//            Pagify this as well
            byte[] pagifiedRemArray = pagify(remArrayBuffer.array(), isHeader);
//            append result to first slice
            finalArray = new byte[pagifiedSliceOne.length + pagifiedRemArray.length];
            IO.println("Pagiefied Slice one : " + pagifiedSliceOne.length + " Pagified Slice two : " + pagifiedRemArray.length);
            System.arraycopy(pagifiedSliceOne, 0, finalArray, 0, pagifiedSliceOne.length);
            System.arraycopy(pagifiedRemArray, 0, finalArray, pagifiedSliceOne.length, pagifiedRemArray.length);
         } else {
             return pagifyHelper(Arrays.copyOfRange(array, bytesUsedSize, pageSizeValue), curBytesUsed, false);
         }
//        Return the new byte array
        return finalArray;
    }

    static byte[] pagifyHelper(byte[] array, short numberOfBytesUsed, boolean isIncomplete){
        ByteBuffer pagifiedBuffer = ByteBuffer.allocate(pageSizeValue + bytesUsedSize + nextPageSize);
        IO.println("Array Len :  " + array.length + " " + numberOfBytesUsed + " " + ByteBuffer.allocate(bytesUsedSize).putShort(numberOfBytesUsed).array().length);
        pagifiedBuffer.put(ByteBuffer.allocate(bytesUsedSize).putShort(numberOfBytesUsed).array());
        pagifiedBuffer.put(array);
        if(numberOfBytesUsed != pageSizeValue){
            pagifiedBuffer.put(new byte[pageSizeValue - numberOfBytesUsed]);
        }
        pagifiedBuffer.put(ByteBuffer.allocate(nextPageSize).putInt(isIncomplete ? SequenceManager.next("pageNumber") : -1));
        return pagifiedBuffer.array();
    }

}
