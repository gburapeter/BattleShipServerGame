package ghipe1.Client.Controller;

import ghipe1.Client.ClientSocket;
import ghipe1.Client.GameService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Legelső scene
 * Itt iródik be a nickname és a port száma amire csatlakozunk
 */
public class StartUpMenuController {

    private ClientSocket clientSocket;

    @FXML
    private Button exitBtn;

    @FXML
    private Button cncBtn;

    @FXML
    private TextField portField;

    @FXML
    private TextField nickField;

    private Tooltip tooltip;

    private GameService service;

    private String nickName;

    /**
     * Intialize metódus, átadjuk a Servicet
     * Illetve az Exit-re kilép
     */

    public void initialize() {
        service = new GameService();
        this.clientSocket = this.service.getClientSocket();
        exitBtn.setOnAction(event-> System.exit(0));

    }

    /**
     * Ha rámegyünk a Connectre ez a metódus hivódik meg
     * Ha nem irtunk be nicknamet alertet kapunk, illetve ha
     * nem a megfelelő portszám iródik be(12345) akkor is
     * Emellett ha véletlenül inditanánk a játékot, de a szerver nem megy, ezt is jelezzük
     * @param event
     * @throws IOException
     */
    @FXML
    private void loadClientMenu(ActionEvent event) throws IOException {
        int port = 0;
        String nick = "";
        try {

            nick = this.nickField.getText();
            port = Integer.parseInt(this.portField.getText());

        }
        catch (IllegalArgumentException e) {
           if (portField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PORT ERROR");
                alert.setHeaderText("Empty port field");
                alert.setContentText("Kérlek irj be egy port számot");
                alert.showAndWait();
                return;
            }
        }
        if (nickField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("NICK ERROR");
            alert.setHeaderText("Empty Nick field");
            alert.setContentText("Kérlek irj be egy nicknamet");
            alert.showAndWait();
            return;
        }


        if (port != 12345)
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PORT ERROR");
                alert.setHeaderText("Wrong port number");
                alert.setContentText("Nem ez a szerver portja! (Hint:12345)");
                alert.showAndWait();
                return;
            }

        /**
         * Ha sikerül csatlakozni, akkor megjelenitjük a következő Scenet
         */
        boolean result = this.service.tryConnect(port);

        if(result) {
            Stage stage;
            stage = (Stage) this.cncBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../res/ClientLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            //Átadjuk a következő Controllernek
            ClientLoginController clientController = loader.getController();
            clientController.setServiceAndName(this.service, this.nickField.getText());


        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("SERVER ERROR");
            alert.setHeaderText("Server not running");
            alert.setContentText("Kérlek inditsd el először a servert");
            alert.showAndWait();
            return;
        }
    }

}
