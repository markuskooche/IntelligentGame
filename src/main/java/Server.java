import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) {
        Game game;

        //String filename = "maps/benesTestMaps/TestOverrideStonesWithNoStones.map";
        //String filename = "maps/testMaps/transitions/map08.map";
        //String filename = "maps/testMaps/bonus.map";
        String filename = "maps/evilMaps/boeseMap01.map";

        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());

            game = new Game(file);
            game.executeMove('1');

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}