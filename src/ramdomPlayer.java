import jsclub.codefest.sdk.algorithm.*;
import jsclub.codefest.sdk.model.Hero;
import io.socket.emitter.Emitter;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class RandomPlayer {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player1-xxx";
    final static String GAME_ID = "1e86f66d-ad89-484a-a391-87e58100e313";

    public static String getRandomPath(int length) {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }

    public static boolean move = true;
    public static int count = 0;
    public static boolean delay = false;
    public static int checkDelay = 0;

    //col la x
    //row la y
    public static void main(String[] args) {
        Hero randomPlayer = new Hero(PLAYER_ID, GAME_ID);

        Emitter.Listener onTickTackListener = objects -> {

            GameInfo game = GameUtil.getGameInfo(objects);
            MapInfo map = game.map_info;
            map.updateMapInfo();
            int[][] mapMatrix = map.mapMatrix;
            String path = "";
            List<Position> restrictPosition = new ArrayList<>();
            restrictPosition.addAll(map.getWalls());
            restrictPosition.addAll(map.getTeleportGate());
            restrictPosition.addAll(map.getBalk());
            Position placeBomb = null;
            //move để delay vì emitter gọi liên tục dẫn đến khi chưa di chuyển đã gọi đến hàm di chuyển lần nx
            if(move){
                move = !move;
                if(map.getSpoils().size() == 0){
                    placeBomb = findWallToBreak(map.getCurrentPosition(randomPlayer), mapMatrix); // tim cho dat bom

                    switch (count % 3){
                        // case 0 di chuyen den cho dat bom
                        case 0:
                            // them điều kiện gần tường thì ms đặt bom
                            if(map.getCurrentPosition(randomPlayer).getCol() == placeBomb.getCol() && map.getCurrentPosition(randomPlayer).getRow() == placeBomb.getRow()){
                                count++;
                            }
                            randomPlayer.move(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), placeBomb));
                            break;
                        // case 1 de dat bom
                        case 1:
                            randomPlayer.move("b");
                            count++;
                            break;

                        // case 2 né bom
                        case 2:
                            delay = !delay;
                            randomPlayer.move(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), canPlaceBomb(placeBomb, mapMatrix)));
                            System.out.println(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), canPlaceBomb(placeBomb, mapMatrix)));
                            count++;
                    }

//                    System.out.println("Current place: " + map.getCurrentPosition(randomPlayer).getCol() + " " + map.getCurrentPosition(randomPlayer).getRow());
//                    System.out.println("Bomb place: " + placeBomb.getCol() + " " + placeBomb.getRow());
//                    System.out.println("Escape place: " + canPlaceBombHorizontal(placeBomb, mapMatrix).getCol() + " " + canPlaceBombHorizontal(placeBomb, mapMatrix).getRow());
                }
                else{
                    randomPlayer.move(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), map.getSpoils().get(0)));
                }
            }
            // delay chống lặp di chuyển
            else{
                //delay đợi bom gần nổ rồi chạy
                //nếu chạy sớm quá sẽ gọi đến hàm tìm vị trí đặt bom => ăn bom
                if(delay){
                    if(checkDelay == 7){
                        delay = !delay;
                        checkDelay = 0;
                    }
                    else{
                        checkDelay ++;
                    }

                }
                else{
                    move = !move;
                }

            }
        };

        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(SERVER_URL);
    }

    public static ArrayList<Position> getSpoilsPosition(MapInfo map){
        ArrayList<Position> spoilList = new ArrayList<>();
        for(Spoil s : map.getSpoils()){
            spoilList.add(new Position(s.getCol(),s.getRow()));
        }
        return spoilList;
    }
    //ham tra ve vi tri ne sau khi dat bom
    public static Position canPlaceBomb(Position current, int[][] mapInfo) {
        for (int i = current.getCol() + 1; i < mapInfo[0].length; i++) {
            if (mapInfo[current.getRow()][i] == 0) {
                if (mapInfo[current.getRow() - 1][i] == 0 ) {
                    return new Position(i, current.getRow() - 1);
                } else if (mapInfo[current.getRow() + 1][i] == 0) {
                    return new Position(i, current.getRow() + 1);
                }
            }
            else {
                break;
            }
        }
        for (int i = current.getCol() - 1; i > 0; i--) {
            if (mapInfo[current.getRow()][i] == 0) {
                if (mapInfo[current.getRow() - 1][i] == 0) {
                    return new Position(i, current.getRow() - 1);
                }
                else if (mapInfo[current.getRow() + 1][i] == 0) {
                    return new Position(i, current.getRow() + 1);
                }
            }
            else {
                break;
            }
        }
        for (int i = current.getRow() + 1; i < mapInfo.length; i++) {
            if(mapInfo[i][current.getCol()]==0){
                if(mapInfo[i][current.getCol() - 1]==0){
                    return new Position(current.getCol() - 1, i);
                } else if (mapInfo[i][current.getCol() + 1]==0) {
                    return new Position(current.getCol() + 1, i);
                }
            }
            else{
                break;
            }
        }
        for (int i = current.getRow() - 1; i > 0; i--) {
            if(mapInfo[i][current.getCol()]==0){
                if(mapInfo[i][current.getCol() - 1]==0){
                    return new Position(current.getCol() - 1, i);
                }
                else if (mapInfo[i][current.getCol() + 1]==0) {
                    return new Position(current.getCol() + 1, i);
                }
            }
            else{
                break;
            }
        }
        return null;
    }

    //tim tường để phá theo hàng dọc hoặc hàng ngang để di chuyển
    public static Position findWallToBreak(Position current, int[][] map){
        for (int i = current.getCol(); i < map[0].length; i++) {
            if(map[current.getRow()][i] == 0 && isNearByWalls(new Position(i, current.getRow()), map) == true && canPlaceBomb(new Position(i, current.getRow()), map) != null){
                return new Position(i, current.getRow());
            } else if (map[current.getRow()][i] == 1) {
                break;
            }
        }
        for (int i = current.getCol() - 1; i > 0; i--) {
            if(map[current.getRow()][i] == 0 && isNearByWalls(new Position(i, current.getRow()), map) == true && canPlaceBomb(new Position(i, current.getRow()), map) != null){
                return new Position(i, current.getRow());
            } else if (map[current.getRow()][i] == 1) {
                break;
            }
        }
        for (int i = current.getRow() ; i < map.length; i++) {
            if(map[i][current.getCol()] == 0 && isNearByWalls(new Position(current.getCol(), i), map) == true && canPlaceBomb(new Position(current.getCol(), i), map) != null){
                return new Position(current.getCol(), i);
            } else if (map[i][current.getCol()] == 1) {
                break;
            }
        }
        for (int i = current.getRow() - 1; i > 0; i--) {
            if(map[i][current.getCol()] == 0 && isNearByWalls(new Position(current.getCol(), i), map) == true && canPlaceBomb(new Position(current.getCol(), i), map) != null){
                return new Position(current.getCol(), i);
            } else if (map[i][current.getCol()] == 1) {
                break;
            }
        }
        return null;
    }

    // ktra xem vị trí truyền vào có ở cạnh tường không
    public static boolean isNearByWalls(Position check, int[][] map){
        int x = check.getCol();
        int y = check.getRow();
        if(map[y + 1][x] == 2 || map[y - 1][x] == 2 || map[y][x + 1] == 2  || map[y][x - 1] == 2 ){
            return true;
        }
        return false;
    }


}














