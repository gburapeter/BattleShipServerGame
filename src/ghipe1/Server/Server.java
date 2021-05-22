package ghipe1.Server;


import ghipe1.Utils.Command;

import java.util.ArrayList;

/**
 * Server szál osztály, amely elinditja a Listenert és várja a csatlakozásokat
 * Tartalmazza a játékokat és a játékosokat
 */
public class Server extends Thread {
    private ArrayList<Player> players;
    private ArrayList<Game> games;


    public Server() {
        games = new ArrayList<>();
        players = new ArrayList<>();

    }

    /**
     * Elinditja a listenert
     */
    @Override
    public void run() {
        Server server = new Server();
        Thread listener = new Listener(server);
         listener.start();
    }

    /**
     * Ha gamet hoztunk létre, hozzáadjuk a szerver listához,
     * és kihirdetjük a playereknek, hogy új játék elérhető
     * @param game
     */
    public synchronized void addGame(Game game) {
        games.add(game);

        String info = Command.AVAILABLE_GAME.toString();
        info = info + "#" + game.getGameName();
        sendBroadcast(info);
    }

    /**
     * Ha törölni akarunk egy játékot
     * Kitöröljük a listből, illetve elküldjük minden playernek a commandot,
     * hogy kitörlődött
     * @param game
     */
    public synchronized void removeGame(Game game) {

        games.remove(game);

        String info = Command.GAME_REMOVED.toString();
        info = info + "#" + game.getGameName();
        sendBroadcast(info);
    }

    /**
     * Nem hozható létre két azonos nevü game
     * @param name
     * @return
     */
    public synchronized boolean checkGameNameAvailability(String name){
        for (Game g: games) {
            if(g.getGameName().equals(name))
                return false;
        }
        return true;
    }

    /**
     * Minden szerveren levő playernek elküldjük az üzenetet
     * @param message
     */
    public void sendBroadcast(String message){
        for (Player p: players) {

            p.sendToPlayer(message);
        }
    }


    /**
     * Hozzáadjuk a playerek listájához a playert
     * @param player
     */
    public void addPlayer(Player player){
        players.add(player);
    }

    /**
     * Megkeressük az adott gamet a szerveren
     * @param name
     * @return
     */
    public synchronized Game findGame(String name) {
        for (Game game: games) {
            if (game.getGameName().equals(name))
                return game;
        }
        return null;
    }

    //getterek
    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Game> getGames() {
        return games;
    }


    public static void main(String[] args) {

                Server server = new Server();
                server.start();

            }
        }

