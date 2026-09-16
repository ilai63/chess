package engine;

import java.util.List;

import chess.*;

public class FENUtils {
	
    private static final Game game = new Game();

    /**
     * Convert your Board object to a FEN string.
     * @param board The Board instance
     * @param player The side to move
     * @param rawMoveList The moves played so far (for castling rights, en passant and the move counters)
     * @return FEN string
     */
    public static String boardToFEN(Board board, Color player, List<String[]> rawMoveList) {
        StringBuilder fen = new StringBuilder();
        
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(piece.getFENSymbol());
                }
            }
            if (emptyCount > 0) fen.append(emptyCount);
            if (row < 7) fen.append("/");
        }

        // Active color
        fen.append(" ").append(player.getColor().charAt(0));

        // Castling rights (the same rules the game uses)
        fen.append(" ").append(Game.getCastlingRights(rawMoveList, rawMoveList.size()));
        
        // En passant square, only written when a pawn can really take (engines are fine with that)
        fen.append(" ").append(getEnPassantSquare(board, player, rawMoveList));

        fen.append(" ").append(getHalfMoveClock(rawMoveList));

        fen.append(" ").append(getFullMoveNumber(rawMoveList));
        
        return fen.toString();
    }
    
    private static String getEnPassantSquare(Board board, Color player, List<String[]> rawMoveList) {
        if (rawMoveList.isEmpty()) return "-";

        String[] lastMove = rawMoveList.get(rawMoveList.size() - 1);

        // Ensure it's a pawn move
        if (lastMove == null || !"Pawn".equals(lastMove[0])) return "-";

        int from = Integer.parseInt(lastMove[1]);
        int to = Integer.parseInt(lastMove[2]);

        int fromRow = from / 10;
        int toRow = to / 10;
        int col = to % 10;

        // Check if it was a double pawn push (moved 2 rows)
        if (Math.abs(fromRow - toRow) != 2) return "-";

        // En passant target is the square the pawn skipped over
        int epRow = (fromRow + toRow) / 2;
        for (int side = -1; side <= 1; side += 2) {
            if (col + side < 0 || col + side > 7) continue;
            Piece pawn = board.getPiece(toRow, col + side);
            if (pawn instanceof Pawn && pawn.getColor() == player &&
                game.isMoveValid(new Position(toRow, col + side), new Position(epRow, col), player, board, rawMoveList) == MoveType.EN_PASSENT) {
                // Convert to FEN coordinate like "e3"
                return "" + (char) ('a' + col) + (8 - epRow);
            }
        }
        return "-";
    }

    private static int getHalfMoveClock(List<String[]> rawMoveList) {
        // half moves since the last pawn move or capture (that move itself doesn't count)
        int count = 0;
        for (int i = rawMoveList.size() - 1; i >= 0; i--) {
            String[] move = rawMoveList.get(i);
            if (move[0].equals("Pawn") || move[4].equals("true")) break;
            count++;
        }
        return count;
    }

    private static int getFullMoveNumber(List<String[]> rawMoveList) {
        return (rawMoveList.size() / 2) + 1; // each full move = 2 half moves
    }
}
