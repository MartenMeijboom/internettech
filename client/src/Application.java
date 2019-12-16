import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.Security;
import javax.swing.*;
import java.util.*;

public class Application {

    private Socket socket = null;
    private DataInputStream input = null;
    private OutputStream out = null;
    private InputStream in;

    private String command;

    private static Myself myself;

    private static boolean compatMode = false;

    private ArrayList<SomeoneElse> otherClients;

    public static void main(String[] args) {
        Security.setProperty("crypto.policy", "unlimited");
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

        otherClients = new ArrayList<>();

        myself = new Myself();
        if(myself.getPublicKey() == null){
            myself.generateKeys();
        }

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
                    if(myself.getName() == null) {
                        System.out.println("Enter username:");
                        PrintWriter writer = new PrintWriter(out);
                        line = input.readLine();

                        if (line.matches("[a-zA-Z0-9_]{3,14}")) {
                            writer.println(command + " " + line);
                            writer.flush();
                            myself.setName(line);
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
            boolean first = true;
            String line = "";
            // keep reading until "Close" is entered
            while (!line.equals("Close"))
            {
                try
                {
                    Thread.sleep(33);
                    if(myself.getName() == null){
                        System.out.println("Enter a username:");
                        PrintWriter writer = new PrintWriter(out);
                        line = input.readLine();

                        if(line.matches("[a-zA-Z0-9_]{3,14}")){
                            writer.println(command + " " + line);
                            writer.flush();
                            myself.setName(line);
                        }
                    }else{
                        if(first){
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
                            System.out.println(" - Files");
                            System.out.println("10) Send a file");
                            System.out.println("");
                            first = false;
                        }

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

                                sendDM(receiver, message);
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
                            case 10:
                                System.out.println("To who would you like to send the file?");
                                userInput = input.readLine();
                                receiver = userInput;

                                System.out.println("Please select the file");
                                File inputFile = new ChooseFile().getFile();

                                sendFile(receiver, inputFile);
                                break;
                        }
                    }
                }
                catch (NullPointerException e){
                    System.out.println("No file chosen");
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

    private void sendFile(String receiver, File file){
        System.out.println(file.getName() + " chosen");
        System.out.println("Sending file to " + receiver +  "...");

        new Thread(() -> {
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;
            try {
                PrintWriter writer = new PrintWriter(out);

                int fileSize = (int)file.length();
                byte[] fileByteArray = new byte[fileSize];

                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(fileByteArray, 0, fileByteArray.length);

                String fileString = Base64.getEncoder().encodeToString(fileByteArray);

                writer.println("FILE {name: '" + receiver + "', file: '" + fileString + "', filename: '" + file.getName() + "'}");
                writer.flush();

                System.out.println("Finished sending file!");
            }catch (NegativeArraySizeException e){
                System.out.println("File is too big. Maximum file size is " + 256 + " mb");
            }
            catch (Exception e){
                System.out.println("Something went wrong while sending the file");
                e.printStackTrace();
            }finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private HashMap<String, String> dms;
    private void sendDM(String receiver, String message){
        dms = null;
        try {
            SomeoneElse otherClient = getPersonByName(receiver);

            if (otherClient != null) {
                if (otherClient.getSessionKey() != null) {
                    String encryptedMessage = otherClient.encrypt(message);

                    PrintWriter writer = new PrintWriter(out);
                    writer.println("DM " + "{name: '" + receiver + "', message: '" + encryptedMessage + "'}");
                    writer.flush();
                }else if(otherClient.getPublicKey() != null){
                    otherClient.generateSessionKey();

                    //encoding
                    byte[] encryptedKey = otherClient.EncryptSecretKey(otherClient.getSessionKey());
                    String encryptedKeyString = Base64.getEncoder().encodeToString(encryptedKey);

                    PrintWriter writer = new PrintWriter(out);
                    writer.println("SESSIONKEY " + "{name: '" + receiver + "', message: '" + encryptedKeyString + "'}");
                    writer.flush();

                    System.out.println("Private connection with " + receiver + " created!");

                    String encryptedMessage = otherClient.encrypt(message);

                    writer = new PrintWriter(out);
                    writer.println("DM " + "{name: '" + receiver + "', message: '" + encryptedMessage + "'}");

                    writer.flush();
                }else{
                    PrintWriter writer = new PrintWriter(out);
                    writer.println("PUBLICKEY " + " { name: '" + receiver + "', message: '" + myself.getPublicKeyString() + "' }");
                    writer.flush();
                    dms = new HashMap<>();
                    dms.put(receiver, message);
                }
            }else{
                otherClient = new SomeoneElse(receiver);
                otherClients.add(otherClient);

                PrintWriter writer = new PrintWriter(out);
                writer.println("PUBLICKEY " + "{name: '" + receiver + "', message: '" + myself.getPublicKeyString() + "'}");
                writer.flush();

                dms = new HashMap<>();
                dms.put(receiver, message);
            }
        }catch (Exception e){
            e.printStackTrace();
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
                    JSONObject jsonObject;

                    switch (message.getMessageType()) {
                        case HELO:
                            System.out.println("Connected");
                            break;

                        case DM:
                            jsonObject = new JSONObject(message.getPayload());
                            String decodedMessage = getPersonByName(jsonObject.getString("username")).decrypt(jsonObject.getString("message"));
                            System.out.println("DM [" + jsonObject.getString("username") + "] " + decodedMessage);

                            break;

                        case BCST:
                            System.out.println(fromServer.substring(5));
                            break;

                        case LS:
                            System.out.println("List of users that are currently online:");
                            jsonArray = new JSONArray(message.getPayload());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                jsonObject = jsonArray.getJSONObject(i);
                                String userName = jsonObject.getString("name");
                                System.out.println(userName);
                            }
                            break;

                        case LG:
                            if(fromServer.length() > 4){
                                System.out.println("List of groups:");
                                jsonArray = new JSONArray(message.getPayload());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    jsonObject = jsonArray.getJSONObject(i);
                                    String groupName = jsonObject.getString("name");
                                    System.out.println(groupName);
                                }
                            }else {
                                System.out.println("No groups available");
                            }
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
                        case PUBLICKEY:
                            handlePublicKey(message);
                            break;
                        case SESSIONKEY:
                            handleSessionKey(message);
                            break;
                        case FILE:
                            receiveFile(message);
                            break;
                        case UNKOWN:
                            //System.out.println("YEET");
                            break;
                    }

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void receiveFile(Message message){
        new Thread(() -> {

            FileOutputStream fileOutputStream = null;
            BufferedOutputStream bufferedOutputStream = null;

            try {
                JSONObject jsonObject = new JSONObject(message.getPayload());
                String sender = jsonObject.getString("name");
                String file = jsonObject.getString("file");
                String fileName = jsonObject.getString("filename");

                System.out.println("Receiving file from " + sender + "...");

                byte[] fileByteArray = Base64.getDecoder().decode(file);
                fileOutputStream = new FileOutputStream("downloads/" + fileName);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                bufferedOutputStream.write(fileByteArray, 0, fileByteArray.length);
                bufferedOutputStream.flush();

                System.out.println("File received!");

            }catch (Exception e){
                System.out.println("Something went wrong while receiving the file");
            }finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handlePublicKey(Message message){
        try {
            JSONObject jsonObject = new JSONObject(message.getPayload());
            String username = jsonObject.getString("name");
            String key = jsonObject.getString("message");

            SomeoneElse user = getPersonByName(username);

            if (user == null) {
                user = new SomeoneElse(username);
                otherClients.add(user);
            }

            user.setPublicKey(Base64.getDecoder().decode(key));

            if(user.getSessionKey() == null){
                user.generateSessionKey();
            }

            //encoding
            byte[] encryptedKey = user.EncryptSecretKey(user.getSessionKey());
            String encryptedKeyString = Base64.getEncoder().encodeToString(encryptedKey);

            PrintWriter writer = new PrintWriter(out);
            writer.println("SESSIONKEY " + "{name: '" + user.getName() + "', message: '" + encryptedKeyString + "'}");
            writer.flush();

            System.out.println("Private connection with " + username + " has been initialised");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleSessionKey(Message message){
        try {
            JSONObject jsonObject = new JSONObject(message.getPayload());
            String username = jsonObject.getString("name");
            String key = jsonObject.getString("message");

            SomeoneElse user = getPersonByName(username);

            if (user == null) {
                user = new SomeoneElse(username);
                otherClients.add(user);
            }

            byte[] decodedEncryptedKey = Base64.getDecoder().decode(key);
            SecretKey originalKey = myself.decryptAESKey(decodedEncryptedKey);

            user.setSessionKey(originalKey);

            System.out.println("Private connection with " + username + " has been initialised");

            if(dms != null){
                for (Map.Entry<String, String> entry:dms.entrySet()) {
                    sendDM(entry.getKey(), entry.getValue());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void userNameError(){
        myself.setName(null);
    }

    private void pong(){
            String temp = command;

            command = "PONG";

            PrintWriter writer = new PrintWriter(out);
            writer.println(command);
            writer.flush();

            command = temp;
    }

    private SomeoneElse getPersonByName(String name){
        for (SomeoneElse e:otherClients) {
            if(e.getName().equals(name)){
                return e;
            }
        }
        return null;
    }
}
