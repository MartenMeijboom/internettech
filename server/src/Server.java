import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;
    ArrayList<ClientThread> threads;
    ArrayList<Group> groups;
    Conf conf;

    public Server(Conf conf) {
        this.conf = conf;
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.conf.getServerPort());
            this.threads = new ArrayList<>();
            this.groups = new ArrayList<>();

            while (true) {
                Socket socket = this.serverSocket.accept();

                ClientThread ct = new ClientThread(socket, this);
                this.threads.add(ct);
                (new Thread(ct)).start();

                if (this.conf.isSendPong()) {
                    PingThread dct = new PingThread(ct);
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
