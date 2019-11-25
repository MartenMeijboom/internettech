import java.util.Random;

public class PingThread implements Runnable {
    ClientThread ct;

    private boolean shouldPing = true;

    PingThread(ClientThread ct) {
        this.ct = ct;
    }


    public void run() {
        while (this.shouldPing) {

            try {
                int sleep = (10 + (new Random()).nextInt(10)) * 1000;
                Thread.sleep(sleep);
                this.ct.pongReceived = false;
                this.ct.writeToClient("PING");


                Thread.sleep(3000L);
                if (!this.ct.pongReceived) {
                    this.shouldPing = false;
                    this.ct.writeToClient("DSCN Pong timeout");
                    this.ct.kill();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}