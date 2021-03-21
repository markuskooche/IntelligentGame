import client.Game;

public class Main {

    public static void main(String[] args) {
        Game game = new Game("ReversiXT/src/main/resources/bomb.map");
        game.board.inversion(2);
        System.out.println(game.toString());
    }
}
