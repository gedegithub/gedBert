import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainRecepteur {
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Starting Receiver/server ...");
        TimeUnit.SECONDS.sleep(2);

        Recepteur receiverServer = new Recepteur();
        receiverServer.startConnection();
        System.out.println("Receiver/Server started");
        receiverServer.listen();
    }
}
