import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) throws IOException {
        Game game;

        //String filename = "maps/initialMaps/bomb.map";
        String filename = "maps/evilMaps/boeseMap08.map";

        Path path = Paths.get(filename);
        List<String> file = Files.lines(path).collect(Collectors.toList());

        game = new Game(file);
        System.out.println(game.getBoard());

        int currentPlayer = 1;

        while (true) {
            Player player = game.getPlayer((char) (currentPlayer + 48));
            game.getBoard().executeMove(player, true);

            currentPlayer = (currentPlayer % game.getPlayers().length) + 1;
        }

    }
}