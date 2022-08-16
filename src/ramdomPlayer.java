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
    final static String GAME_ID = "b010351f-ef21-4153-b717-656a3ed6e6d2";

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

    public static boolean checkPlaceBomb (List<Position> objList, List<Position> surround){
        boolean check = false;

            for (int i=0;i<surround.size();i++){
                for (int j=0; j<objList.size();j++){
                    if (surround.get(i).getCol() == objList.get(j).getCol() && surround.get(i).getRow() == objList.get(j).getRow()) {
                        check = true;
                    }
                }

            }
       return check;
    }

    public static boolean checkBomb (List<Position> objList, List<Position> surround){
        boolean check = false;

        for (int i=0;i<surround.size();i++){
            for (int j=0; j<objList.size();j++){
                if (surround.get(i).getCol() == objList.get(j).getCol() && surround.get(i).getRow() == objList.get(j).getRow()) {
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
                List bombPos = mapInfo.getBombs();

                List<Position> restrictPosition = new ArrayList<>();
                restrictPosition.addAll(mapInfo.teleportGate);
//                restrictPosition.addAll(mapInfo.balk);
                restrictPosition.addAll(mapInfo.walls);
                restrictPosition.addAll(mapInfo.getBombList());
                String path = "";

                //code lay vi tri xung quanh player 4 huong
                List<Position> surround = new ArrayList<>();
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(1,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(2,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(3,1)) ;
                surround.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(4,1)) ;

                //Lay vi tri xung quanh 4 huong 10 o
            List<Position> surroundBomb = new ArrayList<>();
            for (int i = 0; i<10;i++) {
                surroundBomb.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(1, i )) ;
                surroundBomb.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(2, i)) ;
                surroundBomb.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(3, i)) ;
                surroundBomb.add(mapInfo.getCurrentPosition(randomPlayer).nextPosition(4, i)) ;
            }

            //ham check bom thi ne
            if(checkBomb(bombPos, surroundBomb)) {
                System.out.println(checkBomb(bombPos, surroundBomb));
                Position target2 = mapInfo.getBlank().get(0);
                String path2 = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictPosition, mapInfo.getCurrentPosition(randomPlayer), target2);
                randomPlayer.move(path2);
            }


            //ve sau lay target den tk gan nhat
            Position target = mapInfo.getBalk().get(0);
            System.out.println(target);

                if (target != null) {
                    path = AStarSearch.aStarSearch(mapInfo.mapMatrix, restrictPosition, mapInfo.getCurrentPosition(randomPlayer), target);

                    if(checkPlaceBomb(balkPos, surround)) {
                        randomPlayer.move("b");
                    } else
                        randomPlayer.move(path);
                }

        };


        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(SERVER_URL);
    }
}
