package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

import java.util.List;

/** The suite of all JUnit tests for the enigma package.
 *  @author Andrew Kaplan
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * Tests legalMoves for white pieces to make sure it returns all
     * legal Moves. This method needs to be finished and may need to
     * be changed based on your implementation.
     */
    @Test
    public void testLegalWhiteMoves() {
        Board b = new Board();
        List<Move> movesList = b.legalMoves(Piece.WHITE);
        assertEquals(56, movesList.size());

        assertFalse(movesList.contains(Move.mv("e7-8")));
        assertFalse(movesList.contains(Move.mv("e8-f")));

        assertTrue(movesList.contains(Move.mv("e6-f")));
        assertTrue(movesList.contains(Move.mv("f5-8")));
    }

    /**
     * Tests legalMoves for black pieces to make sure it returns all
     * legal Moves. This method needs to be finished and may need to
     * be changed based on your implementation.
     */
    @Test
    public void testLegalBlackMoves() {
        Board b = new Board();
        List<Move> movesList = b.legalMoves(Piece.BLACK);
        assertEquals(80, movesList.size());

        assertFalse(movesList.contains(Move.mv("e8-7")));
        assertFalse(movesList.contains(Move.mv("e7-8")));

        assertTrue(movesList.contains(Move.mv("f9-i")));
        assertTrue(movesList.contains(Move.mv("h5-1")));
    }

    @Test
    public void testMakeMove() {
        Board b = new Board();

        Move m1 = Move.mv("a4-1");
        b.makeMove(m1);
        assertTrue(b.get('a', '1') == Piece.BLACK);

        Move m2 = Move.mv("e3-c");
        b.makeMove(m2);
        assertTrue(b.get('c', '3') == Piece.WHITE);

        Move m3 = Move.mv("a1-c");
        b.makeMove(m3);
        assertTrue(b.get('c', '1') == Piece.BLACK);
    }

    @Test
    public void testKingPosition() {
        Board b = new Board();

        b.makeMove(Move.mv("h5-6"));
        b.makeMove(Move.mv("e4-b"));
        b.makeMove(Move.mv("h6-7"));
        b.makeMove(Move.mv("e5-4"));
        b.makeMove(Move.mv("h7-8"));
        b.makeMove(Move.mv("e4-h"));

        assertSame(b.get('h', '4'), Piece.KING);
        assertSame(b.get('h', '4'), b.get(b.kingPosition()));
    }


    @Test
    public void testHostileThrone() {
        Board b = new Board();

        b.put(Piece.BLACK, Board.NTHRONE);
        b.put(Piece.BLACK, Board.ETHRONE);
        b.put(Piece.BLACK, Board.WTHRONE);
        b.put(Piece.EMPTY, Board.STHRONE);

        Move black1 = Move.mv("f1-g");
        b.makeMove(black1);

        Move white1 = Move.mv("e3-4");
        b.makeMove(white1);

        Move capture = Move.mv("e2-3");
        b.makeMove(capture);
        assertSame(b.get('e', '4'), Piece.EMPTY);
    }

    @Test
    public void testUndo() {
        Board b = new Board();

        Move black1 = Move.mv("a4-1");
        b.makeMove(black1);

        b.undo();
        assertSame(b.get('a', '4'), Piece.BLACK);

        Move black2 = Move.mv("a4-1");
        b.makeMove(black2);
        Move white1 = Move.mv("e3-d");
        b.makeMove(white1);
        b.undo();
        assertSame(b.get('e', '3'), Piece.WHITE);
        b.undo();
        assertSame(b.get('a', '4'), Piece.BLACK);
    }
}


