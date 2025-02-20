package tablut;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Andrew Kaplan
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        String move = findMove().toString();
        System.out.println("* " + move);
        return move;
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int sense;

        if (b.turn() == WHITE) {
            sense = 1;
        } else {
            sense = -1;
        }

        findMove(b, maxDepth(b), true, sense, INFTY * -1, INFTY);

        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE ==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {

        if (depth == 0) {
            return staticScore(board);
        } else if (board.winner() == WHITE) {
            return WINNING_VALUE;
        } else if (board.winner() == BLACK) {
            return -1 * WINNING_VALUE;
        }

        int bestMove = Integer.MIN_VALUE * sense;

        for (Move m : board.legalMoves(board.turn())) {
            board.makeMove(m);
            int moveValue = findMove(board, depth - 1, false,
                    sense * (-1), alpha, beta);
            board.undo();
            if (moveValue * sense >= bestMove * sense) {
                if (saveMove) {
                    _lastFoundMove = m;
                }
                bestMove = moveValue;
                if (sense == 1) {
                    alpha = Integer.max(alpha, moveValue);
                } else {
                    beta = Integer.min(beta, moveValue);
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        return bestMove;
    }



    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 2;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int whiteCount = board.pieceLocations(WHITE).size();
        int blackCount = board.pieceLocations(BLACK).size();
        int pieceCount = whiteCount - blackCount;
        return pieceCount;
    }
}
