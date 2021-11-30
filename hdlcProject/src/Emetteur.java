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

    ArrayList<Tram> readFile(String file) throws FileNotFoundException {

        ArrayList<Tram> listOfTrams = new ArrayList<>();
        Scanner scanner = new Scanner(new File(file));
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
    void sendFile(ArrayList<Tram> listOfTrams) throws IOException, InterruptedException {
        int windowSize = 7;
        int tramCounter = 0;
        int i = 0;
        int broken = 0;


        System.out.println("Sending File using " + listOfTrams.size() + " trams");

        while (tramCounter <= listOfTrams.size() - 1) {
            while (windowSize > 0 && i < listOfTrams.size()) {
                sendTram(listOfTrams.get(i));

                System.out.println(
                        "Sending tram " + ((int) listOfTrams.get(i).getNum() % 8) +
                                " containing\t\t : " +
                                listOfTrams.get(i).getData());
                windowSize--;
                i++;
                broken = 0;
            }

            // wait for confirmation during 3 sec and after send pBit
            TimeUnit.SECONDS.sleep(3);

            // No reply from receiver
            while (!in.ready()) {
                System.out.println("No confirmation after time out of 3 sec, so send a ping tram P");
                sendTram(new Tram('P', 0));
                TimeUnit.SECONDS.sleep(3);
            }
            Tram replyFromReceiver = new Tram(in.readLine());
            char typeOfReply = replyFromReceiver.getType();

            //Confirmation RR that is nbr of last received tram + 1
            if (typeOfReply == 'A') {
                windowSize = Math.min(7, listOfTrams.size() - i);
                tramCounter = replyFromReceiver.getNum();

                System.out.println("Receiver replied RR " + replyFromReceiver.getNum() % 8);

                if(broken == 1 && i == listOfTrams.size()){
                    windowSize++;
                    broken = 0;
                    i--;
                }
                broken++;


            }

            // REJ response
            else {
                System.out.println("Receiver replied REJ " + replyFromReceiver.getNum() % 8);
                //Bring nbr of sent trams to the last non received tram
                int indexOfTramResent = replyFromReceiver.getNum();
                System.out.println("Emitter must resend trams from num: " + indexOfTramResent % 8);
                tramCounter = indexOfTramResent;
                i = indexOfTramResent;
                windowSize = 7 /*- i % 8*/;
            }
        }
        for (int j = 0; i < 10; i++) {
            sendTram(new Tram('F'));
        }

        System.out.println("Sending tram of closing connection");
        TimeUnit.SECONDS.sleep(3);
    }

    void startConnecting(int port) throws IOException {
        clientSocket = new Socket("localhost", port);
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

    void closeConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}