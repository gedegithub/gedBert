import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainEmetteur {

    public static void main(String[] args) throws IOException, InterruptedException {


        if(args[3].compareTo("0") != 0){
            System.out.println("Communication type other than Go-Back-N aren't supported. Please enter 0 as parameter to useGo-Back-N.");
            return;
        }

        Emetteur emitterClient = new Emetteur();

        System.out.println("\nStarting Emitter/Client ... \n& connecting to Receiver/Server using Go-Back-N \n");

        emitterClient.startConnecting(Integer.parseInt(args[1]));


        Tram tram = new Tram('C');

        while(!emitterClient.in.ready()){
            emitterClient.sendTram(tram);
            TimeUnit.SECONDS.sleep(5);
        }
        Tram tramInBuffer = new Tram(emitterClient.in.readLine());
        if (tramInBuffer.getType() == 'A') {
            System.out.println("Confirmation of Go-Back-N received from Receiver \n");
        }else{
            System.out.println("Connection establishment tram was lost");
        }
        ArrayList<Tram> ListOfTram = emitterClient.readFile(args[2]);

        emitterClient.sendFile(ListOfTram);
        emitterClient.closeConnection();
    }
}