package se.hannes.max;

import org.agrona.concurrent.UnsafeBuffer;

import se.hannes.max.protocol.car.*;
import se.hannes.max.protocol.utbildningsinstans.UtbildninginstansEncoder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class WriteCar {

    public static void main(String[] args) throws Exception
    {

        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2048);
        final UnsafeBuffer directBuffer = new UnsafeBuffer(byteBuffer);

        int encodingLengthPlusHeader = writeCar(0, directBuffer);
        encodingLengthPlusHeader += writeUtbildningsinstans("ALG01", encodingLengthPlusHeader, directBuffer);
        encodingLengthPlusHeader += writeUtbildningsinstans("XKARY", encodingLengthPlusHeader, directBuffer);
        Path encoding = Paths.get("output");
        try (FileChannel channel = FileChannel.open(encoding, CREATE, WRITE))
        {
            byteBuffer.limit(encodingLengthPlusHeader);
            channel.write(byteBuffer);
        }

    }

    private static int writeUtbildningsinstans(String kod, int offset, UnsafeBuffer directBuffer) {
        UtbildninginstansEncoder utbildningsinstans = new UtbildninginstansEncoder();
        se.hannes.max.protocol.utbildningsinstans.MessageHeaderEncoder headerEncoder = new se.hannes.max.protocol.utbildningsinstans.MessageHeaderEncoder();
        utbildningsinstans.wrapAndApplyHeader(directBuffer, offset, headerEncoder)
               .version(42)
                .kod(kod)
                .benamningSv("Algebra II")
                .benamningEn("Algebra II ENG");

        System.out.println("utbildningsinstans.encodedLength() = " + utbildningsinstans.limit());
        return utbildningsinstans.encodedLength() + headerEncoder.encodedLength();

    }

    private static int writeCar(int offset, UnsafeBuffer directBuffer) throws UnsupportedEncodingException {
        CarEncoder car = new CarEncoder();
        car.wrapAndApplyHeader(directBuffer, offset, new MessageHeaderEncoder())
                .serialNumber(offset)
                .modelYear(2013)
                .available(BooleanType.T)
                .code(Model.A)
                .putVehicleCode("abcdef".getBytes(CarEncoder.vehicleCodeCharacterEncoding()), 0);

        for (int i = 0, size = CarEncoder.someNumbersLength(); i < size; i++)
        {
            car.someNumbers(i, i);
        }

        car.extras()
                .clear()
                .cruiseControl(true)
                .sportsPack(true)
                .sunRoof(false);

        car.engine()
                .capacity(2000)
                .numCylinders((short)4)
                .putManufacturerCode("123".getBytes(EngineEncoder.manufacturerCodeCharacterEncoding()), 0);

        car.fuelFiguresCount(3)
                .next().speed(30).mpg(35.9f)
                .next().speed(55).mpg(49.0f)
                .next().speed(75).mpg(40.0f);

        final CarEncoder.PerformanceFiguresEncoder figures = car.performanceFiguresCount(2);
        figures.next()
                .octaneRating((short)95)
                .accelerationCount(3)
                .next().mph(30).seconds(4.0f)
                .next().mph(40).seconds(7.5f)
                .next().mph(100).seconds(12.2f);
        figures.next()
                .octaneRating((short)99)
                .accelerationCount(3)
                .next().mph(10).seconds(3.8f)
                .next().mph(60).seconds(7.1f)
                .next().mph(100).seconds(11.8f);

        // An exception will be raised if the string length is larger than can be encoded in the varDataEncoding field
        // Please use a suitable schema type for varDataEncoding.length: uint8 <= 254, uint16 <= 65534
        String manufacturerEncoding = CarEncoder.manufacturerCharacterEncoding();
        car.manufacturer(new String("Honda".getBytes(manufacturerEncoding), Charset.forName(manufacturerEncoding)));
        String modelEncoding = CarEncoder.modelCharacterEncoding();
        car.model(new String("Civic Ölkorv".getBytes(modelEncoding), Charset.forName(modelEncoding)));

        System.out.println("car.encodedLength() = " + car.encodedLength());
        return MessageHeaderEncoder.ENCODED_LENGTH + car.encodedLength();


    }
}
