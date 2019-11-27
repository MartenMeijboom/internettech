public class Message {
    private String line;

    public enum MessageType {
        HELO,
        BCST,
        DSCN,
        LS,
        DM,
        LG,
        BCSTG,
        PING,
        SESSIONKEY,
        PUBLICKEY,
        UNKOWN;
    }


    public Message(String line) {
        this.line = line;
    }


    public String getLine() {
        return this.line;
    }


    public MessageType getMessageType() {
        MessageType result = MessageType.UNKOWN;
        try {
            if(this.line.contains("PUBLICKEY")){
                return MessageType.PUBLICKEY;
            }else if(this.line.contains("SESSIONKEY")){
                return MessageType.SESSIONKEY;
            }

            if (this.line != null && this.line.length() > 0 && !line.contains("+OK") && !line.contains("-ERR")) {
                String[] splits = this.line.split("\\s+");
                result = MessageType.valueOf(splits[0]);
            }else if(line.contains("-")){
                System.out.println(line);
                if(line.equals("-ERR user already logged in")){
                    Application.userNameError();
                }
            }
        } catch (IllegalArgumentException iaex) {
            System.out.println("[ERROR] Unknown command");
            iaex.printStackTrace();
        }
        return result;
    }

    public String getPayload() {
        if (getMessageType().equals(MessageType.UNKOWN)) {
            return this.line;
        }

        if (this.line == null || this.line.length() < getMessageType().name().length() + 1) {
            return "";
        }

        return this.line.substring(getMessageType().name().length() + 1);
    }
}
