
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
    Set<Group> groups;
    ServerConfiguration conf;

    public Server(ServerConfiguration conf) {
        this.conf = conf;
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.conf.getServerPort());
            this.threads = new HashSet<>();
            this.groups = new HashSet<>();

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

    public void addGroup(Group group){
        groups.add(group);
    }

    public void removeGroup(Group group){
        groups.remove(group);
    }

    public String getUsers(){
        String result = "[";
        for (ClientThread t:threads) {
            result += "{name:'" + t.getUsername() + "'},";
        }
        result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }

    public String getGroups(){
        String result = "[";
        for (Group g:groups) {
            result += "{name:'" + g.getName() + "'},";
        }
        result = result.substring(0, result.length() - 1);
        result += "]";
        return result;
    }

    public ClientThread getClientByName(String name){
        for (ClientThread t:threads) {
            if(t.getUsername().equals(name)){
                return t;
            }
        }
        return null;
    }

    public Group getGroupByName(String name){
        for (Group g:groups) {
            if(g.getName().equals(name)){
                return g;
            }
        }
        return null;
    }

}
