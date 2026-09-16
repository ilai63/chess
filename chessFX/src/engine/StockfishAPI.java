package engine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class StockfishAPI {
    private final String url;
    private final HttpClient client = HttpClient.newHttpClient(); // one client for every (re)connect
    private volatile WebSocket webSocket;
    private volatile boolean isConnected = false;
    private boolean connecting = false;
    private final List<String> pendingFens = new ArrayList<>();
    // the final answer ("bestmove") for every request, by taskId and by fen (both come back in the answer). filled by the websocket thread, read by the game
    private final Map<String, EngineResponse> answers = new ConcurrentHashMap<>();
    // a websocket only allows one send at a time, so every send waits for the previous one
    private CompletableFuture<?> lastSend = CompletableFuture.completedFuture(null);
    
    public boolean isBotConnected() {
    	return isConnected;
    }

    public StockfishAPI(String wsUrl) {
        this.url = wsUrl;
        connect();
    }
    
    private synchronized void connect() {
        if (connecting || isConnected) return;
        connecting = true;

        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new WebSocket.Listener() {
                    // a long message can come in a few parts (one buffer per connection)
                    private final StringBuilder partialMessage = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket ws) {
                        System.out.println("Connected to the server!");

                        // Send queued FENs
                        synchronized (StockfishAPI.this) {
                            webSocket = ws;
                            isConnected = true;
                            connecting = false;
                            for (String fen : pendingFens) {
                                send(fen);
                            }
                            pendingFens.clear();
                        }

                        WebSocket.Listener.super.onOpen(ws);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                        partialMessage.append(data);
                        if (last) {
                            String message = partialMessage.toString();
                            partialMessage.setLength(0);
                            if (message.contains("\"type\":\"bestmove\"")) {
                                EngineResponse res = parseEngineResponse(message);
                                if (res.taskId != null && !res.taskId.isEmpty()) answers.put(res.taskId, res);
                                if (res.fen != null && !res.fen.isEmpty()) answers.put(res.fen, res);
                            }
                        }
                        return WebSocket.Listener.super.onText(ws, data, last);
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                        disconnected(ws);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket ws, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                        disconnected(ws);
                    }
                })
                .whenComplete((ws, error) -> {
                    if (error != null) {
                        System.err.println("Couldn't connect to the engine: " + error.getMessage());
                        disconnected(null);
                    }
                });
    }
    
    // ws = the connection that closed (null if connecting failed). an old connection closing late doesn't touch the new one
    private synchronized void disconnected(WebSocket ws) {
        if (ws != null && ws != webSocket) return;
        isConnected = false;
        connecting = false;
        webSocket = null;
    }

    public synchronized void sendFen(String fen, String taskId, int variants, int depth, int maxThinkingTime) {
        String json = String.format(
                "{\"fen\":\"%s\",\"variants\":%d,\"depth\":%d,\"maxThinkingTime\":%d,\"taskId\":\"%s\"}",
                fen, variants, depth, maxThinkingTime, taskId
        );

        if (isConnected && webSocket != null) {
            send(json);
        } else {
            pendingFens.add(json); // queue it until connected
            connect(); // the connection could have been closed, try again
        }
    }
    
    // caller holds the lock
    private void send(String json) {
        WebSocket ws = webSocket;
        lastSend = lastSend.handle((ok, error) -> null).thenCompose(ignored -> ws.sendText(json, true));
        lastSend.exceptionally(error -> {
            System.err.println("Couldn't send to the engine: " + error.getMessage());
            disconnected(ws);
            return null;
        });
    }
    
    // the answer for a request, found by its taskId or else by the position. null if it didn't arrive (yet)
    public EngineResponse getAnswer(String taskId, String fen) {
        EngineResponse res = taskId == null ? null : answers.get(taskId);
        if (res == null && fen != null) res = answers.get(fen);
        return res;
    }

    private EngineResponse parseEngineResponse(String json) {
        EngineResponse res = new EngineResponse();

        res.type = extractValue(json, "\"type\":\"", "\"");
        res.move = extractValue(json, "\"move\":\"", "\"");
        try {
            res.eval = Double.parseDouble(extractValue(json, "\"eval\":", ","));
        } catch (Exception e) {
            res.eval = 0.0;
        }
        try {
            res.depth = Integer.parseInt(extractValue(json, "\"depth\":", ","));
        } catch (Exception e) {
            res.depth = 0;
        }

        // Parse mate
        String mateStr = extractValue(json, "\"mate\":", ",");
        if (mateStr != null && !mateStr.equals("null") && !mateStr.isEmpty()) {
            res.mate = mateStr.trim();
        } else {
            res.mate = null;
        }

        // Parse fromNumeric as int
        try {
            String fromNum = extractValue(json, "\"fromNumeric\":\"", "\"");
            res.from = (fromNum != null && !fromNum.isEmpty()) ? Integer.parseInt(fromNum.trim()) : -1;
        } catch (NumberFormatException e) {
            res.from = -1;
        }

        // Parse toNumeric as int
        try {
            String toNum = extractValue(json, "\"toNumeric\":\"", "\"");
            res.to = (toNum != null && !toNum.isEmpty()) ? Integer.parseInt(toNum.trim()) : -1;
        } catch (NumberFormatException e) {
            res.to = -1;
        }

        res.fen = extractValue(json, "\"fen\":\"", "\"");
        res.taskId = extractValue(json, "\"taskId\":\"", "\"");
        res.san = extractValue(json, "\"san\":\"", "\"");
        res.turn = extractValue(json, "\"turn\":\"", "\"");

        return res;
    }



    private static String extractValue(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return "";
        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) endIndex = text.length();
        return text.substring(startIndex, endIndex);
    }
}
