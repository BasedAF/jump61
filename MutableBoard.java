package jump61;

import java.util.ArrayDeque;

import static jump61.Side.*;
import static jump61.Square.square;

/** A Jump61 board state that may be modified.
 *  @author Alex Freeman
 */
class MutableBoard extends Board {

    /** An N x N board in initial configuration. */
    MutableBoard(int N) {
        _past = new ArrayDeque<MutableBoard>();
        _squares = new Square[N * N];
        _squaresByColor = new int[3];
        _fullByColor = new int[3];
        _piecesByColor = new int[3];
        for (int i = 0; i < _squares.length; i++) {
            _squares[i] = Square.INITIAL;
        }
        startCount();
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear. */
    MutableBoard(Board board0) {
        this(board0.size());
        this.copy(board0);
    }

    /** Sets the counts to their initial settings. */
    private void startCount() {
        _piecesByColor[0] = size() * size();
        _piecesByColor[1] = 0;
        _piecesByColor[2] = 0;

        _squaresByColor[0] = numPieces();
        _squaresByColor[1] = 0;
        _squaresByColor[2] = 0;

        _fullByColor[0] = 0;
        _fullByColor[1] = 0;
        _fullByColor[2] = 0;

    }

    @Override
    void clear(int N) {
        _past.clear();
        _squares = new Square[N * N];
        for (int i = 0; i < _squares.length; i++) {
            _squares[i] = Square.INITIAL;
        }
        startCount();
        announce();
    }

    @Override
    void copy(Board board) {
        int N = board.size();
        for (int i = 0; i < N * N; i++) {
            internalSet(i, board.get(i));
        }
    }


    /** Copy the contents of BOARD into me, without modifying my undo
     *  history.  Assumes BOARD and I have the same size. */
    private void internalCopy(MutableBoard board) {
        int N = board.size();
        Side[] colors = {WHITE, RED, BLUE};
        for (Side color : colors) {
            _squaresByColor[color.ordinal()] = board.numOfSide(color);
            _fullByColor[color.ordinal()] = board.fullSquares(color);
        }
        for (int i = 0; i < N * N; i++) {
            _squares[i] = board.get(i);
        }
        int[] newPiecesByColor = {
            board.piecesOfColor(WHITE),
            board.piecesOfColor(RED),
            board.piecesOfColor(BLUE)
        };
        _piecesByColor = newPiecesByColor;
    }

    @Override
    int size() {
        return (int) Math.sqrt(_squares.length);
    }

    @Override
    Square get(int n) {
        return _squares[n];
    }

    @Override
    int numOfSide(Side side) {
        return _squaresByColor[side.ordinal()];
    }

    /** Returns the number of full squares of side SIDE. */
    int fullSquares(Side side) {
        return _fullByColor[side.ordinal()];
    }

    @Override
    int numPieces() {
        return piecesOfColor(WHITE)
            + piecesOfColor(RED)
            + piecesOfColor(BLUE);
    }

    @Override
    int piecesOfColor(Side side) {
        return _piecesByColor[side.ordinal()];
    }

    /** Updates the _squaresByColor values to reflect a change
     *  from FROM to TO. */
    private void turnASquare(Side from, Side to) {
        if (from != to) {
            _squaresByColor[from.ordinal()] -= 1;
            _squaresByColor[to.ordinal()] += 1;
        }
    }

    @Override
    void addSpot(Side player, int r, int c) {
        markUndo();
        _piecesByColor[player.ordinal()] += 1;
        Square sq = get(r, c);
        Side old = sq.getSide();
        int spots = sq.getSpots();
        _piecesByColor[old.ordinal()] -= spots;
        _piecesByColor[player.ordinal()] += spots;
        turnASquare(old, player);
        _squares[sqNum(r, c)] = square(player, spots + 1);
        if (!isLegal(player.opposite())) {
            return;
        }
        fixSquare(r, c);
        announce();
    }

    @Override
    void addSpot(Side player, int n) {
        if (!exists(n)) {
            return;
        }
        Square sq = get(n);
        Side old = sq.getSide();
        int spots = sq.getSpots();
        if (old != player) {
            _piecesByColor[old.ordinal()] -= spots;
            _piecesByColor[player.ordinal()] += spots;
        }
        turnASquare(old, player);
        _squares[n] = square(player, spots + 1);
        if (!isLegal(player.opposite())) {
            return;
        }
        fixSquare(row(n), col(n));
    }

    /** Adjusts square (R, C) based on the number of spots in it. */
    private void fixSquare(int r, int c) {
        int neighbors = neighbors(r, c);
        Square sq = get(r, c);
        int spots = sq.getSpots();
        Side player = sq.getSide();
        if (spots == neighbors) {
            _fullByColor[player.ordinal()] += 1;
        }
        if (spots > neighbors) {
            _fullByColor[player.ordinal()] -= 1;
            _squares[sqNum(r, c)] = square(player, spots - neighbors);
            explode(player, r, c);
        }
    }

    /** Sends out spot of color PLAYER from square (R, C)
     *  to each of its neighbors. */
    private void explode(Side player, int r, int c) {
        addSpot(player, sqNum(r + 1, c));
        addSpot(player, sqNum(r - 1, c));
        addSpot(player, sqNum(r, c - 1));
        addSpot(player, sqNum(r, c + 1));
    }

    @Override
    void set(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), square(player, num));
    }

    @Override
    void set(int n, int num, Side player) {
        internalSet(n, square(player, num));
        announce();
    }

    @Override
    void undo() {
        this.internalCopy(_past.pop());
    }

    /** Record the beginning of a move in the undo history. */
    private void markUndo() {
        _past.push(new MutableBoard(this));
    }

    /** Set the contents of the square with index IND to SQ. Update counts
     *  of numbers of squares of each color.  */
    private void internalSet(int ind, Square sq) {
        _past.clear();
        Square old = _squares[ind];
        _piecesByColor[old.getSide().ordinal()] -= old.getSpots();
        _piecesByColor[sq.getSide().ordinal()] += sq.getSpots();
        turnASquare(old.getSide(), sq.getSide());
        _squares[ind] = sq;
        fixSquare(row(ind), col(ind));
    }

    /** Notify all Observers of a change. */
    private void announce() {
        setChanged();
        notifyObservers();
    }

    /** Array containing all my squares. */
    private Square[] _squares;

    /** A stack, containing all past versions of this board. */
    private ArrayDeque<MutableBoard> _past;

    /** The number of squares on this board of each color: overall and full. */
    private int[] _squaresByColor, _fullByColor, _piecesByColor;
}
