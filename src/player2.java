import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.Random;

public class player2 {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player2-xxx";
    final static String GAME_ID = "b010351f-ef21-4153-b717-656a3ed6e6d2";

    public static String getRandomPath(int length) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; ++i) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }
    public static void main(String[] args) {
        Hero player2 = new Hero(PLAYER_ID, GAME_ID);


        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo mapInfo=gameInfo.map_info;
            mapInfo.updateMapInfo();
            int[][] map=mapInfo.mapMatrix;
            for (int i=0;i<map.length;i++)
            {
                for (int j=0;j<map[0].length;j++)
                    System.out.print(map[i][j]+" ");
                System.out.println();
            }
            player2.move(getRandomPath(10));

//            player2.move("b");
        };

        player2.setOnTickTackListener(onTickTackListener);

        player2.connectToServer(SERVER_URL);
    }
}
