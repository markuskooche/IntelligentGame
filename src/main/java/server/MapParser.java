package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * MapParser can be used completely static. It receives a byte array and validates the array if its a valid map.
 * The byte array will be converted to a List<String> the a correct map.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class MapParser {

    /**
     * Creates a map from a byte array.
     *
     * @param elements byte array from the map
     * @return a list of strings with each line
     */
    public static List<String> createMap(byte[] elements) {
        List<Byte> mapPieces = new ArrayList<>();
        int length = elements.length;

        int transitionPart = 0;
        int infoCounter = 0;
        int addedPieces = 0;

        int height = 0;
        int width = 0;
        int currentHeight = 0;
        int currentWidth = 0;

        for (int j = 0; j < length; j++) {
            byte currentPiece = elements[j];
            byte nextPiece = ((byte) '\n');

            if ((j + 1) < length) {
                nextPiece = elements[j + 1];
            }

            if (currentPiece == 32 || currentPiece == 10) {
                continue;
            }

            // reading the player amount
            if (infoCounter == 0) {
                // checks if current byte is a number
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    // checks if next byte is not a number
                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the overridestone amount
            else if (infoCounter == 1) {
                // checks if current byte is a number
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    // checks if next byte is not a number
                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the bomb amount and radius
            else if (infoCounter == 2) {
                if (addedPieces == 0) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                } else if (addedPieces == 1) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            infoCounter++;
                            addedPieces = 0;
                        }
                    }
                }
            }
            // reading the height and width
            else if (infoCounter == 3) {
                // check if there no added pieces in this line
                if (addedPieces == 0) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        height = updateLength(height, currentPiece);
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // check if there one added pieces in this line
                else if (addedPieces == 1) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        width = updateLength(width, currentPiece);
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            infoCounter++;
                            addedPieces++;
                        }
                    }
                }
            }
            // reading the field
            else if (infoCounter == 4) {
                // checks if read width is smaller than map width
                if (currentWidth < width) {
                    // checks if it is a valid piece of the map
                    if (isGamePiece(currentPiece)) {
                        mapPieces.add(currentPiece);
                        mapPieces.add(((byte) ' '));
                        currentWidth++;

                        // checks if the read width is equal to the map width
                        if (currentWidth == width) {
                            // added a newline to the array
                            mapPieces.add(((byte) '\n'));
                            currentHeight++;
                            currentWidth = 0;

                            // check if the map has been read completely
                            if (currentHeight == height) {
                                addedPieces = 0;
                                infoCounter++;
                            }
                        }
                    }
                }
            }
            // reading the transitions
            else if (infoCounter == 5) {
                // reading x1, y1 and r1
                if (addedPieces >= 0 && addedPieces < 3) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 0) {
                    // checks if it the front of a transition arrow '<-'
                    if (isTransitionArrowFront(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '<'));
                        mapPieces.add(((byte) '-'));
                        transitionPart++;
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 1) {
                    // checks if it the back of a transition arrow '->'
                    if (isTransitionArrowBack(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '>'));
                        mapPieces.add(((byte) ' '));
                        transitionPart++;
                    }
                }
                // reading x2
                else if (addedPieces == 3 && transitionPart == 2) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            transitionPart = 0;
                            addedPieces++;
                        }

                    }
                }
                // reading y2
                else if (addedPieces == 4) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // reading r2
                else if (addedPieces == 5) {
                    // checks if current byte is a number
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        // checks if next byte is not a number
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            addedPieces = 0;
                        }
                    }
                }
            }
        }

        // create a new char array
        int preparedLength = mapPieces.size();
        char[] preparedMapData = new char[preparedLength];

        // copy all pieces into the new char array
        for (int j = 0; j < preparedLength; j++) {
            byte currentByte = mapPieces.get(j);
            preparedMapData[j] = ((char) currentByte);
        }

        // create a string of the char array and split it
        String preparedMapString = String.valueOf(preparedMapData);
        String[] preparedMap = preparedMapString.split("\n");

        // return the map as a list
        return new LinkedList<>(Arrays.asList(preparedMap));
    }

    /**
     * Updating the Length (Width and Height) of the board.
     *
     * @param currentLength the current recognized length of the board
     * @param currentPiece the next byte of the array
     * @return the new calculated length of the board
     */
    private static int updateLength(int currentLength, byte currentPiece) {
        return (10 * currentLength) + (currentPiece - ((byte) '0'));
    }

    /**
     * Check if it is the first arrow of a transition.
     *
     * @param a first byte ('<' if returned true)
     * @param b second byte ('-' if returned true)
     * @return true if it is the front of the transition
     */
    private static boolean isTransitionArrowFront(byte a, byte b) {
        return ((a == ((byte) '<')) && (b == ((byte) '-')));
    }

    /**
     * Check if it is the second arrow of a transition.
     *
     * @param a first byte ('-' if returned true)
     * @param b second byte ('>' if returned true)
     * @return true if it is the back of the transition
     */
    private static boolean isTransitionArrowBack(byte a, byte b) {
        return ((a == ((byte) '-')) && (b == ((byte) '>')));
    }

    /**
     * Check if it is a number.
     *
     * @param a a byte
     * @return true if it is a number
     */
    private static boolean isNumeric(byte a) {
        return (a >= 48 && a <= 57);
    }

    /**
     * Check if it is not a number.
     *
     * @param a a byte
     * @return true if it is not a number
     */
    private static boolean isNotNumeric(byte a) {
        return (a < 48 || a > 57);
    }

    /**
     * Check if it is an game piece.
     * ('1 - 8', 'b', 'c', 'i', 'x', '-')
     *
     * @param a a byte
     * @return true if it is a game piece
     */
    private static boolean isGamePiece(byte a) {
        byte empty = ((byte) '-');
        byte b = ((byte) 'b');
        byte c = ((byte) 'c');
        byte i = ((byte) 'i');
        byte x = ((byte) 'x');

        // check if it is numeric but not 9 or if it is an special field or an hole
        return ((isNumeric(a) && a != 57) || (a == b || a == c || a == i || a == x || a == empty));
    }
}
