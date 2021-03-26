import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) {
        Game game;
        String filename = "E:\\OTH\\4 Semester\\FW - Reversi\\DeveloperMap.txt";
        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());

            game = new Game(file);
            System.out.println(game.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}