package se.hannes.max;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.hannes.max.domain.Utbildningsinstans;
import se.hannes.max.protocol.utbildningsinstans.MessageHeaderDecoder;
import se.hannes.max.protocol.utbildningsinstans.MessageHeaderEncoder;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansDecoder;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

@SuppressWarnings("WeakerAccess")
public class ReadAndWriteTest {

    private static final String TMPFILE = "output";

    @Test
    @DisplayName("Read/Write from disk, two SMB messages of same typ")
    public void testReadAndWriteFromDisk() throws Exception {
        write();
        read();
        Files.delete(Paths.get(TMPFILE));
    }

    private void write() throws Exception
    {

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);
        int encodingLengthPlusHeader = writeUtbildningsinstans("ALG01Ä", 0, directBuffer);
        encodingLengthPlusHeader += writeUtbildningsinstans("XKARYÅ", encodingLengthPlusHeader, directBuffer);
        Path encoding = Paths.get(TMPFILE);
        try (FileChannel channel = FileChannel.open(encoding, CREATE, WRITE))
        {
            byteBuffer.limit(encodingLengthPlusHeader);
            channel.write(byteBuffer);
        }

    }

    private int writeUtbildningsinstans(String kod, int offset, UnsafeBuffer directBuffer) {
        UtbildninginstansEncoder utbildningsinstans = new UtbildninginstansEncoder();
        MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        utbildningsinstans.wrapAndApplyHeader(directBuffer, offset, headerEncoder)
                .version((short)offset)
                .kod(kod)
                .benamningSv("Algebra II ("+kod+")")
                .benamningEn("Algebra II ENG Ö");
        return utbildningsinstans.encodedLength() + headerEncoder.encodedLength();
    }

    private void read() throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        Path encoding = Paths.get(TMPFILE);
        try (FileChannel channel = FileChannel.open(encoding, READ))
        {
            channel.read(byteBuffer);
        }
        int offset = readUtbildningsinstans(directBuffer, 0);
        readUtbildningsinstans(directBuffer, offset);
    }


    private int readUtbildningsinstans(UnsafeBuffer directBuffer, int offset) {

        UtbildninginstansDecoder utbDecoder = new UtbildninginstansDecoder();
        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        headerDecoder.wrap(directBuffer, offset);
        final int actingBlockLength = headerDecoder.blockLength();
        final int actingVersion = headerDecoder.version();
        utbDecoder.wrap(directBuffer, offset + headerDecoder.encodedLength(), actingBlockLength, actingVersion);
        Utbildningsinstans utbildningsinstans = Utbildningsinstans.enUtbildningsinstans()
                .setVersion(utbDecoder.version())
                .setKod(utbDecoder.kod())
                .setBenämningSv(utbDecoder.benamningSv())
                .setBenämningEn(utbDecoder.benamningEn());
        System.out.println("utbildningsinstans = " + utbildningsinstans);
        return utbDecoder.limit();
    }

}
