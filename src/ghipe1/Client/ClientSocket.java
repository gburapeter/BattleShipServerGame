package ghipe1.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Kliens oldali Socket
 * A megfelelő Writer-Readerekkel
 */
public class ClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Connect boolean, ami visszadja, hogy sikerült e csatlakozni
     * Localhostot állitottam, de ip cimek is kerülhetnek ide
     * A portot a kliens irja be ( 12345-t állitottam a szerver oldalon, de dob alertet a gui)
     * @param address
     * @param port
     * @return
     */
    public boolean connect(String address,int port){
        try {
            socket = new Socket("localhost",port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;

        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Elküldjük az üzenetet a csatorna másik oldalának
     * @param message
     */
    public void sendMessage(String message) {


       out.println(message);

    }

    /**
     * Kiolvassuk az üzenetet
     * @return
     * @throws IOException
     */
    public String receiveMessage() throws IOException {
        String msg;
        msg = in.readLine();
        return msg;
    }
}
