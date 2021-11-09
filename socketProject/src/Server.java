import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8081);
        Socket socket = serverSocket.accept();

        System.out.println("Client connected");

        // to read in buffer
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String str = bufferedReader.readLine();
        System.out.println("client: " + str);

        // Server writing in buffer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println("Hi from Server, yes it's working !");
        printWriter.flush();
    }
}
