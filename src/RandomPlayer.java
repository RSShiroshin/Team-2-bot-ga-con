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
    final static String GAME_ID = "609d6363-5328-47f9-b84d-e3dcf1cc423e";



    public static boolean move = true;
    public static int count = 0;
    public static int countForGetSpoil = 0;
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
            List<Position> restrictPosition = new ArrayList<>();        // restriction co wall
            List<Position> restrictPositionSpoil = new ArrayList<>();   // restriction khong co wall

            // add walls khong pha dc
            restrictPosition.addAll(map.getWalls());
            restrictPositionSpoil.addAll(map.getWalls());

            // add cong tele
            restrictPosition.addAll(map.getTeleportGate());
            restrictPositionSpoil.addAll(map.getTeleportGate());

            // add bomb
            // restrictPosition.addAll(map.getBombList());
            restrictPositionSpoil.addAll(map.getBombList());

            // add vi tri cua doi thu
            restrictPosition.add(map.getEnemyPosition(randomPlayer));
            restrictPositionSpoil.add(map.getEnemyPosition(randomPlayer));

            // add virus
            restrictPosition.addAll(getVirusPosition(map.getVirus()));
            restrictPositionSpoil.addAll(getVirusPosition(map.getVirus()));

            // add dhuman
            restrictPosition.addAll(getDHumanPosition(map.getDhuman()));
            restrictPositionSpoil.addAll(getDHumanPosition(map.getDhuman()));

            // add tuong cho restrictionPosition
            restrictPosition.addAll(map.getBalk());

            Position placeBomb = null;
            System.out.println("Spoil: " + map.getSpoils().get(0).spoil_type);
            //move để delay vì emitter gọi liên tục dẫn đến khi chưa di chuyển đã gọi đến hàm di chuyển lần nx
            if(move){
                move = !move;
                // khi khong co thuoc thi di pha tuong
                if(map.getSpoils().size() == 0) {
                    placeBomb = findWallToBreak(map.getCurrentPosition(randomPlayer), mapMatrix); // tim cho dat bom

                    switch (count % 3) {
                        // case 0 di chuyen den cho dat bom
                        case 0:
                            // them điều kiện gần tường thì ms đặt bom
                            //System.out.println("Tim tuong dat bom");
                            if (map.getCurrentPosition(randomPlayer).getCol() == placeBomb.getCol()
                                    && map.getCurrentPosition(randomPlayer).getRow() == placeBomb.getRow()
                                    && isNearByWalls(map.getCurrentPosition(randomPlayer), mapMatrix)) {
                                count++;
                            }
                            randomPlayer.move(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), findWallToBreak(map.getCurrentPosition(randomPlayer), mapMatrix)));
                            break;
                        // case 1 de dat bom
                        case 1:
                            //System.out.println("Dat bom khi khong co thuoc");
                            randomPlayer.move("b");
                            count++;
                            break;

                        // case 2 né bom
                        case 2:
                            //System.out.println("ne bom khi khogn co thuoc");
                            delay = !delay;
                            randomPlayer.move(AStarSearch.aStarSearch(mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer), canPlaceBomb(placeBomb, mapMatrix)));
                            count++;
                            break;
                    }

                }

                // neu co thuoc thi di an thuoc
                else{
                    switch (countForGetSpoil % 3){
                        case 0:
                            if(canPlaceBomb(map.getCurrentPosition(randomPlayer), mapMatrix) != null && isNearByWalls(map.getCurrentPosition(randomPlayer), mapMatrix) == true){
                                countForGetSpoil++;
                            }
                            randomPlayer.move(AStarSearch.aStarSearch(mapMatrix,
                                    restrictPositionSpoil,
                                    map.getCurrentPosition(randomPlayer),
                                    getNearestSpoil(map.getSpoils(), map.getCurrentPosition(randomPlayer))));
                            break;
                        case 1:
                            randomPlayer.move("b");
                            countForGetSpoil++;
                            break;
                        case 2:
                            delay = !delay;
                            randomPlayer.move(AStarSearch.aStarSearch(
                                    mapMatrix,
                                    restrictPosition,
                                    map.getCurrentPosition(randomPlayer),
                                    canPlaceBomb(map.getCurrentPosition(randomPlayer), mapMatrix))
                            );
                            countForGetSpoil++;
                            count = 0;
                            break;
                    }

                }
            }
            // delay chống lặp di chuyển
            else{
                //delay đợi bom gần nổ rồi chạy
                //nếu chạy sớm quá sẽ gọi đến hàm tìm vị trí đặt bom => ăn bom
                if(delay){
                    if(checkDelay == 8){
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

    // hoi thua nhg co le dung sau
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

    public static Position getNearestSpoil(List<Spoil> spoilList, Position current){
        int min = 100;
        Position nearest = null;
        for (Spoil s: spoilList) {
            if(min > myMahattanDistance(s, current)){
                min = myMahattanDistance(s, current);
                nearest = s;
            }
        }
        return nearest;
    }

    public static int myMahattanDistance(Spoil a, Position b){
        return Math.abs(a.getCol() - b.getCol()) + Math.abs(a.getRow() - b.getRow());
    }

    // convert virusList into position of viruslist
    public static List<Position> getVirusPosition(List<Viruses> virusList){
        List<Position> positionList = new ArrayList<>();
        for (Viruses v: virusList) {
            positionList.add(v.position);
        }
        return positionList;
    }

    // convert dhumanList into position of dhumanList
    public static List<Position> getDHumanPosition(List<Human> dhuman){
        List<Position> positionList = new ArrayList<>();
        for (Human d: dhuman) {
            positionList.add(d.position);
        }
        return positionList;
    }
}














