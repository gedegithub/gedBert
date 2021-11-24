import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/* *  Emetteur = Client
 */
class Emetteur {
    private Socket clientSocket;
    private PrintWriter out;
    BufferedReader in;

// Constructor
    Emetteur() {
    }

    ArrayList<Tram> readFile() throws FileNotFoundException {

        ArrayList<Tram> listOfTrams = new ArrayList<>();
        Scanner scanner = new Scanner(new File("./file.txt"));
        int lineNumber = 0;

        Character type = 'I';

        while (scanner.hasNextLine()) {
            Tram tram = new Tram(type, lineNumber++, scanner.nextLine());
            listOfTrams.add(tram);
        }
        return listOfTrams;
    }

/* *  n = 3   ===> Sequence number coded using 3 bits
************  ===> window size = 2^n -1 = 7 in Go-Back-N method
************  ===> num Tram: 0, 1, 2, 3, 4, 5, 6, 7
*/    
    void sendFile(ArrayList<Tram> listOfTrams, int choice) throws IOException, InterruptedException {
        int windowSize = 7;
        int tramCounter = 0;
        int i = 0;

        boolean notYetSabotaged = true;

        System.out.println("Sending File using " + listOfTrams.size() + " trams\n");

        while (tramCounter < listOfTrams.size() - 1) {
            while (windowSize > 0 && i < listOfTrams.size()) {
                if (choice == 2 && i == listOfTrams.size() - 2 && notYetSabotaged) {
                    System.out.println(" ( skip tram " + i % 8 + " having " + listOfTrams.get(i).getData() + " for testing )");
                    // do not send tram
                    notYetSabotaged = false;
                } else if (choice == 3 && i == listOfTrams.size() - 2 && notYetSabotaged) {
                    System.out.println(" ( sabotage tram crc " + i % 8 + " having " + listOfTrams.get(i).getData() + " for testing )");
                    // do not send tram
                    sendTramBadCRC(listOfTrams.get(i));
                    notYetSabotaged = false;
                } else {
                    sendTram(listOfTrams.get(i));
                }

                System.out.println(
                        "Sending tram " + ((int) listOfTrams.get(i).getNum() % 8) +
                                " containing\t\t : " +
                                listOfTrams.get(i).getData());
                windowSize--;
                i++;
            }

            // wait for confirmation during 3 sec and after send pBit
            TimeUnit.SECONDS.sleep(3);
            if (choice == 4 && tramCounter == 6)
                in.readLine();

            // No reply from receiver
            if (!in.ready()) {
                System.out.println("\nNo confirmation after time out of 3 sec, so send a ping tram P\n");
                sendTram(new Tram('P', 0));
            }
            Tram replyFromReceiver = new Tram(in.readLine());
            char typeOfReply = replyFromReceiver.getType();

            //Confirmation RR that is nbr of last received tram + 1
            if (typeOfReply == 'A') {
                windowSize = Math.min(7, listOfTrams.size() - i);
                tramCounter = replyFromReceiver.getNum();

                System.out.println("Receiver replied RR with num: " + replyFromReceiver.getNum() % 8);

            }

            // REJ response
            else {
                System.out.println("Receiver replied REJ with num: " + replyFromReceiver.getNum() % 8);
                //Bring nbr of sent trams to the last non received tram
                int indexOfTramResent = replyFromReceiver.getNum();
                System.out.println("Receiver must resend trams from num: " + indexOfTramResent % 8);
                tramCounter = indexOfTramResent;
                i = indexOfTramResent;
                windowSize = 7 - i % 8;
            }
        }
    }

    void startConnecting() throws IOException {
        clientSocket = new Socket("localhost", 8082);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    void sendTram(Tram tram) {

        String formattedTram = tram.formatTramToSend();
        formattedTram = bitStuff(formattedTram);

        // Write tram in buffer as a string
        out.println(formattedTram);
    }

    private String bitStuff(String str) {
        int counter = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '1') {
                counter++;
                if (counter == 5) {
                    str = charAdd0At(str, i + 1);
                    counter = 0;
                }
            } else if (str.charAt(i) == '0') {
                counter = 0;
            }
        }
        return str;
    }

    private static String charAdd0At(String string, int i) {
        return string.substring(0, i) + '0' + string.substring(i);
    }

    private void sendTramBadCRC(Tram tram) {

        String formattedTramToSend = tram.formatTramToSend();
        byte[] frameBytes = Tram.stringToByte(formattedTramToSend);

        frameBytes[frameBytes.length - 2] = (byte) (~frameBytes[frameBytes.length - 2]);

        formattedTramToSend = Tram.arr10ToString(Tram.byteArrToArr10(frameBytes));
        formattedTramToSend = bitStuff(formattedTramToSend);

        out.println(formattedTramToSend);
    }

    void closeConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}