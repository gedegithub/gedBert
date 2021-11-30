import java.util.ArrayList;
import java.util.Arrays;

/* HDLC Protocol
       -----------------------------------------
*  Tram: | Flag | Type | Num | Data | CRC | Flag |
**       -----------------------------------------
   flag : 1 octet (01111110)
   type : 'I' , 'C' , 'A', 'R', 'F', 'P'
   num  : tram number  or the number of confirmation tram (RR,REJ) over 1 octet
   data : variable size for I tram only, other trams contain no data
   CRC  : contains the checksum calculated (on Type+Num+Data) using CRC over 2 octets

   References: https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html;
               StackOverflow
 */
class Tram {

    private char type;
    private byte num;
    private String data;
    private static final int[] CRC = {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};

    //constructor for data tram
    Tram(Character type, int number, String line) {

        this.type = type;
        this.num = (byte) number;
        this.data = line;
    }

    // constructor for Go-Back-N Tram
    Tram(char type) {
        this.type = type;
        // in connexion request, num=0 => Go-Back-N
        if (type == 'C') {
            this.num = 0b00000000; // num= 0 => for Go-Back-N is agreed upon.
        }
    }

    // constructor for RR && REJ Trams
    Tram(char type, int num) {
        this.type = type;
        this.num = (byte) num;
    }

    // constructing Tram from a byteArray
    Tram(String frameString) {
        byte[] frameBytes = stringToByte(frameString);

        this.type = (char) frameBytes[1];
        this.num = frameBytes[2];
        if (this.type == 'I') {
            this.data = new String(Arrays.copyOfRange(frameBytes, 3, frameBytes.length - 3));
        }
    }

    // Convert tram to byteArrayList b4 sending it through the socket.
    String formatTramToSend() {
        // for CRC
        byte[] arrayOfByte;
        byte[] arrayCRC;

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //Adding flag, type and num for each Tram
        byte flag = 0b01111110;
        byteArrayList.add(flag);
        byteArrayList.add((byte) this.type);
        byteArrayList.add(this.num);

        if (this.type == 'I') {
            arrayOfByte = data.getBytes();
            arrayCRC = new byte[arrayOfByte.length + 2];
            // byte array for the crc
            arrayCRC[0] = (byte) this.type;
            arrayCRC[1] = this.num;

            // add bytes of the data to CRC byte array
            System.arraycopy(arrayOfByte, 0, arrayCRC, 2, arrayOfByte.length);

            // to complete byteArrayList
            for (byte b : arrayOfByte) {
                byteArrayList.add(b);
            }

        } else {
            arrayCRC = new byte[2];
            arrayCRC[0] = (byte) this.type;
            arrayCRC[1] = this.num;
        }

        int[] dataCRC = byteArrToArr10(arrayCRC);

        int[] CRCresult = divideByCRC(dataCRC);
        // the CRC result is added to the byteArrayList
        ArrayList<Byte> convertedCRCResult = convertToByteArrayList(CRCresult);

        byteArrayList.addAll(convertedCRCResult);
        // the last byte flag is added
        byteArrayList.add(flag);

        // we convert the arraylist into an array
        byte[] result = new byte[byteArrayList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = byteArrayList.get(i);
        }

        return arr10ToString(byteArrToArr10(result));
    }

    static byte[] getFrameToByteArray(String frameStr) {
        return stringToByte(frameStr);
    }

    static int[] byteArrToArr10(byte[] bytes) {

        ArrayList<Integer> arrayOfBits = new ArrayList<>();

        // for each array of bytes store every individual byte in the array list in the correct order
        for (int i = bytes.length - 1; i >= 0; i--) {
            int bits = bytes[i];

            for (int j = 0; j < 8; j++) {
                arrayOfBits.add(bits & 1); // AND operation
                bits >>= 1; // now evaluate next one
            }
        }
        // convert the arrayList of Int to int array
        int[] intArr = new int[arrayOfBits.size()];
        for (int i = 0; i < arrayOfBits.size(); i++) {
            intArr[i] = arrayOfBits.get(arrayOfBits.size() - i - 1);
        }
        return intArr;
    }

    static String arr10ToString(int[] fram10) {
        StringBuilder result = new StringBuilder();
        for (int value : fram10) {
            result.append(value);
        }
        return result.toString();
    }

    static byte[] stringToByte(String intArr) {
        byte[] result = new byte[intArr.length() / 8];
        for (int i = 0; i < intArr.length(); i += 8) {
            long la = Long.parseLong(intArr.substring(i, i + 8), 2);
            result[i / 8] = (byte) la;
        }
        return result;
    }

    private static ArrayList<Byte> convertToByteArrayList(int[] intArr) {

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        byte[] temp = stringToByte(arr10ToString(intArr));
        for (byte b : temp) {
            byteArrayList.add(b);
        }
        return byteArrayList;
    }

    private static int[] bitshift(int[] ints) {
        if (ints.length == 1) {
            return new int[]{0};
        }
        if (ints.length - 1 >= 0) System.arraycopy(ints, 1, ints, 0, ints.length - 1);
        ints[ints.length - 1] = 0;
        return ints;
    }

    //XOR operation with 2 bits given in entry
    private static int xor(int a, int b) {
        return a ^ b;
    }

    static int[] divideByCRC(int[] messageToEncode) {
        int r = CRC.length - 1 + messageToEncode.length - CRC.length;

        // va etre le resultat mais dici la contient data
        int[] tempMessageToEncode = new int[messageToEncode.length + r - 1];
        System.arraycopy(messageToEncode, 0, tempMessageToEncode, 0, messageToEncode.length);

        while (r >= 0) {
            if (tempMessageToEncode[0] == 1) {
                for (int j = 0; j < CRC.length; j++) {
                    tempMessageToEncode[j] = xor(tempMessageToEncode[j], CRC[j]);
                }
            }

            tempMessageToEncode = bitshift(tempMessageToEncode);
            r--;
        }

        return Arrays.copyOfRange(tempMessageToEncode, 0, CRC.length - 1);
    }

    char getType() {
        return type;
    }

    String getData() {
        return data;
    }

    byte getNum() {
        return num;
    }
}