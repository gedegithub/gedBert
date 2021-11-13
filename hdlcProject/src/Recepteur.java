import java.net.*;
import java.io.*;

class Recepteur {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int renduOu = 0;

    void start() throws IOException {
        serverSocket = new ServerSocket(6666);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    void listen() throws IOException {
        String inputLine;
        Tram frame;
        while ((inputLine = in.readLine()) != null)
        {
            if(inputLine.equals("next"))
            {
                System.out.println("\n\n");
                renduOu = 0;
                continue;
            }
            inputLine = bitUnstuff(inputLine);
            frame = new Tram(inputLine);

            processFrame(frame, inputLine);
        }
    }

    private void processFrame(Tram trame, String frameStr) throws IOException {
        char type = trame.getType();
        if (type == 'C')
        {
            System.out.println("Recu une trame de connection demandant go back N, va maintenant envoyer une reponse");
            out.println(genRR(0).formatFrameToSend());
        }

        else if (type == 'I')
        {
            verifyDataFrame(trame, frameStr);
        }

        else if (type == 'P')
        {
            System.out.println("recu demande P de la part du client.");
            Tram rr = genRR(renduOu);
            out.println(rr.formatFrameToSend());
            System.out.println("Envoie RR contenant " + rr.getNum()%8);

        }

        else if (type == 'F')
        {
            stop();
        }
    }

    private void verifyDataFrame(Tram trame, String frameStr) throws IOException {
        System.out.println("Recu du client: Trame avec num " + trame.getNum()%8 + " contenant : " +  trame.getData() );

        byte[] trameEnByteArray = Tram.getFrameToByteArray(frameStr);

        int nombreOctetsSansLesFlags = trameEnByteArray.length - 2;

        byte[] trameSansFlags = new byte[nombreOctetsSansLesFlags];
        System.arraycopy(trameEnByteArray, 1, trameSansFlags, 0, nombreOctetsSansLesFlags);

        int[] arrayIntAValider = Tram.byteArrToArr10(trameSansFlags);
        arrayIntAValider = Tram.divideByCRC(arrayIntAValider);

        boolean CRCwrong = false;
        for (int value : arrayIntAValider)
        {
            if (value == 1)
            {
                CRCwrong = true;
                break;
            }
        }

        if (CRCwrong)
        {
            System.out.println("Une erreur a ete detectee dans le CRC");
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Envoie Trame REJ " + renduOu % 8 + " au client " );
            String inputLine = in.readLine();
            inputLine = bitUnstuff(inputLine);
            Tram trameToErase = new Tram(inputLine);
            System.out.println("Recu du client: Trame avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );

            while (trameToErase.getNum() != renduOu)
            {
                inputLine = in.readLine();
                inputLine = bitUnstuff(inputLine);
                trameToErase = new Tram(inputLine);
                System.out.println("Recu du client: Trame avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );

            }
            renduOu++;
        }

        else if (!verifierNumCorrespondAuCompteur(trameEnByteArray[2], (byte)renduOu))
        {
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Le compteur a détecté une trame manquante");
            System.out.println("Envoie trame REJ " + renduOu % 8 + " au client " );

            String inputLine = in.readLine();
            inputLine = bitUnstuff(inputLine);
            Tram trameToErase = new Tram(inputLine);
            System.out.println("Recu du client: Trame avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );

            while (trameToErase.getNum() != renduOu)
            {
                inputLine = in.readLine();
                inputLine = bitUnstuff(inputLine);
                trameToErase = new Tram(inputLine);
                System.out.println("Recu du client: Trame avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );
            }
            renduOu++;
        }

        else {
            if((renduOu)%8 == 6) {
                System.out.println(" moment d'envoyer un RR ");
                out.println(genRR((renduOu+1) % 8).formatFrameToSend());
                System.out.println("Envoi Trame RR " + ((renduOu+1) % 8) + " au client ");
            }
            renduOu++;
        }
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    // returns a frame of type R => REJ and the num of the tram rejected
    private Tram genREJ(int num){
        return new Tram('R', num);
    }

    // returns a frame of type A => RR that confirms that Receiver received the tram number "num"
    private Tram genRR(int num){
        return new Tram('A', num);
    }

    private boolean verifierNumCorrespondAuCompteur(byte num, byte compteur){
        return num == compteur;
    }
    private String bitUnstuff(String frameString){
        int counter = 0;
        for(int i = 0; i<frameString.length(); i++){
            if(frameString.charAt(i)=='1') counter++;
            else if(frameString.charAt(i)=='0'){
                if(counter >= 5) frameString = charRm0At(frameString, i);
                counter=0;
            }
        }
        return frameString;
    }

    private static String charRm0At(String str, int p) {
        return str.substring(0, p) + str.substring(p + 1);
    }
}