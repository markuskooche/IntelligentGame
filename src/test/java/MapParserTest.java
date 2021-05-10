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

        List<String> expectedResult = new LinkedList<>();
        expectedResult.add("2");
        expectedResult.add("0");
        expectedResult.add("0 0");
        expectedResult.add("8 8");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 1 2 0 0 0 ");
        expectedResult.add("0 0 0 2 1 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");

        List<String> receivedResult = MapParser.createMap(map);
        MatcherAssert.assertThat(receivedResult, is(expectedResult));

        expectedResult.add("0 0 0 0 0 0 0 0 ");
        MatcherAssert.assertThat(receivedResult, is(not(expectedResult)));
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

        List<String> expectedResult = new LinkedList<>();
        expectedResult.add("2");
        expectedResult.add("255");
        expectedResult.add("255 255");
        expectedResult.add("8 8");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 1 2 0 0 0 ");
        expectedResult.add("0 0 0 2 1 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 7 <-> 7 7 3");

        List<String> receivedResult = MapParser.createMap(map);
        MatcherAssert.assertThat(receivedResult, is(expectedResult));

        expectedResult.add("7 0 1 <-> 0 7 5");
        MatcherAssert.assertThat(receivedResult, is(not(expectedResult)));
    }

    @Test
    @DisplayName("Tests if a one line formatted map can be read in")
    void oneLineMapTest() {
        byte[] map = {
                50, 32, 53, 32, 49, 32, 51, 32, 56, 32, 56, 32, 98, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 99, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 49, 32, 50, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 50, 32, 49, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32,
                48, 32, 48, 32, 120, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 48, 32, 105, 32,
                48, 32, 48, 32, 55, 32, 60, 45, 62, 32, 55, 32, 55, 32, 51, 32, 55, 32, 48, 32,
                49, 32, 60, 45, 62, 32, 48, 32, 55, 32, 53
        };

        List<String> expectedResult = new LinkedList<>();
        expectedResult.add("2");
        expectedResult.add("5");
        expectedResult.add("1 3");
        expectedResult.add("8 8");
        expectedResult.add("b 0 0 0 0 0 0 c ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 1 2 0 0 0 ");
        expectedResult.add("0 0 0 2 1 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("0 0 0 0 0 0 0 0 ");
        expectedResult.add("x 0 0 0 0 0 0 i ");
        expectedResult.add("0 0 7 <-> 7 7 3");
        expectedResult.add("7 0 1 <-> 0 7 5");

        List<String> receivedResult = MapParser.createMap(map);
        MatcherAssert.assertThat(receivedResult, is(expectedResult));
    }
}
