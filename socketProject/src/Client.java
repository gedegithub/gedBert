import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException{
        Socket socket = new Socket("localhost", 8081);

        // Client writing in buffer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println("Hello from Client, is it working ?");
        printWriter.flush();

        // to read in buffer
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String str = bufferedReader.readLine();
        System.out.println("server: " + str);
    }
}
