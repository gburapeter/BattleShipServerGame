package ghipe1.Client;



import ghipe1.Client.Controller.ClientLoginController;
import ghipe1.Client.Controller.GameWindowController;
import ghipe1.Utils.Package;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import ghipe1.Utils.Command;

import java.io.IOException;
import java.util.*;

/**
 * Kliens által kapott üzeneteket kezelő osztály
 * FXML-t használtam, tartalmazza ezenek a controllereit is
 * Három Scene-ja van a játéknak:
 * A login phase, a játéklétrehozó/törlő scene és maga a játék sceneje
 * Ezeknek a megfelelő controllerei, megjelenő sorrendben:
 * StartUpMenuController
 * ClientLoginController
 * GameWindowController
 *
 */
public class GameService extends Service<Void> {

    public ClientLoginController viewController;
    public GameWindowController gameWindowController;

    private ClientSocket clientSocket;
    private boolean isConnected = false;

    /**
     * Listával való megjelenitéshez arraylist, ezeket frissitem
     */
    ArrayList<String> games;

    /**
     * Timer a gépi lövéshez
     */
    private Timer timer =new Timer();


    public GameService() {
        super();
        clientSocket = new ClientSocket();
        games = new ArrayList<>();
    }




    /**
     * Ha sikerült csatlakozni true, else false
     * @param port
     * @return
     */
    public boolean tryConnect(int port) {
        isConnected = this.clientSocket.connect("localhost", port);
        return isConnected;
    }

    /**
     * Taskot használ a program.
     * A kapott üzenetetet #-önként tömbbe rakja, és feldolgozza a commandokat
     * Igyekeztem a megfelelő szálokon a guit frissiteni( meg szólt a Task is)
     * @return
     */
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() {
                try{

                    while (true) {
                        String received = clientSocket.receiveMessage();
                        /**
                         * A tmp[0] mindig az üzenet command része
                         */
                        if (received != null) {
                          //  System.out.println(received);
                            String tmp[] = received.split("#");
                            String command = tmp[0];

                            /**
                             * Ha sikerült bejelentkezni, akkor megjelenitjük a welcome üzenetet
                             */
                            if (command.equals(Command.LOGIN_SUCCEED.toString())) {
                                Platform.runLater(() -> {
                                viewController.setText();
                                });
                            }
                            /**
                             * Ha a game create-re kattintunk, és sikerült létrehozni, akkor a szerver visszaküldi ezt
                             * Update message, hogy sikerült létrehozni
                             */
                            else if (command.equals(Command.GAME_CREATED.toString())) {
                                Platform.runLater(() -> {
                                    viewController.setMainLabelText("New game created, its name: "+tmp[1]);


                                });

                            }
                            /**
                             * Ha nem sikerült, ezt az üzenetet tesszük ki
                             */
                            else if (command.equals(Command.GAME_CREATED_ERROR.toString())) {
                                Platform.runLater(() -> {
                                    viewController.setMainLabelText("Could not create the game");


                                });

                            }
                            /**
                             * Ha létrehoztunk egy játékot, ezt kapjuk vissza,
                             * hozzáadja a game listához a megfelelő játéknevet
                             */
                           else if (command.equals(Command.WAIT_FOR_OPPONENT.toString())) {
                                games.add(tmp[1]);
                                Platform.runLater(() -> {

                                    viewController.addGame(tmp[1]);

                                });
                            }
                            /**
                             * Amikor felcsatlakozik valaki, megkapja ezt az üzenetet az összes játékról
                             * Megjelenitjük a listába a játékokat és update üzenetetet helyezünk ki
                             */
                            else if (command.equals(Command.AVAILABLE_GAME.toString())) {
                                Platform.runLater(() -> {

                                    viewController.addGame(tmp[1]);
                                    viewController.setMainLabelText("There's a new game available on the server "+tmp[1]);
                                });
                            }
                            /**
                             * Ha removeolni akartunk egy játékot, akkor meghivjuk a controller remove metódusát,
                             * és frissitjük a guin is
                             */
                            else if (command.equals(Command.GAME_REMOVED.toString())) {
                                Platform.runLater(() -> {

                                    viewController.removeGame(tmp[1]);
                                });
                            }
                            /**
                             * Ha removeolni akartunk, de nem mi voltunk a host
                             */
                            else if (command.equals(Command.YOUR_NOT_THE_HOST_OF_THIS_GAME.toString())) {
                                Platform.runLater(() -> {

                                    viewController.setMainLabelText("You are not the host of this game");
                                });
                            }
                            /**
                             * Update, hogy sikerült removeolni
                             */
                            else if (command.equals(Command.GAME_DELETED_UPDATE.toString())) {
                                Platform.runLater(() -> {

                                    viewController.setMainLabelText("Removed successfully!");
                                });
                            }
                            /**
                             * Itt már a maga a játék Scene, a gamesWindowController van
                             * Names Update amikor valaki felcsatlakozik(játékosok neveit jelenitjük)
                             */
                            else if (command.equals(Command.NAMES_UPDATE.toString())) {
                                Platform.runLater(() -> {
                                gameWindowController.setPlayerNames(tmp[1],tmp[2]);


                                });
                            }

                            /**
                             * Ha a játékba beléptünk,
                             * akkor a Controller mezőit beállitjuk(shipek száma, boardsize)
                              */
                            else if (command.equals(Command.JOINED.toString())) {
                                int boardinho = Integer.parseInt(tmp[2]);
                                int ship2 = Integer.parseInt(tmp[3]);
                                int ship3 = Integer.parseInt(tmp[4]);
                                int ship4 = Integer.parseInt(tmp[5]);
                                int ship5 = Integer.parseInt(tmp[6]);
                                gameWindowController.setBoardsize(boardinho);
                                gameWindowController.setShipNumbers(ship2,ship3,ship4,ship5);

                                /**
                                 * Üdvözlő üzenet a játékba
                                 */
                                Platform.runLater(() -> {
                                    viewController.setMyGame(tmp[1]);
                                   gameWindowController.setWelcomeName("Welcome to the game: "+tmp[7]);

                                });
                            }
                            /**
                             * Ha már csatlakoztunk, és egy másik játékos csatlakozik, akkor ezt megjelenitjük
                             */
                            else if(command.equals(Command.OPPONENT_JOINED.toString())){
                                Platform.runLater(() -> {

                             gameWindowController.gameUpdate("New opponent connected: "+tmp[2]);
                                });

                            }
                            /**
                             * A hajók tipusát és darabszámát frissitjük a kliens inputnak megfelelően
                             */
                            else if (command.equals(Command.SHIPS_UPDATE.toString())) {
                                Platform.runLater(() -> {
                                    gameWindowController.setShip2Nr(tmp[1]);
                                    gameWindowController.setShip3Nr(tmp[2]);
                                    gameWindowController.setShip4Nr(tmp[3]);
                                    gameWindowController.setShip5Nr(tmp[4]);

                                });
                            }
                            /**
                             * Ha mindenki csatlakozott, akkor megkezdődhet a hajó elhelyezés
                             * A setshipplacement boolean állitódik falseról truera
                             */
                            else if (command.equals(Command.START_SHIP_PLACING.toString())) {
                                gameWindowController.setShipPlacement(true);

                            }
                            /**
                             * Ha a szerver helyeselte a hajó elhelyezését, akkor megjelenitjük a gui-n is,
                             * illetve updaet üzenet formájában is
                             */
                            else if (command.equals(Command.PLACEMENT_SUCCEED.toString())) {

                                Platform.runLater(() -> {

                                    gameWindowController.myBoard.placeCurrentShip();
                                    gameWindowController.gameUpdate("Placement successful!");
                                });
                            }
                            /**
                             * Ha nem sikerült, hiba update üzenet
                             */
                            else if (command.equals(Command.PLACEMENT_FAILED.toString())) {
                                Platform.runLater(() -> {
                                   gameWindowController.gameUpdate("Placement failed! Try somewhere else!");
                                });
                            }
                            /**
                             * Ha mindkét player elhelyezte a hajóit, a shipPlaacement véget ért
                             */
                            else if (command.equals(Command.ALL_SHIPS_PLACED.toString())) {
                                gameWindowController.setShipPlacement(false);

                            }
                            /**
                             * Elkezdődhet a lövés phase, a gameRunning boolean true lesz
                             * Mutatjuk hogy kinek van éppen a köre
                             */
                            else if (command.equals(Command.START_SHOOTING.toString())) {
                                gameWindowController.setGameRunning(true);
                                Platform.runLater(() -> {
                                    gameWindowController.gameUpdate("Ships placed, let the shooting begin!");
                                    gameWindowController.setPlayerTurn("Player's turn: "+tmp[2]);
                                });
                            }
                            /**
                             * A controllerhez változtatjuk a MyTurn booleant, ha nem az én köröm van,
                             * nem enged az ellenfél táblájára kattintani
                             *
                             */
                            else if (command.equals(Command.INITIAL_TURN_MSG.toString())) {
                                gameWindowController.setMyTurn(true);
                                Platform.runLater(() -> {

                                });
                            }
                            else if (command.equals(Command.INITIAL_NOT_YOUR_TURN_MSG.toString())) {
                                gameWindowController.setMyTurn(false);
                                Platform.runLater(() -> {

                                });
                            }
                            /**
                             * Update message
                             */
                            else if (command.equals(Command.SHOOTING_TURN_UPDATE.toString())) {
                                Platform.runLater(() -> {

                                    gameWindowController.setPlayerTurn("Player's turn: "+tmp[1]);
                                });
                            }
                            /**
                             * Ha eltaláltuk, akkor megjelenitjük a lángocskát a gui-n,
                             * update mssage, illetve pontot adunk
                             */
                            else if (command.equals(Command.SHOT_HIT.toString())) {
                                Platform.runLater(() -> {
                                    gameWindowController.gameUpdate("You succesfully hit a ship! Point added");
                                    gameWindowController.helpBoard.repaintOnHit(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                                    gameWindowController.pointUpdatePlayer1(tmp[3]);
                                    gameWindowController.pointUpdatePlayer2(tmp[4]);
                                });

                        }
                            /**
                             * Ha eltalált az ellenfél, akkor megjelenitem a saját táblámon
                             */
                            else if (command.equals(Command.OPPONENT_HIT_YOU.toString())) {
                            Platform.runLater(() -> {
                                gameWindowController.gameUpdate("Opponent hit you");
                             gameWindowController.myBoard.repaintOnHit(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                                gameWindowController.pointUpdatePlayer1(tmp[3]);
                                gameWindowController.pointUpdatePlayer2(tmp[4]);
                            });
                        }
                            /**
                             * Ha nem talált, akkor a megfelelő X jelet tesszük ki mindenkinek a segédtáblára, és update message
                             */
                            else if (command.equals(Command.SHOT_MISSED.toString())) {
                                Platform.runLater(() -> {
                                    gameWindowController.gameUpdate("You missed!");
                                    gameWindowController.helpBoard.repaintOnMissed(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]));
                                });
                            }

                            /**
                             * Éppen körön levő játékos kezelő message
                             * Emellett itt valósitottam meg az automatikus lövést is
                             * Nem müködik tökéletesen, ezért tesztelés szempontjából:
                             * Csak akkor lő a computer helyettünk,
                             * ha a közvetlen mouse utáni lövésünk után checkeljük a Timer checkboxot
                             * Ekkor a következő körömbe a gép lőhet helyettem(ha nem lőttem addig)
                             */
                            else if (command.equals(Command.YOUR_TURN.toString())) {

                                gameWindowController.setMyTurn(true);


                                if(gameWindowController.getTimerBox().isSelected()) {
                                    timer = new Timer();
                                    if (gameWindowController.isMyTurn()) {

                                        Random rand = new Random();
                                        TimerTask shootComputer = new TimerTask() {
                                            public void run() {

                                                if (gameWindowController.isGameRunning()) {
                                                    if (gameWindowController.isMyTurn()) {
                                                        Platform.runLater(() -> {
                                                            gameWindowController.gameUpdate("LOTT A GEP HELYETTED");
                                                        });
                                                        Package pack = new Package(Command.SHOOT.toString(), rand.nextInt(5), rand.nextInt(5));
                                                        gameWindowController.getClientSocket().sendMessage(pack.toString());
                                                        timer.cancel();

                                                    }

                                                }
                                            }

                                        };
                                        timer.scheduleAtFixedRate(shootComputer, 15000, 15000);
                                    }
                                }
                                /**
                                 * A myTurn falsera állitódik, nem kezeli az ellenfél boardjára kattintott üzeneteket ekkor
                                  */
                            } else if (command.equals(Command.NOT_YOUR_TURN.toString())) {


                                gameWindowController.setMyTurn(false);

                            }
                            /**
                             * Ha nyertem, megjelenik egy üzenet, és többet nem lőhetek
                             */
                            else if (command.equals(Command.YOU_WIN.toString())) {
                                gameWindowController.setGameRunning(false);
                                Platform.runLater(() -> {

                                    gameWindowController.gameUpdate("YOU WON THE GAME");

                                });
                            }
                            /**
                             * Ha vesztettem, megjelenik egy üzenet, és többet nem lőhetek
                             */
                            else if (command.equals(Command.YOU_LOSE.toString())) {
                                gameWindowController.setGameRunning(false);
                                Platform.runLater(() -> {

                                    gameWindowController.gameUpdate("YOU LOST");

                                });
                            }

                        }
                        }

                } catch (IOException e) {
                    e.printStackTrace();

                } finally {
                    return null;
                }
            }
        };
    }

    /**
     * Setter a controllerekre
     * @param viewController
     */
    public void setViewController(ClientLoginController viewController) {
        this.viewController = viewController;
    }

    public void setViewController(GameWindowController gameWindowController) {
        this.gameWindowController = gameWindowController;
    }

    /**
     * A socket gettere
     * @return
     */
    public ClientSocket getClientSocket() {
        return this.clientSocket;
    }

}



