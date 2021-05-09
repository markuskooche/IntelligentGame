import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.MapParser;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

public class MapParserTest {

    @Test
    @DisplayName("Tests if a simple map can be read in")
    void simpleMapTest() {
        byte[] map = {
                50, 10, 48, 10, 48, 32, 48, 10, 56, 32, 56, 10, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 10, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 10,
                48, 32, 48, 32, 48, 32, 49, 32, 50, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48, 32,
                48, 32, 50, 32, 49, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 10, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48
        };

        List<String> expectedResult01 = new LinkedList<>();
        expectedResult01.add("2");
        expectedResult01.add("0");
        expectedResult01.add("0 0");
        expectedResult01.add("8 8");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        expectedResult01.add("0 0 0 1 2 0 0 0 ");
        expectedResult01.add("0 0 0 2 1 0 0 0 ");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        expectedResult01.add("0 0 0 0 0 0 0 0 ");

        List<String> receivedResult = MapParser.createMap(map);
        MatcherAssert.assertThat(receivedResult, is(expectedResult01));

        expectedResult01.add("0 0 0 0 0 0 0 0 ");
        MatcherAssert.assertThat(receivedResult, is(not(expectedResult01)));
    }

    @Test
    @DisplayName("Tests if a bad formatted map can be read in")
    void badFormattedMapTest() {
        byte[] map = {
                50, 32, 32, 32, 32, 10, 32, 32, 32, 50, 53, 53, 10, 32, 32, 32, 50, 53, 53, 32,
                10, 10, 10, 50, 53, 53, 32, 10, 56, 32, 10, 10, 10, 56, 10, 32, 32, 32, 48, 32,
                48, 32, 48, 32, 48, 10, 10, 10, 32, 48, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48,
                32, 48, 32, 32, 32, 48, 32, 48, 10, 10, 32, 48, 32, 48, 32, 48, 10, 48, 32, 48,
                10, 10, 32, 48, 32, 48, 32, 48, 32, 48, 32, 10, 32, 32, 32, 32, 32, 32, 48, 32,
                48, 10, 48, 32, 32, 32, 32, 32, 32, 32, 48, 32, 48, 32, 49, 10, 10, 32, 50, 32,
                48, 32, 48, 32, 48, 10, 32, 32, 32, 32, 32, 32, 32, 48, 32, 48, 32, 48, 10, 10,
                10, 32, 50, 32, 49, 32, 48, 10, 10, 32, 48, 32, 48, 10, 48, 32, 48, 10, 10, 32,
                32, 32, 32, 32, 32, 32, 32, 48, 32, 48, 32, 48, 32, 10, 10, 48, 32, 48, 32, 48,
                10, 48, 32, 48, 10, 10, 32, 32, 32, 48, 32, 48, 32, 48, 10, 10, 32, 48, 32, 48,
                32, 48, 10, 32, 32, 32, 32, 32, 32, 32, 48, 32, 48, 32, 48, 32, 48, 10, 10, 32,
                48, 32, 48, 32, 48, 32, 48, 10, 10, 48, 32, 10, 48, 32, 55, 32, 60, 45, 62, 32,
                55, 10, 55, 32, 51
        };

        List<String> expectedResult02 = new LinkedList<>();
        expectedResult02.add("2");
        expectedResult02.add("255");
        expectedResult02.add("255 255");
        expectedResult02.add("8 8");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 0 1 2 0 0 0 ");
        expectedResult02.add("0 0 0 2 1 0 0 0 ");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 0 0 0 0 0 0 ");
        expectedResult02.add("0 0 7 <-> 7 7 3");

        List<String> receivedResult = MapParser.createMap(map);

        for (String l : receivedResult) {
            System.out.println(l);
        }

        MatcherAssert.assertThat(receivedResult, is(expectedResult02));

        expectedResult02.add("7 0 1 <-> 0 7 5");
        MatcherAssert.assertThat(receivedResult, is(not(expectedResult02)));
    }
}
