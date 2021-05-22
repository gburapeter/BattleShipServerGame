package ghipe1.Client.Controller;

import ghipe1.Client.ClientSocket;
import ghipe1.Client.GameService;
import ghipe1.Utils.Command;
import ghipe1.Utils.Package;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A masodik scene controllere
 * Mezői tartalmazzák a megjelenitendő java fx elemeket
 */
public class ClientLoginController {

    @FXML
    private Button exitBtn;

    @FXML
    private Button createBtn;

    @FXML
    private Button startGame;

    @FXML
    private ComboBox<String> gameMode;

    @FXML
    private ComboBox<String> gamesCombo;

    @FXML
    private Label mainLabel;

    @FXML
    private TextField totalPlayers;

    @FXML
    private TextField gameNameField;

    @FXML
    private TextField ship2Field;

    @FXML
    private TextField ship3Field;

    @FXML
    private TextField ship4Field;

    @FXML
    private TextField ship5Field;


    /**
     * A gameService mező beállitja a GameServicet erre a controllerre
     * A gameList tartalmazza a megjelenitendő játékokat
     * Tartalmaz egy gameWindowControllert, ami a következő scenere utal
     */

    private String nickName;
    private GameService gameService;
    private ClientSocket clientSocket;
    private ObservableList<String> gameList;
    private  GameWindowController gameWindowController;
    private String myGame;
    private int boardsize;


    /**
     * A controller betöltődésekor, megjelenitjük a gameket
     * Illetve a játékmódhoz beillesztjük a két játékmódot
     * @throws IOException
     */


    public void initialize() throws IOException {

        gameMode.getItems().addAll("Last man standing", "Points game");
        this.gameList = FXCollections.observableArrayList();
        this.gamesCombo.setItems(gameList);

    }


    public void setMyGame(String name){
        this.myGame = name;

    }

    /**
     * Az előző sceneből való átlépéskor, átállitjuk a megfelelő mezőket
     * A gameservicet, illetve a clientsocketet, hogy kezelje a küldéseket
     * @param service
     * @param name
     */

    public void setServiceAndName(GameService service, String name){
        this.gameService=service;
        this.gameService.start();
        this.gameService.setViewController(this);
        this.nickName =name;
        this.clientSocket = this.gameService.getClientSocket();
        Package pack = new Package(Command.LOGIN.toString(), nickName);
        this.clientSocket.sendMessage(pack.toString());
    }

    /**
     * Ezt a Lobby button miatt hoztam létre:
     * az utolsó sceneből(GameWindow) a lobby button segitségével visszaléphetünk erre a scenera
     * @param service
     * @param name
     */
    public void setServiceAndName2(GameService service, String name){
        this.gameService=service;
        this.gameService.setViewController(this);
        this.nickName =name;
        this.clientSocket = this.gameService.getClientSocket();
    }


    /**
     * Üdvözlő üzenet
     */

    public void setText(){
        mainLabel.setText("Welcome "+nickName.toUpperCase()+"! "+mainLabel.getText());
    }

    /**
     * Update üzeneteket jelenitünk meg
     * @param message
     */
    public void setMainLabelText(String message){
        mainLabel.setText(message);
    }

    /**
     * Ha játékot hoztunk létre, hozzáadjuk a listához, és megjelnitjük
     * @param gameName
     */

    public void addGame(String gameName){
       this.gameList.add(gameName);
        this.gamesCombo.setItems(this.gameList);

    }

    /**
     * Ha játékot töröltünk ki, kiszedjük al istából, és megjelenitjük az új listát
     * @param gameName
     */
    public void removeGame(String gameName){
        this.gameList.remove(gameName);

        this.gamesCombo.setItems(this.gameList);

    }

    /**
     * OnAction metódusok
     *
     */


    /**
     * Ha a CreateGamere kattintunk ez a metódus hivódik meg
     * Ha egy mezőt üreseen hagytunk, vagy rossz érték iródott
     * be (pl egyik shiphez nulla, vagy minusz érték) akkor Alertekkel jelzem
     *
     * @param event
     */
    @FXML
    private void createGame(ActionEvent event) {

        String gameName;
        String gameType;
        int nrPlayer;
        int ship2;
        int ship3;
        int ship4;
        int ship5;
        try {
            /**
             * Megpróbáljuk kiolvasni az értékeket, és catcheljük a hibát
             */
         gameName = gameNameField.getText();
         gameType = (String) gameMode.getValue();
         nrPlayer = Integer.parseInt(totalPlayers.getText());
         ship2 = Integer.parseInt(ship2Field.getText());
         ship3 = Integer.parseInt(ship3Field.getText());
         ship4 = Integer.parseInt(ship4Field.getText());
         ship5 = Integer.parseInt(ship5Field.getText());


        }
        catch(IllegalArgumentException e){
            alertCreator(Alert.AlertType.WARNING,"Empty field alert", "You missed a field!",
                    "Please enter a value in every field and choose the gamemode");
            return;

        }
        if(gameName.isEmpty()){
            alertCreator(Alert.AlertType.WARNING, "Empty field", "Gamename not written",
                    "Please enter a game name");
            return;
        }
        if(this.gameMode.getSelectionModel().isEmpty()){
            alertCreator(Alert.AlertType.INFORMATION, "GAMEMODE", "None selected",
                    "Please select a gamemode!");
            return;
        }
        /**
         * Két metódus a shipek darabszámának ellenőrzésére( ha túl nagy, mondjuk 15 az egyik, vagy kisebb egyenlő nulla)
         */
        if( numberLowAlert(ship2,ship3,ship4,ship5)) return;
        if( numberHighAlert(ship2,ship3,ship4,ship5)) return;

        /**
         * Végül elküldjük az üzenetet, hogy létrehozódjon a játék
         */
                Package pack = new Package(Command.CREATE_GAME.toString(),
                        gameName, gameType, ship2, ship3, ship4, ship5, nrPlayer);
                boardsize = (int) Math.ceil(((ship2*(2-1)+ship3*(3-1)+ship4*(4-1)+ship5*(5-1))*Math.ceil((double) nrPlayer/2)+1)/2);

                this.clientSocket.sendMessage(pack.toString());

        }

    /**
     * Ha a removeGame buttonra kattintunk, ez a metdus hivódik meg
     * Ha nem törölnénk semmit, alert jelenik meg
      * @param event
     * @throws IOException
     */
    @FXML
    private void removeGame(ActionEvent event) throws IOException {
        if(this.gamesCombo.getSelectionModel().isEmpty()){
            alertCreator(Alert.AlertType.INFORMATION, "GAME ERROR", "None selected",
                    "Please select a game to remove");
            return;
        }
        else{
            String tmp = this.gamesCombo.getSelectionModel().getSelectedItem();
            Package pack = new Package(Command.REMOVE_GAME_FROM_LIST.toString(),tmp);
            this.clientSocket.sendMessage(pack.toString());
        }
    }

    /**
     * Ha egy létrehozott játékra csatlakozunk, hivódik meg a joinGame,
     * ekkor átmegyünk a Game scenere
     * @param event
     * @throws IOException
     */
    @FXML
    private void joinGame(ActionEvent event) throws IOException {
        if(this.gamesCombo.getSelectionModel().isEmpty()){
            alertCreator(Alert.AlertType.INFORMATION, "GAME ERROR", "Empty gamelist field",
                    "Please create a game or join one");
            return;
        }

        else{
            /**
             * Ha nem volt hiba, megjelenitődik az új scene
             */
            Stage stage;
            stage = (Stage) this.startGame.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../res/GameWindow.fxml"));
            Parent root = (Parent) loader.load();
            //Átadjuk a következő Controllernek
            gameWindowController = loader.getController();
            gameWindowController.setServiceAndName(this.getGameService(), "asd");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            /**
             * Elküldjük, hogy joinoltunk
             */
            String tmp = this.gamesCombo.getSelectionModel().getSelectedItem();
            Package pack = new Package(Command.JOIN_TO_GAME.toString(),tmp);
            this.clientSocket.sendMessage(pack.toString());
        }
    }


    public void alertCreator(Alert.AlertType alertType, String Title, String Header, String Content){
        Alert alert = new Alert(alertType);
        alert.setTitle(Title);
        alert.setHeaderText(Header);
        alert.setContentText(Content);
        alert.showAndWait();
        return;
    }

    /**
     * Ship darabszámának kezelésére
     * @param nr1
     * @param nr2
     * @param nr3
     * @param nr4
     * @return
     */
    private boolean numberLowAlert(int nr1, int nr2, int nr3, int nr4) {
        if (nr1<=0 || nr2<=0 || nr3<=0 || nr4<=0) {
            alertCreator(Alert.AlertType.WARNING,"Wrong ship number", "Zero or lower input",
                    "Please enter a bigger number than 0");
            return true;
        }
        return false;
    }

    private boolean numberHighAlert(int nr1, int nr2, int nr3, int nr4) {
        if (nr1>=6 || nr2>=6 || nr3>=6 || nr4>=6) {
            alertCreator(Alert.AlertType.WARNING,"Wrong ship number", "High input",
                    "Please try a smaller one");
            return true;
        }
        return false;
    }

    //Getterek
    public GameService getGameService() {
        return gameService;
    }
    public String getNickName() {
        return nickName;
    }

}