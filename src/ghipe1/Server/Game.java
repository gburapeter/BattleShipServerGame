package ghipe1.Server;


import ghipe1.Utils.Command;

import java.util.ArrayList;

/**
 * Játéklogikát kezelő osztály
 * Tartalmaz mezőket a játék: nevéről,
 * játéktipusáról, a hajók darabszámáról, illetve, hogy éppen zajlik e a játék
 *
 *
 * A specifikáció bárhány játszható játékosról szólt, nekem csak 2 játékossal sikerült sajnos
 * Próbálkoztam Mappeléssel, illetve különböző ArrayListekkel, de végül igy lett csak játszható:
 *
 * Host és guest player ( 2 személyes)
 * Current player az éppen körön levő játékos
 * Players array tartalmazza az összes játékost
 * Egy tábla a hostnak és a guestnek
 * Ennek megfelelően a játék jól csak akkor müködik jól ha a beirt NumberofPlayer az 2
 */

public class Game {
    private String gameName;
    private String gameMode;

    private Player host = null;
    private Player guest = null;
    private Player currentPlayer = null;

    private ArrayList<Player> players;


    private boolean isGameActive = false;
    private ServerBoard hostBoard = null;
    private ServerBoard guestBoard = null;

    //Játékosok pontszámai
    private int pointsPlayerOne;
    private int pointsPlayerTwo;

    //Letenni való hajói a játékosoknak
    private int hostShipsToPlace;
    private int guestShipsToPlace;

    //Hajók darabszáma tipustól függően
    private int ship2;
    private int ship3;
    private int ship4;
    private int ship5;

    //Tábla mérete (n)
    private int boardsize;

    //Össz hajó darabszám pl 2 Ship2, 2 Ship3, 1 Ship4, 1 Ship5 akkor ez össz 6 lesz
    private int shipsTotal;

    //Össz hajó egység, ez a hajó  darabszáma szorozva a hajó tipusátül függő hossza
    private int shipsUnitsTotal;

    //Össz megengedett játékos
    private int nrPlayer;


    /**
     * Konstruktorba inicializáljuk a fenti mezőket
     * @param name
     * @param player
     * @param gameMode
     * @param ship2
     * @param ship3
     * @param ship4
     * @param ship5
     * @param nrPlayer
     */


   public Game(String name,Player player, String gameMode,
               int ship2, int ship3, int ship4, int ship5, int nrPlayer){

       this.gameName = name;
       this.host = player;
       this.guest=null;
       this.gameMode=gameMode;
       this.ship2=ship2;
       this.ship3=ship3;
       this.ship4=ship4;
       this.ship5=ship5;
       this.nrPlayer=nrPlayer;

       //Kiszámoljuk az össz hajót, és az összes hajó által lefoglalt meződarabszámot
       this.shipsTotal=ship2+ship3+ship4+ship5;
       this.shipsUnitsTotal=ship2*2+ship3*3+ship4*4+ship5*5;

       this.guestShipsToPlace=this.shipsTotal;
       this.hostShipsToPlace=this.shipsTotal;
       this.players=new ArrayList<>();

       //Kiszámoljuk a board sizeját

       this.boardsize=(int) Math.ceil(((ship2*(2-1)+ship3*(3-1)+ship4*(4-1)+ship5*(5-1))*Math.ceil((double)nrPlayer/2)+1)/2);

       //A pontok a játék elején 0
       this.pointsPlayerOne=0;
       this.pointsPlayerTwo=0;

       //Létrehozzuk a két játékosnak a boardot
       hostBoard = new ServerBoard(boardsize,shipsUnitsTotal);
       guestBoard = new ServerBoard(boardsize,shipsUnitsTotal);


   }

    /**
     * Lövés utáni körcserénél használt metódus
     */
    private void changePlayer() {
        if (currentPlayer.equals(host))
            currentPlayer = guest;
        else
            currentPlayer = host;
    }

    /**
     * Ha Join gamere nyomtunk a GUI-n akkor a Game meghivja ezt a metódust
     * Hozzáadja az adott játékost a játékhoz
     * @param player
     */
    public void addPlayer(Player player) {

        /**
         *  Ha nem a játék creatora lép be, akkor ő lesz a guest
         */
        if(player != this.host) {
            this.guest = player;
        }

        //A playerekhez hozzáadjuk ezt a játékost is, és elküldjük a megfelelő üzenetet az infókkal
        players.add(player);
        player.sendToPlayer(Command.JOINED.toString() + "#" + this.getGameName() + "#" + this.boardsize + "#"
                    + ship2 + "#" + ship3 + "#" + ship4 + "#" + ship5 + "#" + player.getUserName());

        //Itt többjátékos módba próbáltam megoldani, hogy minden ezen gamebe levő játékosnak elküldje,
        //ne csak 2-nek
            for(Player p:players) {
                if (p!=player && p!=null) {
                    p.sendToPlayer(Command.NAMES_UPDATE.toString() + "#" + this.getHost().getUserName()
                            + "#" + this.getGuest().getUserName());
                    p.sendToPlayer(Command.OPPONENT_JOINED.toString() + "#" + this.getHost().getUserName()
                            + "#" + player.getUserName());
                }
            }

        //A kört az utolsó belépett játékos fogja kezdeni
        this.currentPlayer=player;


        /**
         * Akkor kezdődik el a hajó lerakás, ha a host által beirt darabszámú player rácsatlakozott a gamere
         * Esetemben 2-re hoztam ki
          */
        if(this.host!=null && this.guest!=null && players.size()==nrPlayer) {
            for(Player p:players) {
                p.sendToPlayer(Command.START_SHIP_PLACING.toString() + "#" + this.getGameName());
            }
        }

    }

    /**
     * Hajót szeretne elhelyezni az adott player, az adott x,y koordinátára
     * @param player
     * @param x
     * @param y
     * @param length
     * @param orientation
     */

    synchronized public void placeShip(Player player,int x,int y,int length,boolean orientation){
        Ship toPlace = new Ship(length,orientation);

        /**
         * Ha a host az adott player akkor erre a táblára tesszük rá ellenőrzés után, és csökkentjük az ő
         * még letevésre váró shipjeit
         */
        if(this.host == player) {
            if(this.hostBoard.placeShip(toPlace, x, y)) {//placement ok
                this.hostShipsToPlace--;
                player.sendToPlayer(Command.PLACEMENT_SUCCEED.toString());

                /**
                 * Ha nulla, akkor ez a player készen van a shipekkel
                 */
                if(this.hostShipsToPlace == 0)
                    player.sendToPlayer(Command.ALL_SHIPS_PLACED.toString());
           }
           else{
                player.sendToPlayer(Command.PLACEMENT_FAILED.toString());
            }
        }
        /**
         * Ha a guest player tesz le shipet,
         * akkor az ő letevésre való shipjeinek számát csökkentjük
         */
        else {
            if(this.guestBoard.placeShip(toPlace, x, y)){

                this.guestShipsToPlace--;
                player.sendToPlayer(Command.PLACEMENT_SUCCEED.toString());

                /**
                 * Ha elfogytak a letevésre váró hajói akkor üzenetet küldünk
                 */
                if(this.guestShipsToPlace == 0)
                    player.sendToPlayer(Command.ALL_SHIPS_PLACED.toString());

            }
            else{
                player.sendToPlayer(Command.PLACEMENT_FAILED.toString());
            }
        }
        /**
         * Ha mindkét player letette a hajóit, akkor elküldjük az üzenetet, hogy kezdődhet
         * a Shooting Phase
         */
        if(this.guestShipsToPlace == 0 && this.hostShipsToPlace == 0) {
            this.host.sendToPlayer(Command.START_SHOOTING.toString() + "#" + this.getGameName() + "#" +
                    this.currentPlayer.getUserName());
            this.guest.sendToPlayer(Command.START_SHOOTING.toString() + "#" + this.getGameName()
            + "#" + this.currentPlayer.getUserName());

            //A legelső lövést a legutoljára connectelt player fogja leadni
            this.currentPlayer.sendToPlayer(Command.INITIAL_TURN_MSG.toString());
            this.getOpponent(this.currentPlayer).sendToPlayer(Command.INITIAL_NOT_YOUR_TURN_MSG.toString());
        }
    }

    /**
     * Visszaadja az adott boardot, függően hogy host vagy guest a player
     * @param player
     * @return
     */
    private ServerBoard getPlayerBoard(Player player){
        if(player == host)
            return this.hostBoard;
        return this.guestBoard;
    }

    /**
     * Lövés metódus, egy adott player lő egy cellára(x,y)
     * @param shooter
     * @param x
     * @param y
     */
    public void shoot(Player shooter,int x, int y) {

        Player opponent = this.getOpponent(shooter);
        ServerBoard opponentBoard = this.getPlayerBoard(opponent);

        /**
         * Megnézzük, hogy talált-e vagy nem
         * Első esetben ha nem talált:
         */
        if (opponentBoard.checkIfMissed(x, y)) {
            /**
             * Mindkét játékosnak elküldjük, hogy nem talált, ezáltal frissül a segédtáblán mindenkinek
             *
             */
            shooter.sendToPlayer(Command.SHOT_MISSED.toString() + "#" + x + "#" + y + "#");
            opponent.sendToPlayer(Command.SHOT_MISSED.toString() + "#" + x + "#" + y);


            /**
             * Turn csere
             */
            opponent.sendToPlayer(Command.YOUR_TURN.toString() + "#" + x + "#" + y);
            shooter.sendToPlayer(Command.NOT_YOUR_TURN.toString() + "#" + x + "#" + y);
            this.changePlayer();
            /**
             * Message update
             */
            opponent.sendToPlayer(Command.SHOOTING_TURN_UPDATE.toString() + "#" + this.currentPlayer.getUserName());
            shooter.sendToPlayer(Command.SHOOTING_TURN_UPDATE.toString() + "#" + this.currentPlayer.getUserName());

        } else {

            /**
             * Ha talált, akkor pontot adunk a megfelelő játékosnak
             */
            if (this.currentPlayer == host) {
                    pointsPlayerOne++;
                } else {
                    pointsPlayerTwo++;
                }
            /**
             * Újból elküldjük mindkét játékosnak, hogy frissiteni tudják a saját illetve a segédtáblájukat
             */
            shooter.sendToPlayer(Command.SHOT_HIT.toString() + "#" + x + "#" + y + "#" + pointsPlayerOne + "#" + pointsPlayerTwo + "#");
            opponent.sendToPlayer(Command.SHOT_HIT.toString() + "#" + x + "#" + y + "#" + pointsPlayerOne + "#" + pointsPlayerTwo + "#");
            opponent.sendToPlayer(Command.OPPONENT_HIT_YOU.toString() + "#" + x + "#" + y + "#" + pointsPlayerOne + "#" + pointsPlayerTwo + "#");

            /**
             * Csökkentjük a shipcellák számát
             */
            opponentBoard.setTotalShips(opponentBoard.getTotalShips() - 1);

            /**
             * Ha az utolsó shipcellát lőttük le, akkor megnézzük, hogy milyen játékmód volt
             *
             */
            if (opponentBoard.getTotalShips() == 0) {
                /**
                 *  Ha last man standing, akkor mindenképpen az éppen lövő nyert
                 */
                    if (this.gameMode.equals("Last man standing")) {
                        shooter.sendToPlayer(Command.YOU_WIN.toString());
                        opponent.sendToPlayer(Command.YOU_LOSE.toString());

                        /**
                         * A points gamenek ebben a játékmegvalósitásban sajnos nincs sok értelme
                         * Mert úgyis a last man standing nyer(másképp nincs vége a játéknak)
                         */
                    } else if (this.gameMode.equals("Points game")) {

                        if (pointsPlayerOne > pointsPlayerTwo) {
                                this.host.sendToPlayer(Command.YOU_WIN.toString());
                                for (Player p : players) {
                                    if (p != this.host)
                                        p.sendToPlayer(Command.YOU_LOSE.toString() + "#" + this.getGameName());
                                }
                        }else {
                                this.guest.sendToPlayer(Command.YOU_WIN.toString());
                                for (Player p : players) {
                                    if (p != this.guest)
                                        p.sendToPlayer(Command.YOU_LOSE.toString() + "#" + this.getGameName());
                                }

                            }

                    }
                }
            /**
             * Ha eltaláltuk, de nincs vége a játéknak, akkor elküldjük a kör cseréről való üzenetet
             */
            else {

                    opponent.sendToPlayer(Command.YOUR_TURN.toString() + "#" + x + "#" + y);
                    shooter.sendToPlayer(Command.NOT_YOUR_TURN.toString() + "#" + x + "#" + y);
                    this.changePlayer();
                    opponent.sendToPlayer(Command.SHOOTING_TURN_UPDATE.toString() + "#" + this.currentPlayer.getUserName());
                    shooter.sendToPlayer(Command.SHOOTING_TURN_UPDATE.toString() + "#" + this.currentPlayer.getUserName());

                }


        }
    }

    /**
     * Getter-setterek
     * @return
     */


    public Player getHost() {
        return this.host;
    }

    public Player getGuest() {
        return this.guest;}

    public int getShip2() {
        return ship2;
    }

    public int getShip3() {
        return ship3;
    }

    public int getShip4() {
        return ship4;
    }

    public int getShip5() {
        return ship5;
    }

    public synchronized String getGameName() {
        return this.gameName;
    }

    /**
     * Visszaadja a
     * @param player
     * @return
     */
    public Player getOpponent(Player player) {
        if (player == host) return guest;
        else return host;
    }

}
