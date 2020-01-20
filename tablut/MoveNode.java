package tablut;

/** MoveNode class used to store information about changes
 * to the board state. Used in Undo.
 *  @author Andrew Kaplan
 */
public class MoveNode {

    /** Create new MoveNode with Piece P, Square SQ. */
    MoveNode(Piece p, Square sq) {
        _piece = p;
        _square = sq;
    }
    /** Return Piece. */
    Piece nodeP() {
        return _piece;
    }

    /** Return Square. */
    Square nodeSq() {
        return _square;
    }

    /** Piece instance. */
    private Piece _piece;
    /** Square instance. */
    private Square _square;
}
