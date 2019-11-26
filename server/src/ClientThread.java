import java.io.*;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientThread implements Runnable {
    private DataInputStream is;

    private OutputStream os;

    private Socket socket;
    private Server server;

    private ServerState state;
    private String username;
    boolean pongReceived = false;

    public ClientThread(Socket socket, Server server) {
        this.state = ServerState.INIT;
        this.socket = socket;
        this.server = server;
    }


    public String getUsername() {
        return this.username;
    }


    public void run() {
        try {
            this.os = this.socket.getOutputStream();
            this.is = new DataInputStream(this.socket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.is));


            this.state = ServerState.CONNECTING;
            //server.conf.getClass();
            String welcomeMessage = "HELO " + "Welkom to WhatsUpp!";
            writeToClient(welcomeMessage);

            while (!this.state.equals(ServerState.FINISHED)) {

                String line = reader.readLine();
                if (line != null) {

                    boolean userExists, isValidUsername, isIncomingMessage = true;
                    logMessage(isIncomingMessage, line);


                    Message message = new Message(line);


                    switch (message.getMessageType()) {

                        case HELO:
                            isValidUsername = message.getPayload().matches("[a-zA-Z0-9_]{3,14}");
                            if (!isValidUsername) {
                                this.state = ServerState.FINISHED;
                                writeToClient("-ERR username has an invalid format (only characters, numbers and underscores are allowed)");
                                continue;
                            }
                            userExists = false;
                            for (ClientThread ct : server.threads) {
                                if (ct != this && message.getPayload().equals(ct.getUsername())) {
                                    userExists = true;
                                    break;
                                }
                            }
                            if (userExists) {
                                writeToClient("-ERR user already logged in");
                                continue;
                            }
                            this.state = ServerState.CONNECTED;
                            this.username = message.getPayload();
                            writeToClient("+OK " + message.getLine());
                            break;

                        case BCST:
                            for (ClientThread ct : server.threads) {
                                if (ct != this) {
                                    ct.writeToClient("BCST [" + getUsername() + "] " + message.getPayload());
                                }
                            }
                            writeToClient("+OK " + message.getLine());
                            break;

                        case LS:
                            writeToClient("LS " + server.getUsers());
                            break;

                        case LG:
                            writeToClient("LG " + server.getGroups());
                            break;

                        case JG:
                            joinGroup(message.getPayload());
                            break;

                        case LEAVE:
                            leaveGroup(message.getPayload());
                            break;

                        case CG:
                            Group group = new Group(this, message.getPayload());
                            server.addGroup(group);
                            break;

                        case DM:
                            sendDM(message.getPayload());
                            break;

                        case KICK:
                            kickUser(message.getPayload());
                            break;

                        case QUIT:
                            this.state = ServerState.FINISHED;
                            writeToClient("+OK Goodbye");
                            break;

                        case BCSTG:
                            broadcastGroup(message.getPayload());
                            break;

                        case PONG:
                            this.pongReceived = true;
                            break;

                        case PUBLICKEY:
                            handlePublicKey(message);
                            break;
                        case SESSIONKEY:
                            handleSessionKey(message);
                            break;
                        case UNKOWN:
                            writeToClient("-ERR Unkown command");
                            break;
                    }
                }
            }
            server.threads.remove(this);
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
    }

    public void kill() {
        try {
            System.out.println("[DROP CONNECTION] " + getUsername());
            server.threads.remove(this);
            this.socket.close();
        } catch (Exception ex) {
            System.out.println("Exception when closing outputstream: " + ex.getMessage());
        }
        this.state = ServerState.FINISHED;
    }

    private void handlePublicKey(Message message){
        JSONObject jsonObject = new JSONObject(message.getPayload());
        String username = jsonObject.getString("name");
        String key = jsonObject.getString("message");

        ClientThread user = server.getClientByName(username);

        if(user != null){
            user.writeToClient("PUBLICKEY {name: '" + this.username + "', message: '" + key + "'}" );
        }else{
            writeToClient("-ERR user not found");
        }
    }

    private void handleSessionKey(Message message){
        JSONObject jsonObject = new JSONObject(message.getPayload());
        String username = jsonObject.getString("name");
        String key = jsonObject.getString("message");

        ClientThread user = server.getClientByName(username);

        if(user != null){
            user.writeToClient("SESSIONKEY {name: '" + this.username + "', message: '" + key + "'}" );
        }else{
            writeToClient("-ERR user not found");
        }
    }

    private void joinGroup(String message){
        try {
            Group group = server.getGroupByName(message);

            if(group != null){
                group.addMember(this);
                this.writeToClient("+OKDJG " + message);
            }else{
                this.writeToClient("-ERR group not found");
            }

        }catch (JSONException err){
            err.printStackTrace();
        }
    }

    private void leaveGroup(String message){
        try {
            Group group = server.getGroupByName(message);

            if(group != null){
                group.removeMember(this);
                this.writeToClient("+OKLG " + message);
            }else{
                this.writeToClient("-ERR group not found");
            }

        }catch (JSONException err){
            err.printStackTrace();
        }
    }

    private void kickUser(String message){
        try {
            JSONObject jsonObject = new JSONObject(message);
            String groupName = jsonObject.getString("name");
            String userName = jsonObject.getString("user");

            Group group = server.getGroupByName(groupName);
            ClientThread user = server.getClientByName(userName);

            if(user != null){
                if(group != null){
                    if(group.getOwner().equals(this)){
                        group.removeMember(user);
                        this.writeToClient("+OKKICK " + groupName);
                        user.writeToClient("BCST You have been kicked from " + groupName);
                    }else{
                        this.writeToClient("-ERR you are not the owner of this group");
                    }
                }else{
                    this.writeToClient("-ERR group not found");
                }
            }else{
                this.writeToClient("-ERR user not found");
            }

        }catch (JSONException err){
            err.printStackTrace();
        }
    }

    private void broadcastGroup(String message){
        try {
            JSONObject jsonObject = new JSONObject(message);
            String groupName = jsonObject.getString("name");
            String messageText = jsonObject.getString("message");

            Group group = server.getGroupByName(groupName);

            if(group != null){
                group.broadCastMessage("{groupname: '" + groupName + "', sender: '" + this.username + "', message: '" + messageText + "'}");
                this.writeToClient("+OKBCSTG " + groupName);
            }else{
                this.writeToClient("-ERR group not found");
            }

        }catch (JSONException err){
            err.printStackTrace();
        }
    }

    void writeToClient(String message) {
        PrintWriter writer = new PrintWriter(this.os);
        writer.println(message);
        writer.flush();

        boolean isIncomingMessage = false;
        logMessage(isIncomingMessage, message);
    }

    private void sendDM(String message){
        try {
            JSONObject jsonObject = new JSONObject(message);
            String reveiverName = jsonObject.getString("name");
            String payload = jsonObject.getString("message");

            ClientThread receiver = server.getClientByName(reveiverName);

            if(receiver != null){
                receiver.writeToClient("DM { username: '" + this.getUsername() + "', message: '" + payload + "'}");
                this.writeToClient("+OKDM " + payload);
            }else{
                this.writeToClient("-ERR user not found");
            }

        }catch (JSONException err){
            err.printStackTrace();
        }
    }

    private void logMessage(boolean isIncoming, String message) {
        String logMessage;
        server.conf.getClass();
        String colorCode = "\033[32m";
        String directionString = "<< ";
        if (isIncoming) {
            server.conf.getClass();
            colorCode = "\033[31m";
            directionString = ">> ";
        }


        if (getUsername() == null) {
            logMessage = directionString + message;
        } else {
            logMessage = directionString + "[" + getUsername() + "] " + message;
        }


        if (server.conf.isShowColors()) {
            server.conf.getClass();
            System.out.println(colorCode + logMessage + "\033[0m");
        } else {
            System.out.println(logMessage);
        }
    }
}