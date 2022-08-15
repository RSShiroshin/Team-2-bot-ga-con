import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.util.GameUtil;

import javax.xml.xpath.XPathEvaluationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.String.valueOf;


public class ramdomPlayer {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player1-xxx";
    final static String GAME_ID = "ce05572b-2e50-46a5-9d44-2fae35aeb0e0";

    public static String getRandomPath(int length) {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }

//    public static List<Position> bombPos(){
//        GameInfo gameInfo = GameUtil.getGameInfo(objects);
//        MapInfo mapinfo = gameInfo.getMapInfo();
//        List bombPos =  mapinfo.getBombList();
//        return bombPos;
//    }

//    public static List<Position> wallPos(){
//        Emitter.Listener onTickTackListener = objects -> {
//            GameInfo gameInfo = GameUtil.getGameInfo(objects);
//            MapInfo mapinfo = gameInfo.getMapInfo();
//            List wallPos = mapinfo.getWalls();
//            return wallPos;
//        };
//    }

    public static boolean checkPlaceBomb (List<Position> obj, List<Position> surround){
        boolean check = false;

            for (int i=0;i<surround.size();i++){
                for (int j=0; j<obj.size();j++){
                    if (surround.get(i).getCol() == obj.get(j).getCol() && surround.get(i).getRow() == obj.get(j).getRow()) {
                        check = true;
                    }
                }

            }
       return check;
    }



    public static void main(String[] args) {
        Hero randomPlayer = new Hero(PLAYER_ID, GAME_ID);

        Emitter.Listener onTickTackListener = objects -> {
                GameInfo gameInfo = GameUtil.getGameInfo(objects);
                MapInfo mapInfo=gameInfo.map_info;
                mapInfo.updateMapInfo();

                List balkPos = mapInfo.getBalk();

                List<Position> restrictPosition = new ArrayList<>();
                restrictPosition.addAll(mapInfo.teleportGate);
//                restrictPosition.addAll(mapInfo.balk);
                restrictPosition.addAll(mapInfo.walls);
                restrictPosition.addAll(mapInfo.getBombList());
                String path = "";

                List<Position> surround = new ArrayList<>();
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(1,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(2,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(3,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(4,1)) ;

            Position target = mapInfo.getBalk().get(0);
            System.out.println(target);
                if (target != null) {
                    path = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictPosition, mapInfo.getCurrentPosition(randomPlayer), target);

                    if(checkPlaceBomb(balkPos, surround)) {
                        randomPlayer.move("b");
                        randomPlayer.move("1");
                        randomPlayer.move("1");
                        randomPlayer.move("4");
                        randomPlayer.move("4");
                    } else
                        randomPlayer.move(path);
                }

        };


        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(SERVER_URL);
    }
}
