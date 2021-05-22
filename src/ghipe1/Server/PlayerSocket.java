package ghipe1.Server;

/**
 * Szerver oldalán müködő socket
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Megfelelő BufferedReader és PrintWriter az irás olvasáshoz
 */
public class PlayerSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public PlayerSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Elküldi az üzenetet
     * @param message
     */
    public void sendMessage(String message) {
        this.out.println(message);
    }

    /**
     * Beolvassa, és visszadja ezt Stringbe
     * @return
     * @throws IOException
     */
    public String receiveMessage() throws IOException {
        String msg = null;
        msg = in.readLine();
        return msg;
    }

    /**
     * Létrehozza a megfelelő readert és writer
     * @throws IOException
     */
    public void connect() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
}
