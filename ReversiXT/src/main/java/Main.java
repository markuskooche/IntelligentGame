import client.Game;

public class Main {

    public static void main(String[] args) {
        Game game = new Game("ReversiXT/src/main/resources/bomb.map");
        System.out.println(game.toString());
    }
}
