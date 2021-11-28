import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Test {

    private Random random = new Random();

    //Change one bit of the tram to introduce and error which should be detected with the CRC
    String bitWiseError(String tram) {


        int tramLenght = tram.length();
        char[] modifiedTram = tram.toCharArray();
        int bitsToChange = random.nextInt(tramLenght / 2) + 2;
        int changedBit = 0;

        for (int i = 0; i < bitsToChange; i++) {

            changedBit = 8 + random.nextInt(tramLenght - 16);

            if (modifiedTram[changedBit] == '0') {
                modifiedTram[changedBit] = '1';
            } else {
                modifiedTram[changedBit] = '0';
            }

            System.out.println("Introducing bitwise error on the following tram");
            return String.valueOf(modifiedTram);

        }

        return "";
    }

    //Change a serie of bits of the tram to introduce and error which should be detected with the CRC
    String burstError(String tram, int burstLenght) {


        System.out.println("Introducing burst error on the following tram");

        Random random = new Random();
        int tramLenght = tram.length();
        char[] modifiedTram = tram.toCharArray();
        int startingBit = random.nextInt(tramLenght - burstLenght);

        for (int i = 0; i < burstLenght; i++) {

            if (modifiedTram[startingBit + i] == '0') {
                modifiedTram[startingBit + i] = '1';
            } else {
                modifiedTram[startingBit + i] = '0';
            }

        }

        return String.valueOf(modifiedTram);

    }

    //Delays the tram so that has to be sent twice, but both instances arrive to the receptor
    void delayTram() throws InterruptedException {

        System.out.println("Delaying tram for 5 seconds");
        TimeUnit.SECONDS.sleep(5);
    }

    boolean errorDecider(int probabilityThreshold) {

        return Math.random() * 10 < probabilityThreshold;
    }

}
