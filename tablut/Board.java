package tablut;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;
import java.util.List;
import java.util.Formatter;

import static tablut.Move.ROOK_MOVES;
import static tablut.Piece.*;
import static tablut.Square.*;


/** The state of a Tablut Game.
 *  @author Andrew Kaplan
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();

        this._turn = model.turn();
        this._winner = model.winner();
        this._moveLimit = model._moveLimit;
        this._moveCount = model.moveCount();
        this._repeated = model._repeated;
        this._castle = model._castle;


        Piece [][] copy = new Piece[SIZE][SIZE];

        for (int i = 0; i < SIZE; i += 1) {
            for (int j = 0; j < SIZE; j += 1) {
                copy[i][j] = _board[i][j];
            }
        }

        this._board = copy;

        this._boardStrings = new HashSet<>();
        this._boardStrings.addAll(model._boardStrings);

        this._turnNodes = new Stack<>();
        this._turnNodes.addAll(model._turnNodes);

        this._gameMoves = new Stack<>();
        this._gameMoves.addAll(model._gameMoves);
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = BLACK;
        _winner = null;
        _moveLimit = Integer.MAX_VALUE / 2;
        _moveCount = 0;
        _repeated = false;
        _board = new Piece[SIZE][SIZE];
        _castle = new HashSet<>();
        _boardStrings = new HashSet<>();
        _turnNodes = new Stack<>();
        _gameMoves = new Stack<>();

        for (int i = 0; i < SIZE; i += 1) {
            for (int j = 0; j < SIZE; j += 1) {
                _board[j][i] = EMPTY;
            }
        }
        for (Square sq: INITIAL_ATTACKERS) {
            put(BLACK, sq);
        }
        for (Square sq: INITIAL_DEFENDERS) {
            put(WHITE, sq);
        }
        castle();
        put(KING, THRONE);
        _boardStrings.add(encodedBoard());
    }

    /** Init _castle HashSet for easy access. */
    void castle() {
        _castle.add(THRONE);
        _castle.add(NTHRONE);
        _castle.add(STHRONE);
        _castle.add(ETHRONE);
        _castle.add(WTHRONE);
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount(). */
    void setMoveLimit(int lim) {
        _moveLimit = lim;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        String currentBoard = encodedBoard();
        if (_boardStrings.contains(currentBoard)) {
            _winner = _turn;
        } else {
            _boardStrings.add(currentBoard);
        }

    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (Square k : pieceLocations(WHITE)) {
            if (get(k) == KING) {
                return k;
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /** Set square TO to P and record square FROM to P for undoing. */
    final void revPutMove(Piece p, Square to, Square from) {
        MoveNode curr = new MoveNode(p, from);
        _turnNodes.add(curr);
        put(p, to);

    }

    /** Set square SQ to EMPTY and record square SQ to CAPTURED for undoing. */
    final void revPutCapture(Piece captured, Piece empty, Square sq) {
        MoveNode capture = new MoveNode(captured, sq);
        _turnNodes.add(capture);
        put(empty, sq);

    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        int dir = from.direction(to);
        int stepSize = 1;
        if (get(to) != EMPTY) {
            return false;
        }
        while (from.rookMove(dir, stepSize) != to) {
            if (get(from.rookMove(dir, stepSize)) != EMPTY) {
                return false;
            }
            stepSize += 1;
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        assert winner() == null;
        if (to == THRONE) {
            if (get(from) != KING) {
                return false;
            }
        }
        return isUnblockedMove(from, to);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from);
        if (2 * _moveLimit <= _moveCount) {
            _winner = _turn.opponent();
            return;
        } else if (!hasMove(get(from))) {
            _winner = _turn.opponent();
            return;
        }
        assert isLegal(from, to);
        Piece fromPiece = get(from);
        Piece toPiece = get(to);

        revPutMove(fromPiece, to, from);
        revPutMove(toPiece, from, to);

        for (int dir = 0; dir < 4; dir += 1) {
            if (to.rookMove(dir, 2) != null) {
                capture(to, to.rookMove(dir, 2));
            }

        }
        Stack<MoveNode> temp = new Stack<MoveNode>();
        while (!_turnNodes.isEmpty()) {
            temp.add(_turnNodes.pop());
        }
        _gameMoves.add(temp);

        if (fromPiece == KING && to.isEdge()) {
            _winner = WHITE;
            return;
        }

        if (winner() != null) {
            return;
        }

        _turn = _turn.opponent();
        checkRepeated();
        _moveCount += 1;

    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square sq1 = sq0.between(sq2);
        Piece p1 = get(sq1);

        if (!hostileConditions(sq1, sq2)) {
            return;
        }
        if (_castle.contains(sq1)) {
            throneCapture(sq0, sq1, sq2);
        } else {
            if (get(sq2) == _turn) {
                if (get(sq1) == KING) {
                    _winner = BLACK;
                }
                revPutCapture(p1, EMPTY, sq1);
            } else if (_turn == WHITE && get(sq2) == KING) {
                if (p1 == BLACK) {
                    revPutCapture(p1, EMPTY, sq1);
                }
            }
        }
    }

    /** Helper function for capture, determines whether Square SQ1
     *  and Square SQ2 constitute hostile conditions needed to capture.
     *  return TRUE or FALSE*/
    private boolean hostileConditions(Square sq1, Square sq2) {
        if (get(sq1) == _turn.opponent() && (get(sq2) == _turn
                || _castle.contains(sq2))) {
            return true;
        } else if (get(sq1) == KING && (get(sq2) == BLACK
                || _castle.contains(sq2))) {
            return true;
        }
        return false;
    }

    /** Helper Function for capture, handles situation where prospective
     * capture of piece on Square SQ1 by pieces on Square SQ0 and
     * Square SQ2 occurs when SQ1 is on the throne. */
    private void throneCapture(Square sq0, Square sq1, Square sq2) {
        int hostileThrown = 0;
        Piece p0 = get(sq0);
        Piece p1 = get(sq1);
        Piece p2 = get(sq2);

        if (sq1 != THRONE && p1 != KING) {
            if (sq2 != THRONE || p1 == BLACK) {
                revPutCapture(p1, EMPTY, sq1);
            } else if (sq2 == THRONE && p2 == EMPTY) {
                revPutCapture(p1, EMPTY, sq1);
            } else {
                for (Square t : _castle) {
                    if (get(t) == BLACK) {
                        hostileThrown += 1;
                    }
                }
                if (hostileThrown == 3) {
                    revPutCapture(p1, EMPTY, sq1);
                }
            }
        } else {
            Square diag1 = sq0.diag1(sq1);
            Square diag2 = sq0.diag2(sq1);
            if (p0 == BLACK  && (p2 == BLACK || sq2 == THRONE)
                    && (get(diag1) == BLACK  || diag1 == THRONE)
                    && (get(diag2) == BLACK  || diag2 == THRONE)) {
                revPutCapture(p1, EMPTY, sq1);
                _winner = BLACK;
            }
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            if (!_gameMoves.isEmpty()) {
                Stack<MoveNode> turnNodes = _gameMoves.pop();
                undoTurnNodes(turnNodes);
                _turn = _turn.opponent();
                undoPosition();
                _moveCount -= 1;
            }
        }

    }
    /** Undo each board change in a single move using TURNNODES.
     * Has no effect on the initial board. */
    void undoTurnNodes(Stack<MoveNode> turnNodes) {
        while (!turnNodes.isEmpty()) {
            MoveNode n = turnNodes.pop();
            if (n.nodeP() == KING) {
                _winner = null;
            }
            put(n.nodeP(), n.nodeSq());
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        if (_moveCount > 0) {
            _boardStrings.remove(encodedBoard());
        }
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        _gameMoves.clear();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        ArrayList<Move> sideMoves = new ArrayList<>();
        for (Square sq: pieceLocations(side)) {
            for (int dir = 0; dir < 4; dir += 1) {
                for (Move m : ROOK_MOVES[sq.index()][dir]) {
                    if (isLegal(m)) {
                        sideMoves.add(m);
                    }
                }
            }

        }
        if (sideMoves.isEmpty()) {
            return null;
        }
        return sideMoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        return legalMoves(side) != null;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> sideLocations = new HashSet<>();
        for (Square sq : SQUARE_LIST) {
            if (get(sq) == side) {
                sideLocations.add(sq);
            } else if (side == WHITE && get(sq) == KING) {
                sideLocations.add(sq);
            }
        }
        return sideLocations;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** Instance variable of the board - 2d array of pieces. */
    private Piece [][] _board;
    /** Throne squares HashSet for easy access. */
    private HashSet<Square> _castle;
    /** String representation of board HashSet to compare board states. */
    private HashSet<String> _boardStrings;
    /** Move limit integer instance. */
    private int _moveLimit;
    /** Stack of MoveNodes representing changes in the game state during
     * a single move. */
    private Stack<MoveNode> _turnNodes;
    /** Stack of stack of MoveNodes (_turnNode instances) representing
     * all changes in the game state since the last clear undo. */
    private Stack<Stack<MoveNode>> _gameMoves;
}
