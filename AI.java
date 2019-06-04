package jump61;

import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Point;

/** An automated Player.
 *  @author Alex Freeman
 */
class AI extends Player {

    /** These are used to determine whether a move is worth testing. */
    private static enum Criterion {
        /** All the criteria I can look at. */
        FULL, CORNER, EDGE, ONE, TWO, THREE;

        /** Return the result of this criterion's evaluation function of
         *  square N of board B for player P. */
        boolean eval(Board b, int n) {
            switch (this) {
            case FULL:
                return b.isFullAt(n);
            case CORNER:
                return b.isCorner(n);
            case EDGE:
                return b.isEdge(n);
            case ONE:
                return b.get(n).getSpots() == 1;
            case TWO:
                return !b.isCorner(n)
                    && b.get(n).getSpots() == 2;
            case THREE:
                return b.get(n).getSpots() == 3;
            default:
                return false;
            }
        }
    }

    /** The criteria, in the order I'm planning to use them. */
    private final Criterion[] _criteria = {
        Criterion.FULL, Criterion.THREE, Criterion.EDGE,
        Criterion.TWO, Criterion.ONE
    };

    /** Time allotted to all but final search depth (milliseconds). */
    private static final long TIME_LIMIT = 15000;

    /** Number of calls to minmax between checks of elapsed time. */
    private static final long TIME_CHECK_INTERVAL = 10000;

    /** Number of milliseconds in one second. */
    private static final double MILLIS = 1000.0;

    /** Start time that gets reset each time a move is made. */
    private long _start;

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically.
     */
    AI(Game game, Side color) {
        super(game, color);
        _memory = new HashMap<Board, Point>();
    }

    /** Starts the timer. */
    private void start() {
        _start = System.currentTimeMillis();
    }

    /** Sets my difficulty setting to LEVEL. */
    void setLevel(int level) {
        getGame().message("Loading level %d...%n", level);
        if (level > _level) {
            HashMap<Board, Point> old = new HashMap<Board, Point>(_memory);
            start();
            for (Board b : old.keySet()) {
                if (b.size() == getBoard().size() && isNonTerminal(b)) {
                    memoizeBoard(b, getSide(), level(),
                                 minmax(getSide(), b, level(), I_WIN, null));
                }
                if (outOfTime()) {
                    break;
                }
            }
        }
        _level = level;
    }

    /** Removes all board mappings from my memo table. */
    void wipeMemory() {
        _memory.clear();
    }

    /** Prints all memoized boards of size N. */
    void showMemory(int n) {
        System.out.printf("Memory for player %s:%n", getSide());
        for (Board b : _memory.keySet()) {
            if (b.size() == n) {
                System.out.print(b.toDisplayString());
                System.out.print("Value: ");
                System.out.println(scoreFromMemo(b));
                System.out.print("Highest level: ");
                System.out.println(highestLevel(b));
            }
        }
    }

    /** Returns the number of moves I look ahead when I make a move. */
    int level() {
        return _level;
    }

    /** Returns the highest level at which BOARD exists in the memo table. */
    private int highestLevel(Board board) {
        if (_memory.containsKey(board)) {
            return (int) _memory.get(board).getX();
        }
        return -1;
    }

    @Override
    void makeMove() {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        Board b = getBoard();
        Game g = getGame();
        int movePos;
        if (b.numOfSide(Side.WHITE) == b.size() * b.size()) {
            movePos = 0;
        } else {
            start();
            memoizeBoard(b, getSide(), level(),
                         minmax(getSide(), b, level(), I_WIN, moves));
            if (moves.size() == 0) {
                movePos = randomLegalMove();
            } else {
                movePos = moves.get(g.randInt(moves.size()));
            }
        }
        int[] move = {b.row(movePos), b.col(movePos)};
        g.makeMove(move[0], move[1]);
        g.reportMove(getSide(), move[0], move[1]);
    }

    /** If I end up with no moves in my list, just return something. */
    private int randomLegalMove() {
        int N = getBoard().size();
        ArrayList<Integer> options = new ArrayList<Integer>();
        for (int i = 0; i < N * N; i++) {
            if (getBoard().isLegal(getSide(), i)) {
                options.add(i);
            }
        }
        if (options.size() > 0) {
            return options.get(getGame().randInt(options.size()));
        }
        return -1;
    }

    /** Returns true if I don't remember a win at BOARD. */
    private boolean isNonTerminal(Board board) {
        return _memory.containsKey(board)
            && scoreFromMemo(board) != I_WIN
            && scoreFromMemo(board) != I_LOSE;
    }

    /** Puts B into the memo table with VAL as its value to SIDE
     *  if LEVEL is higher than its highest level. */
    private void memoizeBoard(Board b, Side side, int level, int val) {
        if (!_memory.containsKey(b)
            || (level > highestLevel(b) && isNonTerminal(b))) {
            if (side == getSide()) {
                _memory.put(new ConstantBoard(new MutableBoard(b)),
                            new Point(level, val));
            } else {
                _memory.put(new ConstantBoard(new MutableBoard(b)),
                            new Point(level, -val));
            }
        }
    }

    /** Saves B as either a win or a loss, depending on DIDIWIN. */
    void saveTerminal(Board b, boolean didIWin) {
        int val = didIWin ? I_WIN : I_LOSE;
        _memory.put(new ConstantBoard(new MutableBoard(b)), new Point(0, val));
    }

    /** Returns the memoized score of B. */
    private int scoreFromMemo(Board b) {
        return (int) _memory.get(b).getY();
    }

    /** Returns true if I have exceeded 15 seconds in decision making. */
    private boolean outOfTime() {
        return System.currentTimeMillis() - _start
            > TIME_LIMIT - 2 * MILLIS;
    }

    /** Return the minimum of CUTOFF and the minmax value of board B
     *  (which must be mutable) for player P to a search depth of D
     *  (where D == 0 denotes statically evaluating just the next move).
     *  If MOVES is not null and CUTOFF is not exceeded, set MOVES to
     *  a list of all highest-scoring moves for P; clear it if
     *  non-null and CUTOFF is exceeded. the contents of B are
     *  invariant over this call. */
    int minmax(Side p, Board b, int d, int cutoff,
               ArrayList<Integer> moves) {
        MutableBoard testBoard = new MutableBoard(b);
        int N, bestVal, val, i;
        N = testBoard.size();
        bestVal = I_LOSE;
        val = bestVal - 1;

        ArrayList<Integer> legal = new ArrayList<Integer>();
        for (i = 0; i < N * N; i++) {
            if (testBoard.isLegal(p, i)) {
                legal.add(i);
            }
        }
        i = 0;
        for (Criterion criterion : _criteria) {
            for (int n : legal) {
                if (criterion.eval(testBoard, n)) {
                    val = bustAMove(p, testBoard, d, n,
                                    bestVal, cutoff);
                    if (isCutoff(val, cutoff, moves)) {
                        return cutoff;
                    }
                    bestVal = updateBest(val, bestVal, n, moves);
                }
            }
            if (outOfTime() || bestVal == I_WIN) {
                return bestVal;
            }
        }
        return bestVal;
    }

    /** Returns the new best value, given VAL and BESTVAL
     *  and N. Updates MOVES appropriately. */
    private int updateBest(int val, int bestVal, int n,
                           ArrayList<Integer> moves) {
        if (val > bestVal) {
            if (moves != null) {
                moves.clear();
            }
            bestVal = val;
            if (moves != null) {
                moves.add(n);
            }
        }
        return bestVal;
    }

    /** Returns true if VAL exceeds CUTOFF and updates MOVES. */
    private boolean isCutoff(int val, int cutoff, ArrayList<Integer> moves) {
        if (val >= cutoff && cutoff != I_WIN) {
            if (moves != null) {
                moves.clear();
            }
            return true;
        }
        return false;
    }

    /** Returns the minmax value for player P of playing
     *  at square N on board B at depth D, pruning with
     *  BESTVAL and CUTOFF and writing to MOVES. */
    private int bustAMove(Side p, MutableBoard b, int d, int n,
                          int bestVal, int cutoff) {
        int result;
        b.addSpot(p, b.row(n), b.col(n));
        if (d == 0) {
            result = staticEval(p, b);
        } else {
            result = -minmax(p.opposite(), b, d - 1, -bestVal, null);
            if (result == I_WIN || result == I_LOSE) {
                memoizeBoard(b, p, d, result);
            }
        }
        b.undo();
        return result;
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    int staticEval(Side p, Board b) {
        if (_memory.containsKey(b)) {
            if (p == this.getSide()) {
                return scoreFromMemo(b);
            } else {
                return -scoreFromMemo(b);
            }
        }
        int result;
        if (!b.isLegal(p.opposite())) {
            result = I_WIN;
        } else if (!b.isLegal(p)) {
            result = I_LOSE;
        } else {
            int squareCount = 2 * b.numOfSide(p) - b.numOfSide(p.opposite());
            int vulnerability = 0;
            if (b.numOfSide(Side.WHITE) + level()
                < b.numOfSide(p) + b.numOfSide(p.opposite())) {
                vulnerability = b.vulnerability(p);
            }
            result = squareCount * squareCount * squareCount
                / (2 * vulnerability + 1);
        }
        memoizeBoard(b, p, 0, result);
        return result;
    }

    /** The value of a board on which I have won. */
    private static final int I_WIN = Integer.MAX_VALUE;
    /** The value of a board on which I have lost. */
    private static final int I_LOSE = -I_WIN;

    /** Memo table of boards I've already evaluated, with their values to blue
     *  as well as the highest level to which each has been evaluated. */
    private HashMap<Board, Point> _memory;

    /** The number of moves I look ahead on each turn. */
    private int _level = Defaults.AI_LEVEL;

}
