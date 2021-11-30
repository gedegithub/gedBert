import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainRecepteur {
    public static void main(String[] args) throws IOException, InterruptedException {


        System.out.println("\nStarting Receiver/server ...\n");
        Recepteur receiverServer = new Recepteur();

        System.out.println("Please enter an operating mode among the following: ");
        System.out.println("1 : Introduce bitwise errors to frames");
        System.out.println("2 : Introduce burst errors to frames");
        System.out.println("3 : Introduce delay in tram transmission");
        System.out.println("4 : Introduce tram loss in communication");
        System.out.println("Any other entry will lead to errorless execution");

        Scanner entry = new Scanner(System.in);
        String command = entry.nextLine();
        System.out.println(command);

        switch(command){

            case "1":
                receiverServer.setTest("BIT");
                System.out.println("Entering system with bitwise errors");
                break;

            case "2":
                receiverServer.setTest("BURST");
                System.out.println("Entering system with burst errors");
                break;

            case "3":
                receiverServer.setTest("DELAY");
                System.out.println("Entering system with delay in tram delivery");
                break;

            case "4":
                receiverServer.setTest("LOSS");
                System.out.println("Entering system with tram loss");
                break;

            default:
                System.out.println("Entering system in errorless mode");
                receiverServer.setTest("ERRORLESS");
                break;
        }


        receiverServer.startConnection(Integer.parseInt(args[0]));
        System.out.println("Receiver/Server started\n");
        receiverServer.listen();
    }
}
