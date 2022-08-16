import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.Random;


public class ramdomPlayer {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player2-xxx";
    final static String GAME_ID = "e4931658-52d5-495f-835d-02cd70c22003";

    public static String getRandomPath(int length) {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Hero randomPlayer = new Hero(PLAYER_ID, GAME_ID);


        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            randomPlayer.move(getRandomPath(10));
        };

        randomPlayer.setOnTickTackListener(onTickTackListener);

        randomPlayer.connectToServer(SERVER_URL);
    }

    public static boolean canPlaceBombHorizontal(Position current, int[][] mapInfo) {
        for (int i = current.getCol() + 1; i < mapInfo.length; i++) {
            if (mapInfo[i][current.getRow()] == 0) {
                if (mapInfo[i][current.getRow() - 1] == 0 || mapInfo[i][current.getRow() + 1] == 0) {
                    return true;
                } else {
                    break;
                }
            }
        }
        for (int i = current.getCol() - 1; i >= 0; i--) {
            if (mapInfo[i][current.getRow()] == 0) {
                if (mapInfo[i][current.getRow() - 1] == 0 || mapInfo[i][current.getRow() + 1] == 0) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    public static boolean canPlaceBombVertical(Position current, int[][] mapInfo){
        for (int i = current.getRow() +1; i < mapInfo[0].length; i++) {
            if(mapInfo[current.getCol()][i]==0){
                if(mapInfo[current.getCol()-1][i]==0 || mapInfo[current.getCol()+1][i]==0){
                    return true;
                }
            }
            else{
                break;
            }
        }
        for (int i = current.getRow()-1; i > 0; i--) {
            if(mapInfo[current.getCol()][i]==0){
                if(mapInfo[current.getCol()-1][i]==0 || mapInfo[current.getCol()+1][i]==0){
                    return true;
                }
            }
            else{
                break;
            }
        }
        return false;
    }
}
