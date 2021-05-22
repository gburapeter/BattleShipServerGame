package ghipe1.Server;


import ghipe1.Utils.Command;
import java.io.IOException;
import java.net.Socket;

/**
 * Játékos szál osztály
 * Tartalmazza a játékos nicknamejét,
 * és a Gamet amelyikbe éppen van
 * Emellett, hogy melyik szerverre csatlakozott
 */
public class Player extends Thread {


    private String userName = null;
    private PlayerSocket socket;
    private Game game = null;
    private final Server server;

    /**
     * Beállitjuk konstruktorba az adatokat
     * @param socket
     * @param server
     */
    public Player(Socket socket, Server server) {
        this.socket = new PlayerSocket(socket);
        this.server=server;

    }

    /**
     * A szál futásában dolgozza fel a klienstől kapott üzeneteket
     */
    public void run() {
        try {
            // Létrehozódik a megfelelő Writer és Reader
           this.socket.connect();
            String received;
           //Végtelen ciklusba müködik
            loop:

            while ( ( received = this.socket.receiveMessage())!= null ) {

                //A kapott üzenetet egy tömbbe helyezi, a #-al elválasztva, ezek tartalmazzák az adott infókat
                String tmp[] = received.split("#");
                String command = tmp[0];

                /**
                 * Ha bejelentkezett a kliens, akkor visszaküldjük neki, a sikeres logint,
                 * és elküldjük neki a szerveren eddig létrehozott játékokat
                 */
                if (command.equals(Command.LOGIN.toString())) {

                    userName = tmp[1];
                    sendToPlayer(Command.LOGIN_SUCCEED.toString());

                       for (Game game : server.getGames()) {
                            String info = Command.AVAILABLE_GAME.toString();
                            info = info + "#" + game.getGameName();
                            sendToPlayer(info);
                        }

                }
                /**
                 * Ha játékot hoz létre a játékos, akkor kiszedjük ebből a Packageből a:
                 * játékos nevét, a gamemode-ot(points, last man standing),
                 * a 2-es tipusu ship darabszámát, a 3-as tipusu ship darabszámát,
                 * a 4-es tipusu ship darabszámát, és az 5-s tipusú ship darabszámát,
                 * Emellett az összes ezen játékba játszható player számát
                 */
                else if (command.equals(Command.CREATE_GAME.toString())) {

                    resetPlayer();
                    String name = tmp[1];
                    String gameMode = tmp[2];
                    int ship2 = Integer.parseInt(tmp[3]);
                    int ship3 = Integer.parseInt(tmp[4]);
                    int ship4 = Integer.parseInt(tmp[5]);
                    int ship5 = Integer.parseInt(tmp[6]);
                    int nrPlayer = Integer.parseInt(tmp[7]);

                   //Ha már van ilyen nevü játék akkor nem hozható még egy ilyen létre
                    if(server.checkGameNameAvailability(name)){
                        //Létrehozzuk az új játékot, és a szerverbeli gamekhez is hozzáadjuk

                        Game game = new Game(name, this, gameMode, ship2, ship3, ship4, ship5,nrPlayer);
                        this.game = game;
                        server.addGame(game);

                        // Üzenet a játékosnak, hogy sikerült létrehozni
                        sendToPlayer(Command.GAME_CREATED.toString() + "#" + name);
                    } else
                        {
                            // Üzenet a játékosnak, hogy nem sikerült létrehozni
                        sendToPlayer(Command.GAME_CREATED_ERROR.toString());
                    }

                }
                /**
                 * Ha játékot szeretnénk kitörölni
                 * Csak a host törölheti ki a saját játékát
                 */
                else if (command.equals(Command.REMOVE_GAME_FROM_LIST.toString())) {

                    if (this.game!= null && this == this.game.getHost() ) {
                        server.removeGame(game);
                    ///Ha sikerült kitörölni ahost által, akkor elküldjük az update üzenetet
                        this.sendToPlayer(Command.GAME_DELETED_UPDATE.toString());
                    }
                    else{
                        //Nem a host próbálta kitörölni
                        this.sendToPlayer(Command.YOUR_NOT_THE_HOST_OF_THIS_GAME.toString());
                    }

                }
                /**
                 * Ha egy létrehozott játékhoz szeretnén csatlakozni
                 * Megkeresi a szerveren található Listába az adott gamet, és ha van ilyen akkor
                 * hozzáaadja a játékost
                 *
                 */
                else if (command.equals(Command.JOIN_TO_GAME.toString())) {
                    resetPlayer();
                    Game toJoin = server.findGame(tmp[1]);
                    if (toJoin != null) {

                        toJoin.addPlayer(this);
                        this.game = toJoin;

                        //Üzenetetet küldök, hogy updatelje a GUI-t
                       if(this.game.getGuest()!=null) {
                            sendToPlayer(Command.NAMES_UPDATE.toString() + "#" + this.game.getHost().getUserName() + "#"
                                    + this.game.getGuest().getUserName());
                        }
                        else{
                            sendToPlayer(Command.NAMES_UPDATE.toString() + "#" + this.game.getHost().getUserName() + "#"
                                    + "Not connected");
                        }
                        //Üzenetet küldök, hogy updatelje a shipek számát a GUI-n
                        sendToPlayer(Command.SHIPS_UPDATE.toString() + "#" + this.game.getShip2() +"#"
                                + this.game.getShip3() + "#" + this.game.getShip4() + "#" + this.game.getShip5());
                    } else
                        //Nem sikerült csatlakozni
                        {
                        sendToPlayer(Command.JOIN_TO_GAME_FAILED.toString());
                    }
                }
                /**
                 * Ha hajót szeretnénk letenni, akkor kiszedjük a megfelelő infókat:
                 * hajó orientáltsága, koordinátái, hossza, és a Game-nek átadjuk, hogy létrehozza a hajót(ha lehetséges)
                 */
                else if (command.equals(Command.PLACE_A_SHIP.toString())) {
                    try {

                        boolean vertical = Boolean.parseBoolean(tmp[1]);
                        int x = Integer.parseInt(tmp[2]);
                        int y = Integer.parseInt(tmp[3]);
                        int length = Integer.parseInt(tmp[4]);
                        this.game.placeShip(this, x, y, length, vertical);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                /**
                 * Lövünk az adott X,Y koordinátára
                 */
                else if (command.equals(Command.SHOOT.toString())) {
                    try {
                        int x = Integer.parseInt(tmp[1]);
                        int y = Integer.parseInt(tmp[2]);
                        this.game.shoot(this, x, y);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetPlayer() {
        game = null;
    }

    /**
     * Message sender metódus
     * @param msg
     */
    public void sendToPlayer(String msg) {
        this.socket.sendMessage(msg);
    }

    /**
     * Visszaadja a player becenevét
     * @return
     */
    public String getUserName() {
        return userName;
    }

}
