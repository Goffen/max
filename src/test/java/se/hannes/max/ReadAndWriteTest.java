package se.hannes.max;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import se.hannes.max.protocol.utbildningsinstans.MessageHeaderDecoder;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansDecoder;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

public class ReadAndWriteTest {

    @Test
    public void testReadAndWriteFromDisk() throws Exception {
        write();

        // 64Kbytes
        long heapSize = Runtime.getRuntime().totalMemory();

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        // Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("heapSize = " + heapSize);
        System.out.println("heapMaxSize = " + heapMaxSize);
        System.out.println("heapFreeSize = " + heapFreeSize);


        read();
    }

    private void write() throws Exception
    {

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2048);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
        int encodingLengthPlusHeader = writeUtbildningsinstans("ALG01", 0, directBuffer);
        // encodingLengthPlusHeader += writeUtbildningsinstans("XKARY", encodingLengthPlusHeader, directBuffer);
        Path encoding = Paths.get("output");
        try (FileChannel channel = FileChannel.open(encoding, CREATE, WRITE))
        {
            byteBuffer.limit(encodingLengthPlusHeader);
            channel.write(byteBuffer);
        }

    }

    private int writeUtbildningsinstans(String kod, int offset, UnsafeBuffer directBuffer) {
        UtbildninginstansEncoder utbildningsinstans = new UtbildninginstansEncoder();
        se.hannes.max.protocol.utbildningsinstans.MessageHeaderEncoder headerEncoder = new se.hannes.max.protocol.utbildningsinstans.MessageHeaderEncoder();
        utbildningsinstans.wrapAndApplyHeader(directBuffer, offset, headerEncoder)
                .version(42)
                .kod(kod)
                .benamningSv("Algebra II")
                .benamningEn("Algebra II ENG");
        return utbildningsinstans.encodedLength() + headerEncoder.encodedLength();
    }

    private void read() throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        Path encoding = Paths.get("output");
        try (FileChannel channel = FileChannel.open(encoding, READ))
        {
            // byteBuffer.limit(encodingLengthPlusHeader);
            channel.read(byteBuffer);
        }
        int offset = readUtbildningsinstans(directBuffer, 0);
        System.out.println("offset = " + offset);
        //offset = readUtbildningsinstans(directBuffer, offset);
        //System.out.println("offset = " + offset);
/*
        System.out.println("bufferOffset = " + bufferOffset);
        CAR_DECODER.wrap(directBuffer, bufferOffset + CAR_DECODER.encodedLength(), actingBlockLength, actingVersion);
        System.out.println(CAR_DECODER.toString());
*/
    }


    private int readUtbildningsinstans(UnsafeBuffer directBuffer, int offset) {

        UtbildninginstansDecoder utbDecoder = new UtbildninginstansDecoder();
        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        headerDecoder.wrap(directBuffer, offset);
        final int actingBlockLength = headerDecoder.blockLength();
        final int actingVersion = headerDecoder.version();
        utbDecoder.wrap(directBuffer, offset + headerDecoder.encodedLength(), actingBlockLength, actingVersion);
        System.out.println(utbDecoder.toString());
        return utbDecoder.limit();
    }

}
