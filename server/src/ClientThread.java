import java.io.*;
import java.net.Socket;

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


                        case BCST:
                            for (ClientThread ct : server.threads) {
                                if (ct != this) {
                                    ct.writeToClient("BCST [" + getUsername() + "] " + message.getPayload());
                                }
                            }
                            writeToClient("+OK " + message.getLine());


                        case QUIT:
                            this.state = ServerState.FINISHED;
                            writeToClient("+OK Goodbye");


                        case PONG:
                            this.pongReceived = true;


                        case UNKOWN:
                            writeToClient("-ERR Unkown command");
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


    void writeToClient(String message) {
        PrintWriter writer = new PrintWriter(this.os);
        writer.println(message);
        writer.flush();


        boolean isIncomingMessage = false;
        logMessage(isIncomingMessage, message);
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