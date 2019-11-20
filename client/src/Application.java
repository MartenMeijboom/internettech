import java.io.*;
import java.net.Socket;

public class Application {

    private Socket socket = null;
    private DataInputStream input = null;
    private OutputStream out = null;
    private InputStream in;

    private enum commands{
        HELO,
        BCST,
        PONG,
        QUIT
    };
    private String command;
    private String username;

    public static void main(String[] args) {
        new Application().run("127.0.0.1", 1337);
    }

    private void run(String ip, int port){
        connect(ip, port);
        readUserInput();
        readServerMessages();
    }

    private void connect(String ip, int port){
        try{
            socket = new Socket(ip, port);
            System.out.println("Connected");

            input = new DataInputStream(System.in);
            out = socket.getOutputStream();
            in = socket.getInputStream();

            command = commands.HELO.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readUserInput(){
        new Thread(() -> {
            String line = "";
            // keep reading until "Close" is entered
            while (!line.equals("Close"))
            {
                try
                {
                    if(username == null){
                        System.out.println("Enter a username:");
                    }

                    PrintWriter writer = new PrintWriter(out);
                    line = input.readLine();

                    writer.println(command + " " + line);
                    writer.flush();

                    if(command.equals("HELO")){
                        username = line;
                    }

                    if(command.equals("BCST")){
                        System.out.println("[" + username + "] " + line);
                    }

                    command = commands.BCST.toString();
                }
                catch(IOException e)
                {
                    System.out.println(e);
                }
            }

            // close the connection
            try
            {
                command = commands.QUIT.toString();

                PrintWriter writer = new PrintWriter(out);
                writer.println(command);
                writer.flush();

                input.close();
                out.close();
                socket.close();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }).start();
    }

    private void readServerMessages(){
        new Thread(() -> {
            try {
                String fromServer;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((fromServer = reader.readLine()) != null) {
                    //System.out.println("Server: " + fromServer);

                    if(fromServer.contains("-ERR user")){
                        command = commands.HELO.toString();
                        username = null;
                    }
                    else if(fromServer.contains("BCST")){
                        if(!fromServer.contains("+OK")){
                            System.out.println(fromServer.substring(5));
                        }
                    }
                    else if(fromServer.contains("PING")){
                        pong();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void pong(){
            String temp = command;

            command = commands.PONG.toString();

            PrintWriter writer = new PrintWriter(out);
            writer.println(command);
            writer.flush();

            command = temp;
    }
}
