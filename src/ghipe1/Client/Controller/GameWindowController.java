package ghipe1.Client.Controller;

import ghipe1.Client.ClientCell;
import ghipe1.Client.ClientSocket;
import ghipe1.Client.HelpBoard;
import ghipe1.Client.GameService;
import ghipe1.Client.PlayerBoard;
import ghipe1.Utils.Command;
import ghipe1.Utils.Package;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * A harmadik scene, maga a játék megjelenítése
 * Tartalmaz egy timert, amit ha checkkolunk akkor lő a gép is, ha mi nem lőttünk adott ideig
 */
public class GameWindowController implements Initializable {


    //FXML elemek
    @FXML
    private VBox VBoxMy;
    @FXML
    private VBox VBoxEnemy;
    @FXML
    private Button initBoard;
    @FXML
    private Label player1Name;
    @FXML
    private Label player2Name;
    @FXML
    private ComboBox shipSelector;
    @FXML
    private CheckBox TimerBox;
    @FXML
    private Label ship2Nr;
    @FXML
    private Label ship3Nr;
    @FXML
    private Label ship4Nr;
    @FXML
    private Label ship5Nr;
    @FXML
    private Label gameUpdates;
    @FXML
    private Label pointsPlayer1;
    @FXML
    private Label pointsPlayer2;
    @FXML
    private Label nameUpdate;
    @FXML
    private Label playerTurn;


    //Mezők
    private String nickName;
    private GameService gameService;
    private ClientSocket clientSocket;

    //Saját és segédtábla
    public PlayerBoard myBoard;
    public HelpBoard helpBoard;

    /**
     * Kezeljük, hogy éppen hajókat helyezhetünk-e, a mi körünk van-e
     * lőhetünk-e,
     * és hogy vége van e a játéknak
     */
    private boolean shipPlacement = false;
    private boolean myTurn = false;
    private boolean gameRunning;


    /**
     * A tábla méretét, illetve a hajók darabszámát tartalmazó mezők
     */
    private int boardsize;
    private int ship2,ship3,ship4,ship5;
    private String ship;

    /**
     * Initialize metódus, feltöltjük a shippeket a játékhoz
     * @param url
     * @param resourceBundle
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       shipSelector.getItems().addAll("Ship 2", "Ship 3", "Ship 4", "Ship 5");
       ship = null;

    }


   //OnAction metódusok

    /**
     * A játék elején, ahhoz hogy megjelenjenek a boardok, az InitBoard buttont kell megnyomni
     * Ekkor létrejön egy saját és egy segéd tábla.
     * Sajátra helyezhetjük el a hajóinkat kiválasztva a listából
     */

   public void initBoards() {

       myBoard = new PlayerBoard(event -> {
           /**
            * Ha még nem csatlakozott az ellenfél, vagy már lövés van akkor nem kattintható
            */
           if (!shipPlacement) {
               gameUpdates.setText("Waiting for an opponent");
               return;
           }

           /**
            * Megkapjuk a cellát
            */
           ClientCell cell = (ClientCell) event.getSource();
           if (cell.wasUsed()) {
               return;
           }

           /**

            *  A megfelelő 2-3-4-5ös tipusú shipekhez olyan megvalósitást csináltam, hogy:
            *   Egy arraybe tárolom
            *   0-as indexen a 2-es hosszúságú ship - benne ennek a darabszámát
            *   1-as indexen a 3-es hosszúságú ship - benne ennek a darabszámát
            *   2-as indexen a 4-es hosszúságú ship - benne ennek a darabszámát
            *   3-as indexen a 5-es hosszúságú ship - benne ennek a darabszámát
            */

           ship = (String) shipSelector.getValue();
          int index=-1;
           if (ship==null){
               Alert alert = new Alert(Alert.AlertType.WARNING);
               alert.setTitle("SHIP ERROR");
               alert.setHeaderText("SELECT A SHIP");
               alert.setContentText("Press the list and select a ship");
               alert.showAndWait();
               return;
           }
           else
          if(ship.equals("Ship 2")){
              index = 0;
          }
          else if (ship.equals("Ship 3")){
              index = 1;
          }
          else if (ship.equals("Ship 4")){
              index = 2;
          }
          else if (ship.equals("Ship 5")){
              index = 3;
          }
          else if (ship.equals(null)){
              Alert alert = new Alert(Alert.AlertType.WARNING);
              alert.setTitle("SHIP ERROR");
              alert.setHeaderText("SELECT A SHIP");
              alert.setContentText("Press the list and select a ship");
              alert.showAndWait();
              return;
          }

           /**
            * Az adott cellát jelölöm ki, és megnézem hogy horizontal vagy vertical,
            * bal vagy jobb mouse clicktől függően
            */
           myBoard.setCurrentCell(cell);
           boolean vertical = (event.getButton() == MouseButton.PRIMARY);
           myBoard.setCurrentVertical(vertical);

           /**
            * Ha még van elhelyezendő Shipem az adott tipusból:
            * pl. ha 2 hosszúságó shipből 1 volt, és ezt elhelyeztem,
            * ha ezután még szeretném ezt elhelyezni, akkor alertet dob
            */
           if(myBoard.getShipSizes(index) > 0) {
               myBoard.setCurrentPlacingSize(index+2);

               /**
                * Elküldjük az üzenetet
                */
               Package pack = new Package(Command.PLACE_A_SHIP.toString(), Boolean.toString(vertical),
                       cell.getXCoordinate(), cell.getYCoordinate(),
                       myBoard.getCurrentPlacingSize());
               this.clientSocket.sendMessage(pack.toString());

           }
           else{
               Alert alert = new Alert(Alert.AlertType.WARNING);
               alert.setTitle("SHIP ERROR");
               alert.setHeaderText("NO MORE SHIPS");
               alert.setContentText("Ezeket a shippeket kiraktad mar");
               alert.showAndWait();
               return;
           }
       }, this,boardsize,this.ship2,this.ship3,this.ship4,this.ship5);
       /**
        * A segédtáblát is létrehozzuk
        * Ha még nem a lövés fázis van, akkor enged oda kattintani
        * Ha nem a te köröd van, akkor se
        */
       helpBoard = new HelpBoard(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               if(!gameRunning){
                   gameUpdates.setText("The game is not running!");
                   return;
               }

               if (shipPlacement) {
                   gameUpdates.setText("Wait until everyone places their ships");
                   return;
               }
               if (!myTurn) {
                    gameUpdates.setText("It's not your turn!");
                   return;
               }
               /**
                * Lekérjük a cellát
                */
               ClientCell cell = (ClientCell) event.getSource();
               if (cell.wasUsed()) {
                   return;
               }
               /**
                * Elküldjük, hogy hova lövünk
                */
               Package pack = new Package(Command.SHOOT.toString(), cell.getXCoordinate(), cell.getYCoordinate());
               GameWindowController.this.clientSocket.sendMessage(pack.toString());
           }
       },boardsize);

       Label mylabel = new Label();
       mylabel.setText("Saját tábla");
       mylabel.setAlignment(Pos.CENTER_RIGHT);
       Label enemyLabel = new Label();
       enemyLabel.setText("Segédtábla");
       enemyLabel.setAlignment(Pos.CENTER_RIGHT);
       this.VBoxMy.getChildren().add(mylabel);
       this.VBoxMy.getChildren().add(myBoard);
       this.VBoxEnemy.getChildren().add(enemyLabel);
       this.VBoxEnemy.getChildren().add(helpBoard);
       /**
        * Disable, hogy ne lehessen többször létrehozni őket
        */
       initBoard.setDisable(true);
   }

    /**
     * Ezt magamnak tesztelés szempontjából hoztam ki, hogy ne kelljen mindig be-ki jelentkezni
     * Visszamehetünk a lobbyba, majd új gamet hozhatunk létre, stb
     * @param event
     * @throws IOException
     */
    @FXML
    private void goToLobby(ActionEvent event) throws IOException {

        Stage stage;
        stage = (Stage) this.initBoard.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../res/ClientLogin.fxml"));
        Parent root = loader.load();
        ClientLoginController clientController = loader.getController();
        clientController.setServiceAndName2(this.gameService, this.nickName);
        Scene scene = new Scene(root,600,600);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    //Update és welcoming message megjelenitő üzenetek:
    // pontok frissülnek, a játékosok nevei, hajószámok a kliens áltla megadott adatok által,
    //illetve hogy kinek a köre van éppen
   public void gameUpdate(String message){
        gameUpdates.setText(message);
   }

    public void pointUpdatePlayer1(String message){
        pointsPlayer1.setText(message);
    }
    public void pointUpdatePlayer2(String message){
        pointsPlayer2.setText(message);
    }

    public void setShip2Nr(String text) {
        ship2Nr.setText(text);
    }

    public void setShip3Nr(String text) {
        ship3Nr.setText(text);
    }
    public void setShip4Nr(String text) {
        ship4Nr.setText(text);
    }

    public void setShip5Nr(String text) {
        ship5Nr.setText(text);
    }

    public void setPlayerNames(String player1, String player2){
        player1Name.setText(player1);
        player2Name.setText(player2);
    }
    public void setWelcomeName(String name){
        this.nameUpdate.setText(name);
    }

    public void setPlayerTurn(String name){
        this.playerTurn.setText(name);
    }
    //

    //Beállitjuk, hogy a mi körünk van e, vagy hogy hajó elhelyezés
    public void setShipPlacement(boolean shipPlacement) {
        this.shipPlacement = shipPlacement;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }


   //Getter-setterek

    public void setShipNumbers(int ship2, int ship3, int ship4, int ship5) {
        this.ship2=ship2;
        this.ship3=ship3;
        this.ship4=ship4;
        this.ship5=ship5;

    }
    public String getNickName() {
        return nickName;
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }

    public CheckBox getTimerBox() {
        return TimerBox;
    }

    public void setBoardsize(int boardsize) {
        this.boardsize = boardsize;
    }
    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    /**
     * Az előző sceneből átlépve ide, ezzel a metódussal állitjuk be a gameServicet, és a controllerét
     * @param service
     * @param name
     */
    public void setServiceAndName(GameService service, String name){
        this.gameService=service;
        this.gameService.setViewController(this);
        this.nickName =name;
        this.clientSocket = this.gameService.getClientSocket();

    }
}
