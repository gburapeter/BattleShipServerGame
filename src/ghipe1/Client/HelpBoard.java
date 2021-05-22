package ghipe1.Client;



import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


/**
 * Szerver oldali segédtábla
 * Vbox sorokkal, és táblamérettel
 */
public class HelpBoard extends Parent {
    protected VBox rows = new VBox();
    int boardsize;

    /**
     * Konstruktorba bellitjuk, hogy mouse clicket kezelje
     * @param mouseClickHandler
     * @param boardsize
     */
    public HelpBoard(EventHandler<? super MouseEvent> mouseClickHandler, int boardsize) {
        this.boardsize=boardsize;
        System.out.println(boardsize);
        for (int y = 0; y < boardsize; y++) {
            HBox row = new HBox();
            for (int x = 0; x < boardsize; x++) {
                ClientCell cell = new ClientCell(x, y);
                cell.setOnMouseClicked(mouseClickHandler);
                row.getChildren().add(cell);
            }
            rows.getChildren().add(row);
        }
        this.getChildren().add(rows);


    }

    /**
     * Visszaadja az adoot koordinátán levő cellát
     * @param x
     * @param y
     * @return
     */
    public ClientCell getCell(int x, int y){
        return (ClientCell)((HBox)rows.getChildren().get(y)).getChildren().get(x);
    }

    /**
     * Ha eltaláljuk beállitjuk a képet, és Useddá tesszük a cellát, többé ide nem lőhet senki
     * @param x
     * @param y
     */
    public void repaintOnHit(int x, int y){
        ClientCell cell = getCell(x,y);
        cell.setShot();
        cell.setImage();

    }
    /**
     * Ha eltaláljuk beállitjuk a képet, és nem tesszük Useddá a cellát, ide még lőhet más játékos
     * @param x
     * @param y
     */
    public void repaintOnMissed(int x, int y){
        ClientCell cell = getCell(x,y);
        cell.setImage2();
    }

}
