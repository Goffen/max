package se.hannes.max;

import org.agrona.concurrent.UnsafeBuffer;
import se.hannes.max.protocol.car.CarDecoder;
import se.hannes.max.protocol.car.CarMessageHeaderDecoder;
import se.hannes.max.protocol.utbildningsinstans.MessageHeaderDecoder;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansDecoder;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.READ;

public class ReadCar {

    public static void main(String[] args) throws Exception
    {

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        Path encoding = Paths.get("output");
        try (FileChannel channel = FileChannel.open(encoding, READ))
        {
            // byteBuffer.limit(encodingLengthPlusHeader);
            channel.read(byteBuffer);
        }

        int offset = readCar(directBuffer, 0);
        System.out.println("offset = " + offset);
        offset = readUtbildningsinstans(directBuffer, offset);
        System.out.println("offset = " + offset);
        offset = readUtbildningsinstans(directBuffer, offset);
        System.out.println("offset = " + offset);
/*
        System.out.println("bufferOffset = " + bufferOffset);
        CAR_DECODER.wrap(directBuffer, bufferOffset + CAR_DECODER.encodedLength(), actingBlockLength, actingVersion);
        System.out.println(CAR_DECODER.toString());
*/
    }

    private static int readUtbildningsinstans(UnsafeBuffer directBuffer, int offset) {

        UtbildninginstansDecoder utbDecoder = new UtbildninginstansDecoder();
        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        headerDecoder.wrap(directBuffer, offset);
        final int actingBlockLength = headerDecoder.blockLength();
        final int actingVersion = headerDecoder.version();
        utbDecoder.wrap(directBuffer, offset + headerDecoder.encodedLength(), actingBlockLength, actingVersion);
        System.out.println(utbDecoder.toString());
        return utbDecoder.limit();
    }

    private static int readCar(UnsafeBuffer directBuffer, int offset) {
        CarDecoder carDecoder = new CarDecoder();
        CarMessageHeaderDecoder carMessageDecoder = new CarMessageHeaderDecoder();
        carMessageDecoder.wrap(directBuffer, offset);

        // Lookup the applicable flyweight to decode this type of message based on templateId and version.
        final int actingBlockLength = carMessageDecoder.blockLength();
        final int actingVersion = carMessageDecoder.version();

        carDecoder.wrap(directBuffer, offset + carMessageDecoder.encodedLength(), actingBlockLength, actingVersion);
        System.out.println(carDecoder.toString());
        return carDecoder.limit();
    }


}
