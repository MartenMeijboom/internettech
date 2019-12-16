public class Conf {
    public final int DEFAULT_SERVER_PORT = 1337;

    private int serverPort;


    private boolean showColors = true;


    private boolean sendPong = true;


    public Conf() {
        this.serverPort = 1337;
    }


    public int getServerPort() {
        return this.serverPort;
    }


    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


    public boolean isShowColors() {
        return this.showColors;
    }


    public void setShowColors(boolean showColors) {
        this.showColors = showColors;
    }


    public boolean isSendPong() {
        return this.sendPong;
    }


    public void setSendPong(boolean sendPong) {
        this.sendPong = sendPong;
    }
}
