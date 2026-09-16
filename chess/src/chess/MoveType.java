package chess;

public enum MoveType {
	INVALID,
    NORMAL,
    CASTLING_KINGSIDE,
    CASTLING_QUEENSIDE,
    EN_PASSENT,
	PROMOTION;
    
    public String getMoveType() {
		switch (this) {
		case INVALID: return "invalid";
		case NORMAL: return "normal";
		case CASTLING_KINGSIDE: return "castlingKingSide";
		case CASTLING_QUEENSIDE: return "castlingQueenSide";
		case EN_PASSENT: return "enPassent";
		case PROMOTION: return "promotion";
		default: return "no color found";
		}
	}
}
