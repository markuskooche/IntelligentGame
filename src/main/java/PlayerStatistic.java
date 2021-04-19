public class PlayerStatistic {

    private final String[] players;

    public PlayerStatistic(Player[] players, int amount) {
        int counter = 0;
        String[] playerString = new String[amount * 4];

        for (Player player : players) {
            playerString[counter * 4] = "Spieler: " + (counter + 1);
            playerString[counter * 4 + 1] = "- Überschreibsteine: " + player.getOverrideStone();
            playerString[counter * 4 + 2] = "- Bomben: " + player.getBomb();
            playerString[counter * 4 + 3] = " ";
            counter++;
        }

        this.players = playerString;
    }

    public String[] getPlayer() {
        return players;
    }
}
