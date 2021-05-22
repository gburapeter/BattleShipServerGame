package ghipe1.Client;


import ghipe1.Utils.Coordinate;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Kliens oldali cella, amely a Java FX Rectangleből származik le
 */

public class ClientCell extends Rectangle {
    /**
     * Tartalmazza a pozicióját a cellának,
     * és hogy használták-e már(lőttek e már ide)
     *
     */
    private final Coordinate position;
    private boolean wasUsed = false;
    Image img = new Image("/ghipe1/res/fire.png");
    Image img2 = new Image("/ghipe1/res/miss.png");
    public ClientCell(int x, int y){
        super(25,25);
        position = new Coordinate(x,y);
        this.setFill(Color.LIGHTGREEN);
        this.setStroke(Color.BLACK);
    }

    public boolean wasUsed(){
        return this.wasUsed;
    }

    /**
     * Getter-setterek
     * A játék segédtábláján megjelenik az összes player össze lövése
     * Ha valahova lőttek és talált volt, akkor oda többet nem enged lőni
     * Ha missed, akkor még lőhet más játékos oda
     *
     */

    public void setShot(){

       this.wasUsed = true;
    }
    public void setColorAndUsed(Color fill){
        this.wasUsed = true;
        this.setFill(Color.RED);
    }

    /**
     * Képeket beállíása
     *
     */
    public void setImage()
    {
        this.setFill(new ImagePattern(img));
    }
    public void setImage2() {

        this.setFill(new ImagePattern(img2));
    }

    /**
     * Koordináta getterek
     * @return
     */
    public int getXCoordinate() {
        return position.getX();
    }

    public int getYCoordinate() {
        return position.getY();
    }


}
