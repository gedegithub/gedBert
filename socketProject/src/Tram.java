import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Tram {

    //enum FrameType { I, C, A, R, F, P}

    private byte flag = 0b01111110;
    private byte num; //the number of the Frame, we use a Byte, but its value will truly fluctuate on 3 bits
    private char type;// the type of the Frame, it takes one of the following values : { I, C, A, R, F, P}
    private String data;// The text which is transported in the frame
    private int crc; /*The CRC once it has been computed based on num, type and data. Not to be confused with
                        the generator polynome, which will be given as a parameter to calculate crc.*/

    //Constructor for frames that will be sent
    Tram(char type, byte num, String data){

        this.num = num;
        this.type = type;
        this.data = data;

    }

    //Constructor for frames that are recieved by the recipient of frames
    Tram(byte[] message){

        this.num = message[1];
        this.type = (char)message[2];
        this.data = new String(Arrays.copyOfRange(message, 3, message.length -3));
    }

    //this method converts the tram object into a string so that it may be used by sockets
    String tramToString(){

        ArrayList<Byte> message = new ArrayList<>();

        //we add the flag, tram number and tram type to the beginning of the tram
        message.add(flag);
        message.add((byte)type);
        message.add(num);

        byte[] dataBytes = this.data.getBytes();
        //We add the bytes from the data
        for(int i = 0; i < dataBytes.length; i++){
            message.add(dataBytes[i]);
        }

        //TODO ajouter la partie qui calcule et aoute le crc

        //we add the flag to the end of the message
        message.add(flag);

        return message.toString();
    }


    private void calculateCRC(){


    }

    public String getBinaryMessage(){

        String message = constructMessage();

        String binaryMessage = new BigInteger(message.getBytes()).toString(2);

        return binaryMessage;

    }

    public String constructMessage(){

        ArrayList<Byte> message = new ArrayList<>();

        //we add the tram number and tram type to the beginning of the tram
        message.add((byte)type);
        message.add(num);

        byte[] dataBytes = this.data.getBytes();
        //We add the bytes from the data
        for(int i = 0; i < dataBytes.length; i++){
            message.add(dataBytes[i]);
        }

        return message.toString();

    }

    public Boolean verifyCRC(int generator){
        return true;
    }


    public byte getFlag(){
        return this.flag;
    }

    public byte getNum(){
        return this.num;
    }

    public void setNum(Byte num){
        this.num = num;
    }

    public char getType(){
        return this.type;
    }

    public void setType(char Type){
        this.type = type;
    }

    public String getData(){
        return this.data;
    }

    public void setData(String data){
        this.data = data;
    }

    public int getCRC(){
        return this.crc;
    }

    public void setCRC(int crc){
        this.crc = crc;
    }


}
