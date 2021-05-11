package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MapParser {

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
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the overridestone amount
            else if (infoCounter == 1) {
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the bomb amount and radius
            else if (infoCounter == 2) {
                if (addedPieces == 0) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                } else if (addedPieces == 1) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

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
                if (addedPieces == 0) {
                    if (isNumeric(currentPiece)) {
                        height = updateLength(height, currentPiece);
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                } else if (addedPieces == 1) {
                    if (isNumeric(currentPiece)) {
                        width = updateLength(width, currentPiece);
                        mapPieces.add(currentPiece);

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
                if (currentWidth < width) {
                    if (isGamePiece(currentPiece)) {
                        mapPieces.add(currentPiece);
                        mapPieces.add(((byte) ' '));
                        currentWidth++;

                        if (currentWidth == width) {
                            mapPieces.add(((byte) '\n'));
                            currentHeight++;
                            currentWidth = 0;

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
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 0) {
                    if (isTransitionArrowFront(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '<'));
                        mapPieces.add(((byte) '-'));
                        transitionPart++;
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 1) {
                    if (isTransitionArrowBack(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '>'));
                        mapPieces.add(((byte) ' '));
                        transitionPart++;
                    }
                }
                // reading x2
                else if (addedPieces == 3 && transitionPart == 2) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            transitionPart = 0;
                            addedPieces++;
                        }

                    }
                }
                // reading y2
                else if (addedPieces == 4) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // reading r2
                else if (addedPieces == 5) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

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

    private static int updateLength(int currentLength, byte currentPiece) {
        return (10 * currentLength) + (currentPiece - ((byte) '0'));
    }

    private static boolean isTransitionArrowFront(byte a, byte b) {
        return ((a == ((byte) '<')) && (b == ((byte) '-')));
    }

    private static boolean isTransitionArrowBack(byte a, byte b) {
        return ((a == ((byte) '-')) && (b == ((byte) '>')));
    }

    private static boolean isNumeric(byte a) {
        return (a >= 48 && a <= 57);
    }

    private static boolean isNotNumeric(byte a) {
        return (a < 48 || a > 57);
    }

    private static boolean isGamePiece(byte a) {
        byte empty = ((byte) '-');
        byte b = ((byte) 'b');
        byte c = ((byte) 'c');
        byte i = ((byte) 'i');
        byte x = ((byte) 'x');

        return (isNumeric(a) || (a == b || a == c || a == i || a == x || a == empty));
    }
}
