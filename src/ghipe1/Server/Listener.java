package ghipe1.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.*;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Listener osztály
 * Várja a kapcsolatfelépitést, majd fogadja ezeket, és az
 * ExecutorService segitségével kezeli a szálakat
 * A portot 12345-re állitottam, a kliens is ezt kell beirja, hogy csatlakozzon,
 * de Alertet is csináltam, ha nem ezt irná be
 *
 */
public class Listener extends Thread {


    private Server server;
    ExecutorService pool = Executors.newFixedThreadPool(20);


    public Listener(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        try {
            System.out.println("Server waiting for client");
            ServerSocket serverSocket = new ServerSocket(12345);
            serverSocket.setSoTimeout(50);

        ////Hozzáadjuk az adott klienst a játékosok közé, majd elinditja a szálat
            while (!interrupted()) {
                try {

                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connected: " + clientSocket);

                    Player player = new Player(clientSocket, server);
                    server.addPlayer(player);
                    pool.execute(player);

                } catch (SocketTimeoutException e) {

                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
