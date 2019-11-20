import org.json.JSONArray;
import org.json.JSONObject;

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
                        PrintWriter writer = new PrintWriter(out);
                        line = input.readLine();

                        if(line.matches("[a-zA-Z0-9_]{3,14}")){
                            writer.println(command + " " + line);
                            writer.flush();
                            username = line;
                        }
                    }else{
                        System.out.println("");
                        System.out.println("Available options:");
                        System.out.println(" - Users");
                        System.out.println("1) Broadcast message to all users");
                        System.out.println("2) Show list of users");
                        System.out.println("3) Send private message to a user");
                        System.out.println(" - Groups");
                        System.out.println("4) Show a list of groups");
                        System.out.println("5) Create a group");
                        System.out.println("6) Join a group");
                        System.out.println("7) Leave a group");
                        System.out.println("8) Kick someone from my group");
                        System.out.println("");
                        System.out.println("What would you like to do? (1 / 8)");


                        int choice = Integer.parseInt(input.readLine());
                        PrintWriter writer = new PrintWriter(out);

                        switch (choice){
                            case 1:
                                System.out.println("What would you like to broadcast?");
                                String userInput = input.readLine();

                                writer.println("BCST " + userInput);
                                writer.flush();
                                System.out.println("[" + username + "] " + userInput);
                                break;
                            case 2:
                                writer.println("LS");
                                writer.flush();
                                break;
                        }
                    }
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
                    System.out.println("Server: " + fromServer);

                    Message message = new Message(fromServer);

                    switch (message.getMessageType()) {

                        case HELO:

                            break;

                        case BCST:
                            System.out.println(fromServer.substring(5));
                            break;

                        case LS:
                            System.out.println("List of users that are currently online:");
                            JSONArray jsonArray = new JSONArray(message.getPayload());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String userName = jsonObject.getString("name");
                                System.out.println(userName);
                            }
                            break;

                        case LG:
                            break;

                        case JG:
                            break;

                        case LEAVE:
                            break;

                        case CG:

                            break;

                        case DM:
                            break;

                        case KICK:

                            break;

                        case QUIT:

                            break;

                        case BCSTG:

                            break;

                        case PING:
                            pong();
                            break;

                        case UNKOWN:

                            break;
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
