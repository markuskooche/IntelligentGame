import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Start {

    private static Game createGame(String filename){
        Game game = null;
        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());
            game = new Game(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return game;
    }

    public static void main(String[] args) {
        Game game = createGame("maps/initialMaps/europa.map");
        System.out.println(game.toString());

        Player player = game.getPlayer(1);

        System.out.println(player);
        game.getBoard().executeMove(player, true);
        System.out.println(player);
    }
}
