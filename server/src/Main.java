public class Main {
    public static void main(String[] args) {
        Conf config = new Conf();
        boolean ParsePort = false;
        for (String arg : args) {
            if (ParsePort) {
                if (ParseIfInt(arg)) {
                    int port = Integer.parseInt(arg);
                    config.setServerPort(port);
                    System.out.println("Port has been set");
                } else {
                    System.out.println("Not a valid port");
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

        System.out.println("port: " + config.getServerPort());
        System.out.println("-----------");
        (new Server(config)).run();
    }

    static boolean ParseIfInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
