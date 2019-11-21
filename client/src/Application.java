import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class Application {

    private Socket socket = null;
    private DataInputStream input = null;
    private OutputStream out = null;
    private InputStream in;

    private String command;
    private String username;

    private static boolean compatMode = false;

    public static void main(String[] args) {
        if(args != null && args.length > 0){
            if(args[0].equals("--compat-mode")){
                compatMode = true;
            }
        }
        new Application().run("127.0.0.1", 1337);
    }

    private void run(String ip, int port){
        connect(ip, port);
        readServerMessages();
        if(compatMode){
            readUserInputCompatMode();
        }else {
            readUserInput();
        }
    }

    private void connect(String ip, int port){
        try{
            socket = new Socket(ip, port);

            input = new DataInputStream(System.in);
            out = socket.getOutputStream();
            in = socket.getInputStream();

            command = "HELO";
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readUserInputCompatMode(){
        new Thread(() -> {
            String line = "";
            // keep reading until "Close" is entered
            while (!line.equals("Close"))
            {
                try{
                    Thread.sleep(33);
                    if(username == null) {
                        System.out.println("Enter username:");
                        PrintWriter writer = new PrintWriter(out);
                        line = input.readLine();

                        if (line.matches("[a-zA-Z0-9_]{3,14}")) {
                            writer.println(command + " " + line);
                            writer.flush();
                            username = line;
                        }
                    }else{
                        System.out.println("Type message and hit enter");
                        PrintWriter writer = new PrintWriter(out);
                        String userInput = input.readLine();
                        writer.println("BCST " + userInput);
                        writer.flush();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            closeConnection();
        }).start();
    }

    private void readUserInput(){
        new Thread(() -> {
            String line = "";
            // keep reading until "Close" is entered
            while (!line.equals("Close"))
            {
                try
                {
                    Thread.sleep(33);
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
                        System.out.println("7) Send message to group");
                        System.out.println("8) Leave a group");
                        System.out.println("9) Kick someone from my group");
                        System.out.println("");
                        System.out.println("What would you like to do? (1 / 9)");


                        int choice = Integer.parseInt(input.readLine());
                        PrintWriter writer = new PrintWriter(out);

                        switch (choice){
                            case 1:
                                System.out.println("What would you like to broadcast?");
                                String userInput = input.readLine();

                                writer.println("BCST " + userInput);
                                writer.flush();
                                break;
                            case 2:
                                writer.println("LS");
                                writer.flush();
                                break;
                            case 3:
                                System.out.println("To who would you like to send the message?");
                                userInput = input.readLine();
                                String receiver = userInput;
                                System.out.println("What would you like to send?");
                                userInput = input.readLine();
                                String message = userInput;

                                writer.println("DM " + "{name: '" + receiver + "', message: '" + message + "'}");
                                writer.flush();
                                break;
                            case 4:
                                writer.println("LG");
                                writer.flush();
                                break;
                            case 5:
                                System.out.println("How would you like to name the new group?");
                                userInput = input.readLine();
                                writer.println("CG " + userInput);
                                writer.flush();
                                break;
                            case 6:
                                System.out.println("What group would you like to join?");
                                userInput = input.readLine();
                                writer.println("JG " + userInput);
                                writer.flush();
                                break;
                            case 7:
                                System.out.println("To what group would you like to send the message?");
                                userInput = input.readLine();
                                String groupname = userInput;
                                System.out.println("What would you like to send?");
                                userInput = input.readLine();
                                message = userInput;

                                writer.println("BCSTG " + "{name: '" + groupname + "', message: '" + message + "'}");
                                writer.flush();
                                break;
                            case 8:
                                System.out.println("What group would you like to leave?");
                                userInput = input.readLine();
                                writer.println("LEAVE " + userInput);
                                writer.flush();
                                break;
                            case 9:
                                System.out.println("From what group would you like to kick someone");
                                userInput = input.readLine();
                                groupname = userInput;
                                System.out.println("Who would you like to kick?");
                                userInput = input.readLine();
                                String name = userInput;

                                writer.println("KICK " + "{name: '" + groupname + "', user: '" + name + "'}");
                                writer.flush();
                                break;
                        }
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
            }

            closeConnection();
        }).start();
    }

    private void closeConnection(){
        // close the connection
        try
        {
            command = "QUIT";

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
    }

    private void readServerMessages(){
        new Thread(() -> {
            try {
                String fromServer;
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                while ((fromServer = reader.readLine()) != null) {
                    //System.out.println("Server: " + fromServer);

                    Message message = new Message(fromServer);
                    JSONArray jsonArray;

                    switch (message.getMessageType()) {

                        case HELO:
                            System.out.println("Connected");
                            break;

                        case BCST:
                            System.out.println(fromServer.substring(5));
                            break;

                        case LS:
                            System.out.println("List of users that are currently online:");
                            jsonArray = new JSONArray(message.getPayload());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String userName = jsonObject.getString("name");
                                System.out.println(userName);
                            }
                            break;

                        case LG:
                            if(fromServer.length() > 4){
                                System.out.println("List of groups:");
                                jsonArray = new JSONArray(message.getPayload());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String groupName = jsonObject.getString("name");
                                    System.out.println(groupName);
                                }
                            }else {
                                System.out.println("No groups available");
                            }
                            break;

                        case DM:
                            JSONObject jsonObject = new JSONObject(message.getPayload());
                            System.out.println("DM [" + jsonObject.getString("username") + "] " + jsonObject.getString("message"));
                            break;

                        case BCSTG:
                            jsonObject = new JSONObject(message.getPayload());
                            System.out.println("GROUP [" + jsonObject.getString("groupname") + "] [" + jsonObject.getString("sender") + "] " + jsonObject.getString("message"));
                            break;

                        case PING:
                            pong();
                            break;

                        case DSCN:
                            System.out.println("Disconnected");
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

            command = "PONG";

            PrintWriter writer = new PrintWriter(out);
            writer.println(command);
            writer.flush();

            command = temp;
    }
}
