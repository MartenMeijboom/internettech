public class Main {
    public static void main(String[] args) {
        
        if (args.length == 0) {
            System.out.println("Starting the client with the default configuration.");
        } else {
            System.out.println("Starting the client with:");
        }

        ServerConfiguration config = new ServerConfiguration();
        boolean ParsePort = false;
        for (String arg : args) {
            if (ParsePort) {
                if (tryParseInt(arg)) {
                    int port = Integer.parseInt(arg);
                    config.setServerPort(port);
                    System.out.println(" * Port has been configured");
                } else {
                    System.out.println(" ERROR: Port is not a valid number.");
                }
                ParsePort = false;
            } else if (arg.equals("--no-colors")) {
                config.setShowColors(false);
            } else if (arg.equals("--no-pong")) {
                config.setSendPong(false);
            } else if (arg.equals("--port")) {
                ParsePort = true;
            }
        }


        if (config.isShowColors()) {
            System.out.println(" * Colors in debug message enabled");
        } else {
            System.out.println(" * Colors in debug message disabled");
        }
        if (config.isSendPong()) {
            System.out.println(" * Sending of PONG messages enabled");
        } else {
            System.out.println(" * Sending of PONG messages disabled");
        }

        System.out.println("Starting the server:");
        System.out.println("-------------------------------");
        //config.getClass();
        System.out.println("\tversion:\t" + "1.3");
        System.out.println("\thost:\t\t127.0.0.1");
        System.out.println("\tport:\t\t" + config.getServerPort());
        System.out.println("-------------------------------");
        (new Server(config)).run();
    }

    static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
