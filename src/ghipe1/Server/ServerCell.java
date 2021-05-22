package ghipe1.Server;

/**
 * Szerver oldali Cell osztály
 * Alapjáraton nem tartalmaz hajót, ha viszont rákerül akkor ez megváltozik
 */

public class ServerCell {
    private Ship ship = null;

    public ServerCell() {}

    /**
     * Az adott hajó tartalmazni fog hajót
     * @param s
     */
    public void placeShip(Ship s){
        this.ship = s;
    }


    /**
     * Visszaadja hogy van e rajta hajó
     * @return
     */
    boolean isShip() {
        if(this.ship == null)
            return false;
        return true;
    }


    public Ship getShip() {
        return ship;
    }


}
