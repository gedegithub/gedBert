import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainEmetteur {

    public static void main(String[] args) throws IOException, InterruptedException {

        Emetteur emitterClient = new Emetteur();

        System.out.println("\nStarting Emitter/Client ... \n& connecting to Receiver/Server using Go-Back-N \n");
        TimeUnit.SECONDS.sleep(2);

        emitterClient.startConnecting();
        Tram tram = new Tram('C');
        emitterClient.sendTram(tram);

        Tram tramInBuffer = new Tram(emitterClient.in.readLine());
        if (tramInBuffer.getType() == 'A') {
            System.out.println("Confirmation of Go-Back-N received from Receiver \n");
        }

        ArrayList<Tram> ListOfTram = emitterClient.readFile();

        emitterClient.sendFile(ListOfTram, 0);
        emitterClient.closeConnection();
    }
}