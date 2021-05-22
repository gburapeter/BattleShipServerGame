package ghipe1.Client;



import ghipe1.Client.Controller.GameWindowController;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Kliens oldali 'saját' board
 * Tartalmaz egy VBoxot, amelyet feltöltök Cell-ákkal
 */
public class PlayerBoard extends Parent {
    protected VBox rows = new VBox();

    /**
     * Tartalmaz egy boardsize- tábla mérete mezőt
     * shipsLeft - hátralevő shipek száma
     */
    private ClientCell currentCell;
    private int shipSizes[];
    private int boardsize;
    private int shipsLeft;
    private int currentPlacingSize;
    private boolean isCurrentVertical = true;


    public PlayerBoard(EventHandler<? super MouseEvent> mouseClickHandler,GameWindowController viewController,
                       int boardsize, int ship2, int ship3, int ship4, int ship5) {
        this.boardsize=boardsize;
        /**
         * A boarsizeot a kiszámolt képlet alapján adjuk át
         * Feltöltjük a táblát a megfelelő cellákkal
         * És eventHandlert helyezünk rájuk
         */
        for (int y = 0; y < boardsize; y++) {
            HBox row = new HBox();
            for (int x = 0; x < boardsize; x++) {
                ClientCell cell = new ClientCell(x, y);
                cell.setOnMouseClicked(mouseClickHandler);
                row.getChildren().add(cell);
            }
            rows.getChildren().add(row);
        }
        this.currentPlacingSize=1;
        this.getChildren().add(rows);

        /**
         *  A megfelelő 2-3-4-5ös tipusú shipekhez olyan megvalósitást csináltam, hogy:
         *   Egy arraybe tárolom
         *   0-as indexen a 2-es hosszúságú ship - benne ennek a darabszámát
         *   1-as indexen a 3-es hosszúságú ship - benne ennek a darabszámát
         *   2-as indexen a 4-es hosszúságú ship - benne ennek a darabszámát
         *   3-as indexen a 5-es hosszúságú ship - benne ennek a darabszámát
         */
        this.shipSizes = new int[4];
        this.shipSizes[0]=ship2;
        this.shipSizes[1]=ship3;
        this.shipSizes[2]=ship4;
        this.shipSizes[3]=ship5;

        /**
         * Összesen az egyenkénti darabszámok szummája
         */
        this.shipsLeft=ship2+ship3+ship4+ship5;


    }

    /**
     * Beállitjuk a megfelelő láng és X képet
     * @param x
     * @param y
     */
    public void repaintOnHit(int x, int y){
        ClientCell cell = getCell(x,y);
        cell.setShot();
        cell.setImage();

    }
    public void repaintOnMissed(int x, int y){
        ClientCell cell = getCell(x,y);
        cell.setShot();
        cell.setImage2();
    }


    /**
     * Visszaadja az adott kattintott cellát
     * @param x
     * @param y
     * @return
     */
    public ClientCell getCell(int x, int y){
        return (ClientCell)((HBox)rows.getChildren().get(y)).getChildren().get(x);
    }


    /**
     * Elhelyezzük a hajót
     * A hátralevő hajók számát csökkentjük együtt
     *
     *   this.shipSizes[this.currentPlacingSize-2]--;
     *  Tehát a currentPlacingSize az 5-ös ship, akkor az én tárolási módszerembe
     *  a 3-as indexen levő darabszámot csökkentem( itt van az 5-ös ship)
     */

    public void placeCurrentShip(){
        if(this.isCurrentVertical){ //== vertical
            for(int i=this.currentCell.getYCoordinate(); i<this.currentCell.getYCoordinate()+this.currentPlacingSize; i++){
                getCell(this.currentCell.getXCoordinate(),i).setColorAndUsed(Color.BURLYWOOD);
            }

        this.setShipsLeft(this.getShipsLeft()-1);
        this.shipSizes[this.currentPlacingSize-2]--;

        }
        else { //horizontal
            for (int i = this.currentCell.getXCoordinate(); i < this.currentCell.getXCoordinate()+this.currentPlacingSize; i++) {
                getCell(i, this.currentCell.getYCoordinate()).setColorAndUsed(Color.BURLYWOOD);
            }

        }
    }

    /**
     * Az adott indexen levő darabszámot adja vissza
     * Pl. ha az index 1, akkor a 3 hosszúságú hajók darabszámát kapjuk vissza
     * @param index
     * @return
     */
    public int getShipSizes(int index) {

        return shipSizes[index];
    }

    //Getter-setterek

    public ClientCell getCurrentCell() {
        return currentCell;
    }

    public int getShipsLeft() {

        return shipsLeft;
    }

    public void setShipsLeft(int shipsLeft) {

        this.shipsLeft = shipsLeft;
    }

    public void setCurrentPlacingSize(int currentPlacingSize) {
        this.currentPlacingSize = currentPlacingSize;
    }
    public void setCurrentCell(ClientCell currentCell) {
        this.currentCell = currentCell;
    }

    public void setCurrentVertical(boolean currentVertical) {
        isCurrentVertical = currentVertical;
    }

    public int getCurrentPlacingSize(){return this.currentPlacingSize;}


}