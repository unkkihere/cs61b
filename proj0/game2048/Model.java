package game2048;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    //===============需要实现===============
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        /*
            特别注意: 此游戏中的第 0 行是整个 board 底部的那行，从下往上依次是第 1,2,3,... 行
         */


        int size = board.size();
        int moveCnt = 0;

        board.setViewingPerspective(side);
        Board copyB = copyBoard(board, size, score);

        int[][] record = new int[size][size];   // record 数组用来记录各位置 tile 的状态，0 代表此 tile 位置没变化，值为除 0 外的数
                                                // 字就代表此 tile 会移动到哪个位置。如果 tile 移动发生合并时，则记值为原数值的负数


        for (int row = size-2; row >= 0; row--){
            for(int col = 0; col < size; col++){
                Tile t = board.tile(col, row);

                if (t != null){
                    for (int k = row+1; k < size ; k++){
                        // 检查该 tile 上方的 tile 是否有效
                        if (checkTileValid(copyB, col, k)){

                            // 当上方 tile 与此 tile 值不相等时
                            if (t.value() != copyB.tile(col, k).value()){

                                // 1. 看上方 tile 的 record 值为多少
                                //  1). 若上方 tile 的 record 值为零，表示上方 tile 位置无变化，则此 tile 需要移动到上方 tile 的下一行
                                if ( record[k][col] == 0 ){

                                    if (k - row == 1){
                                        record[row][col] = 0;
                                    }else{
                                        record[row][col] = k-1;
                                        board.move(col, k-1, t);
                                        moveCnt++;
                                    }
                                }

                                //  2). 若上方 tile 的 record 值不等于零，表示上方 tile 位置发生了变化，则此 tile 移动到上方 tile 的位置来填充
                                else {

                                    record[row][col] = k;
                                    board.move(col, k, t);
                                    moveCnt++;

                                }


                            }

                            // 当上方 tile 与此 tile 值相等时
                            if (t.value() == copyB.tile(col, k).value()){
                                // 1). 若上方 tile 的 record 值为零，则需要将此 tile 移动到上方 tile 的位置进行合并
                                if (record[k][col] == 0){
                                    record[row][col] = -1 * k;
                                    board.move(col, k, t);
                                    moveCnt++;
                                    score += board.tile(col, k).value();
                                }

                                // 2). 若上方 tile 的 record 值大于零，表示上方 tile 发生了位移但并未发生合并，则将此 tile 上移到上方 tile 移动后的位置
                                if (record[k][col] > 0){
                                    int place = record[k][col];
                                    record[row][col] = -1 * place;
                                    board.move(col, place, t);
                                    moveCnt++;
                                    score += board.tile(col, place).value();
                                }

                                //  3). 若上方 tile 的 record 值小于零，表示上方 tile 发生位移并且也合并过了，则此 tile 需要上移到上方 tile 移动后位置的下一行
                                if (record[k][col] < 0){
                                    int place = (-1 * record[k][col]) - 1;
                                    record[row][col] = place;
                                    board.move(col, place, t);
                                    moveCnt++;
                                }
                            }

                            break;
                        }

                        // 当最上方的 tile 为 null 时，将 t 上移
                        if (k == size-1 && copyB.tile(col, k) == null){
                            board.move(col, k, t);
                            record[row][col] = k ;
                            moveCnt++;
                        }
                    }

                }
            }
        }

        board.setViewingPerspective(Side.NORTH);


        // 如果 moveCnt != 0, 则改变 changed
        if (moveCnt != 0){
            changed = true;
        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** 复制 board   */
    private static Board copyBoard(Board b, int size, int score){
        int[][] boardArr = new int[size][size];
        String boardStr = b.toString().replaceAll(" +", "0").trim().replaceAll("\r\n", "").replace("[", "");

        String[] str2Arr = boardStr.split("\\|");

        int cntOfEmpty = 0;

        for (int i = 0; i < str2Arr.length; i++) {

            if (str2Arr[i].equals("")){
                cntOfEmpty++;
            }

            else if (Integer.parseInt(str2Arr[i]) != 0){
                int row = (i - cntOfEmpty) / size;
                int col = (i - cntOfEmpty) % size;

                boardArr[row][col] = Integer.parseInt(str2Arr[i]);
            }

        }
        return new Board(boardArr, score);
    }

    public static int nextMergeTile(Board b,int col, int row){
        for(int i = row-1; i >= 0; i--){
            if(b.tile(col,i) != null)
            {
                return i;
            }
        }
        return -1;

    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    //===============需要实现===============(已完成)
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.

        // size 获取当前 board 的行数/列数
        int size = b.size();

        // 当 board 中有 tile 为 null 时，说明此时 board 中有 emptySpace，返回 true
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (b.tile(j, i) == null){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    //===============需要实现===============(已完成)
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.

        int size = b.size();
        int maxVal = 0;
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){

                // 当 board 中存在 emptySpace 时，需要跳过此 tile
                if (b.tile(j, i) == null){
                    continue;
                }
                maxVal = Math.max(maxVal, b.tile(j, i).value());
            }
        }

        if (maxVal == MAX_PIECE){
            return true;
        }
        return false;


    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    //===============需要实现===============(已完成)
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        int size = b.size();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                if (b.tile(j, i) == null){
                    return true;
                }
                // 利用现成的代码
                /*else if (checkTileMoveExists(b, j, i)){
                    return true;
                }*/

                // 自己编写
                else{
                    Tile curTile = b.tile(j, i);

                    // 在检查 curTile 邻近的 tile 时，首先检查其邻近的 tile 是否为有效 tile
                    // 检查 curTile 下方的 tile 是否和其值相等
                    if (checkTileValid(b, j, i+1)){
                        if (curTile.value() == b.tile(j, i+1).value()){
                            return true;
                        }
                    }

                    // 检查 curTile 右方的 tile 是否和其值相等
                    if (checkTileValid(b, j+1, i)){
                        if (curTile.value() == b.tile(j+1, i).value()){
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    private static boolean checkTileMoveExists(Board b, int col, int row){
        Tile t = b.tile(col,row);
        if(checkTileValid(b,col+1,row)){
            if(t.value() == b.tile(col+1, row).value()){
                return true;
            }
        }
        if(checkTileValid(b,col-1,row)){
            if(t.value() == b.tile(col-1,row).value()){
                return true;
            }
        }
        if(checkTileValid(b,col,row+1)){
            if(t.value() == b.tile(col,row+1).value()){
                return true;
            }
        }
        if(checkTileValid(b,col,row-1)){
            if(t.value() == b.tile(col,row-1).value()){
                return true;
            }
        }
        return false;
    }

    private static boolean checkTileValid(Board b, int col, int row){
        if(col < 0 || col > b.size()-1 || row < 0 || row > b.size()-1){
            return false;
        }
        if(b.tile(col,row) == null){
            return false;
        }
        return true;
    }
    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
