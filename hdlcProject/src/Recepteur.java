import java.net.*;
import java.io.*;

class Recepteur {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int at = 0;

    void startConnection() throws IOException {
        serverSocket = new ServerSocket(8082);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    void listen() throws IOException {
        String inputLine;
        Tram tram;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.equals("next")) {
                System.out.println("\n\n");
                at = 0;
                continue;
            }
            inputLine = bitUnStuff(inputLine);
            tram = new Tram(inputLine);

            processTram(tram, inputLine);
        }
    }

    private void processTram(Tram tram, String trameStr) throws IOException {
        char type = tram.getType();
        if (type == 'C') {
            System.out.println("Acknowledge receipt of a Go-Back-N tram");
            out.println(createRR(0).formatTramToSend());
        } else if (type == 'I') {
            verifyDataTram(tram, trameStr);
        } else if (type == 'P') {
            System.out.println("Ping received from Emitter");
            Tram rr = createRR(at);
            out.println(rr.formatTramToSend());
            System.out.println("Sending RR having " + rr.getNum() % 8);

        } else if (type == 'F') {
            stop();
        }
    }

    private void verifyDataTram(Tram tram, String tramStr) throws IOException {

        System.out.println("Received from Emitter/Client: Tram num " + tram.getNum() % 8 + " whose content is : " + tram.getData());

        byte[] tram2ByteArray = Tram.getFrameToByteArray(tramStr);
        int nbrOctetsWithoutFlags = tram2ByteArray.length - 2;
        byte[] tramWithoutFlags = new byte[nbrOctetsWithoutFlags];

        System.arraycopy(tram2ByteArray, 1, tramWithoutFlags, 0, nbrOctetsWithoutFlags);

        int[] arrayIntToValidate = Tram.byteArrToArr10(tramWithoutFlags);
        arrayIntToValidate = Tram.divideByCRC(arrayIntToValidate);

        boolean CRCerror = false;
        for (int value : arrayIntToValidate) {
            if (value == 1) {
                CRCerror = true;
                break;
            }
        }

        if (CRCerror) {
            System.out.println("CRC error detected");
            out.println(createREJ(at).formatTramToSend());
            System.out.println("Send REJ tram " + at % 8 + " to Emitter/Client ");
            readLine();
        } else if (!compareNum2Counter(tram2ByteArray[2], (byte) at)) {
            out.println(createREJ(at).formatTramToSend());
            System.out.println("Missing Tram detected");
            System.out.println("Send REJ tram " + at % 8 + " to Emitter/Client ");

            readLine();
        } else {
            if ((at) % 8 == 6) {
                System.out.println(" Time to send RR ");
                out.println(createRR((at + 1) % 8).formatTramToSend());
                System.out.println("Send RR Tram " + ((at + 1) % 8) + " to Emitter/Client ");
            }
            at++;
        }
    }

    private void readLine() throws IOException {
        String inputLine = in.readLine();
        inputLine = bitUnStuff(inputLine);
        Tram tramToErase = new Tram(inputLine);
        System.out.println("Received from Emitter/Client: Tram num " + tramToErase.getNum() % 8 + " containing : " + tramToErase.getData());

        while (tramToErase.getNum() != at) {
            inputLine = in.readLine();
            inputLine = bitUnStuff(inputLine);
            tramToErase = new Tram(inputLine);
            System.out.println("Received from Emitter/Client: Tram num " + tramToErase.getNum() % 8 + " containing : " + tramToErase.getData());

        }
        at++;
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    // create a tram of type REJ with its num
    private Tram createREJ(int num) {
        return new Tram('R', num);
    }

    // create a tram of type RR with its num
    private Tram createRR(int num) {
        return new Tram('A', num);
    }

    private boolean compareNum2Counter(byte num, byte counter) {
        return num == counter;
    }

    private String bitUnStuff(String trameString) {
        int counter = 0;
        for (int i = 0; i < trameString.length(); i++) {
            if (trameString.charAt(i) == '1') counter++;
            else if (trameString.charAt(i) == '0') {
                if (counter >= 5) trameString = charRm0At(trameString, i);
                counter = 0;
            }
        }
        return trameString;
    }

    private static String charRm0At(String str, int p) {
        return str.substring(0, p) + str.substring(p + 1);
    }
}