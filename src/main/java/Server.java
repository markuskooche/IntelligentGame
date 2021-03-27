import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Server {

    public static void main(String[] args) {
        Game game;
        String filename = "maps/benesTestMaps/EasyTestMap.map";
        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());

            game = new Game(file);
            Scanner scanner = new Scanner(System.in);
            String input;
            String[] arr;
            int x, y;

            System.out.println(game.getBoard());

            do {
                System.out.println("Please enter a move like 'x y player' (enter 'end' to end game)");
                input = scanner.nextLine();
                arr = input.split(" ");
                if(!arr[0].equals("end")) {
                    x = Integer.parseInt(arr[0]);
                    y = Integer.parseInt(arr[1]);
                    game.getBoard().executeMove(x, y, arr[2].charAt(0));
                    System.out.println(game.getBoard());
                }
            }while(!arr[0].equals("end"));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}