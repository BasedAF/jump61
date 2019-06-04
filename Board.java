package jump61;

import java.util.Observable;

import static jump61.Side.*;

/** Represents the state of a Jump61 game.  Squares are indexed either by
 *  row and column (between 1 and size()), or by square number, numbering
 *  squares by rows, with squares in row 1 numbered 0 - size()-1, in
 *  row 2 numbered size() - 2*size() - 1, etc.
 *
 *  The class extends java.util.Observable in case one wants a class (such
 *  as a GUI component) to be notified when the board changes.
 *  @author Alex Freeman
 */
abstract class Board extends Observable {

    /** (Re)initialize me to a cleared board with N squares on a side. Clears
     *  the undo history and sets the number of moves to 0. */
    void clear(int N) {
        unsupported("clear");
    }

    /** Copy the contents of BOARD into me. */
    void copy(Board board) {
        unsupported("copy");
    }

    /** Return the number of rows and of columns of THIS. */
    abstract int size();

    /** Return true if square N is a corner. */
    boolean isCorner(int n) {
        return neighbors(n) == 2;
    }

    /** Return true if square N is on an edge. */
    boolean isEdge(int n) {
        return neighbors(n) == 2;
    }

    /** Return true if square N is full. */
    boolean isFullAt(int n) {
        return get(n).getSpots() == neighbors(n);
    }

    /** Returns the contents of the square at row R, column C
     *  1 <= R, C <= size (). */
    Square get(int r, int c) {
        return get(sqNum(r, c));
    }

    /** Returns the contents of square #N, numbering squares by rows, with
     *  squares in row 1 number 0 - size()-1, in row 2 numbered
     *  size() - 2*size() - 1, etc. */
    abstract Square get(int n);

    /** Returns the total number of spots on the board. */
    abstract int numPieces();

    /** Returns the number of full squares of color SIDE. */
    abstract int fullSquares(Side side);

    /** Returns the Side of the player who would be next to move.  If the
     *  game is won, this will return the loser (assuming legal position). */
    Side whoseMove() {
        return ((numPieces() + size()) & 1) == 0 ? RED : BLUE;
    }

    /** Return true iff row R and column C denotes a valid square. */
    final boolean exists(int r, int c) {
        return 1 <= r && r <= size() && 1 <= c && c <= size();
    }

    /** Return true iff S is a valid square number. */
    final boolean exists(int s) {
        int N = size();
        return 0 <= s && s < N * N;
    }

    /** Return the row number for square #N. */
    final int row(int n) {
        return n / size() + 1;
    }

    /** Return the column number for square #N. */
    final int col(int n) {
        return n % size() + 1;
    }

    /** Return the square number of row R, column C. */
    final int sqNum(int r, int c) {
        if (!exists(r, c)) {
            return -1;
        }
        return (c - 1) + (r - 1) * size();
    }


    /** Returns true iff it would currently be legal for PLAYER to add a spot
        to square at row R, column C. */
    boolean isLegal(Side player, int r, int c) {
        return isLegal(player, sqNum(r, c));
    }

    /** Returns true iff it would currently be legal for PLAYER to add a spot
     *  to square #N. */
    boolean isLegal(Side player, int n) {
        return n >= 0 && player.playableSquare(get(n).getSide());
    }

    /** Returns true iff PLAYER is allowed to move at this point. */
    boolean isLegal(Side player) {
        int N = size();
        return numOfSide(player.opposite()) != (N * N);
    }

    /** Returns the winner of the current position, if the game is over,
     *  and otherwise null. */
    final Side getWinner() {
        if (!isLegal(RED)) {
            return BLUE;
        }
        if (!isLegal(BLUE)) {
            return RED;
        }
        return null;
    }

    /** Return the number of squares of given COLOR. */
    abstract int numOfSide(Side color);

    /** Return the number of pieces of color SIDE. */
    abstract int piecesOfColor(Side side);

    /** Return the average number of pieces per square of color SIDE. */
    int density(Side side) {
        return (piecesOfColor(side) + 1) / (numOfSide(side) + 1);
    }

    /** Returns the number of squares player P will lose from a loss at N. */
    private int vulnerability(Side p, int n) {
        if (!isFullAt(n)) {
            return get(n).getSide() == p ? 1 : 0;
        }
        int r, c, friends, foes;
        friends = foes = 0;
        r = row(n);
        c = col(n);
        int[] neighbors = {
            sqNum(r - 1, c), sqNum(r + 1, c),
            sqNum(r, c - 1), sqNum(r, c + 1)
        };
        for (int neighbor : neighbors) {
            if (!exists(neighbor)) {
                continue;
            }
            if (get(neighbor).getSide() == p.opposite()) {
                foes += 1;
                if (isFullAt(neighbor)) {
                    friends += neighbors(neighbor);
                }
            } else {
                friends += 1;
                if (isFullAt(neighbor)) {
                    friends += (density(p) + 1) * neighbors(neighbor);
                }
            }
        }
        if (friends == 0 || foes == 0) {
            return foes;
        }
        return friends;
    }

    /** Returns player P's total vulnerability. */
    int vulnerability(Side p) {
        int result = 0;
        for (int i = 0; i < size() * size(); i++) {
            result += vulnerability(p, i);
        }
        return result;
    }

    /** Add a spot from PLAYER at row R, column C.  Assumes
     *  isLegal(PLAYER, R, C). */
    void addSpot(Side player, int r, int c) {
        unsupported("addSpot");
    }

    /** Add a spot from PLAYER at square #N.  Assumes isLegal(PLAYER, N). */
    void addSpot(Side player, int n) {
        unsupported("addSpot");
    }

    /** Set the square at row R, column C to NUM spots (0 <= NUM), and give
     *  it color PLAYER if NUM > 0 (otherwise, white).  Clear the undo
     *  history. */
    void set(int r, int c, int num, Side player) {
        unsupported("set");
    }

    /** Set the square #N to NUM spots (0 <= NUM), and give it color PLAYER
     *  if NUM > 0 (otherwise, white).  Clear the undo history. */
    void set(int n, int num, Side player) {
        unsupported("set");
    }

    /** Undo the effects of one move (that is, one addSpot command).  One
     *  can only undo back to the last point at which the undo history
     *  was cleared, or the construction of this Board. */
    void undo() {
        unsupported("undo");
    }

    /** Returns my dumped representation. */
    @Override
    public String toString() {
        String result = "";
        result += "===\n";
        int N = size();
        for (int r = 1; r <= N; r++) {
            result += rowToString(r);
            result += "\n";
        }
        result += "===";
        return result;
    }

    /** Returns a string representation of row R. */
    private String rowToString(int r) {
        String result = "    ";
        int N = size();
        for (int c = 1; c <= N; c++) {
            result += get(r, c).toString();
            if (c < N) {
                result += " ";
            }
        }
        return result;
    }

    /** Returns an external rendition of me, suitable for
     *  human-readable textual display.  This is distinct from the dumped
     *  representation (returned by toString). */
    public String toDisplayString() {
        StringBuilder out = new StringBuilder(toString());
        out.delete(0, 3 + NL_LENGTH);
        out.delete(out.length() - 3, out.length());
        return out.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toDisplayString().hashCode();
    }

    /** Returns the number of neighbors of the square at row R, column C. */
    int neighbors(int r, int c) {
        int size = size();
        int n;
        n = 0;
        if (r > 1) {
            n += 1;
        }
        if (c > 1) {
            n += 1;
        }
        if (r < size) {
            n += 1;
        }
        if (c < size) {
            n += 1;
        }
        return n;
    }

    /** Returns the number of neighbors of square #N. */
    int neighbors(int n) {
        return neighbors(row(n), col(n));
    }

    /** Indicate fatal error: OP is unsupported operation. */
    private void unsupported(String op) {
        String msg = String.format("'%s' operation not supported", op);
        throw new UnsupportedOperationException(msg);
    }

    /** The length of an end of line on this system. */
    private static final int NL_LENGTH =
        System.getProperty("line.separator").length();
}
