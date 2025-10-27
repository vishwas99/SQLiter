package org.SQLiter.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileManager {

    /**
     * Takes path including fileName and writes byte array at a position
     * @param path
     * @param index
     * @param newData
     */
    public static void writeToDBFile(Path path, int index, byte[] newData){
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.position(index); // move to the offset
            ByteBuffer buffer = ByteBuffer.wrap(newData);
            channel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer readDBFile(Path path){
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeBufferToFile(ByteBuffer buffer, Path path){
        IO.println("Writing Buffer to File of capacity : " + buffer.capacity());
        buffer.position(0);

        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE,           // create if it doesn’t exist
                StandardOpenOption.WRITE,            // open for writing
                StandardOpenOption.TRUNCATE_EXISTING // replace existing content
        )) {
            int noOfBytesWritten = channel.write(buffer);
            IO.println("No of Bytes Written : " + noOfBytesWritten);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IO.println("✅ Replaced file contents: " + path.toAbsolutePath());
    }

    public static ByteBuffer expandBufferIfNeeded(ByteBuffer oldBuffer, int requiredExtraBytes) {
        // If enough room already, no need to expand
        if (oldBuffer.remaining() >= requiredExtraBytes) {
            return oldBuffer;
        }

        int newCapacity = oldBuffer.position() + requiredExtraBytes;

        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);

        oldBuffer.flip();            // reset to beginning for read
        newBuffer.put(oldBuffer);    // copy all old data

        IO.println("Buffer expanded: " + oldBuffer.capacity() + " → " + newBuffer.capacity());
        return newBuffer;
    }

}
