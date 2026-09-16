package engine;

public class EngineResponse {
    public String text;
    public boolean isCapture;
    public String promotion;
    public boolean isPromotion;
    public boolean isCastling;
    public String fen;
    public String type;
    public int depth;
    public String move;
    public double eval;
    public String centipawns;
    public String mate;
    public String[] continuationArr;
    public double winChance;
    public String taskId;
    public String turn;
    public String color;
    public String piece;
    public int from;
    public int to;
    public String san;
    public String flags;
    public String lan;

    @Override
    public String toString() {
        return String.format("Best move: %s | Eval: %.2f | Depth: %d | FEN: %s | SAN: %s | Turn: %s",
                move, eval, depth, fen, san, turn);
    }
}