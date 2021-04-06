import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) {
        Game game;

        String filename = "maps/benesTestMaps/TestMapAnalyze.map";

        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());

            game = new Game(file);
            MapAnalyzer analyzer = new MapAnalyzer(game.getBoard());
            analyzer.createField();
            System.out.println(analyzer.toString());
            System.out.println(analyzer.calculateScoreForPlayer('1'));
        //game.executeMove('1');

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}