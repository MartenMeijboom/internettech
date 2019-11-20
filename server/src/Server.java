
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Server {
    private final int PING_TIMEOUT = 3000;
    private ServerSocket serverSocket;
    Set<ClientThread> threads;
    ServerConfiguration conf;

    public Server(ServerConfiguration conf) {
        this.conf = conf;
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.conf.getServerPort());
            this.threads = new HashSet<>();

            while (true) {
                Socket socket = this.serverSocket.accept();

                ClientThread ct = new ClientThread(socket, this);
                this.threads.add(ct);
                (new Thread(ct)).start();
                System.out.println("Num clients: " + this.threads.size());

                if (this.conf.isSendPong()) {
                    PingClientThread dct = new PingClientThread(ct);
                    (new Thread(dct)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

}
