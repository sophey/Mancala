import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

import java.util.*;

/**
 * A Mancala web service.
 *
 * @author Sophey Dong
 * @version 0.1 2016-12-13
 */

public class MancalaService {
    public static void main(String[] args) {
        // default port
        int port = 9090;

        // parse command line arguments to override defaults
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);

            } catch (NumberFormatException ex) {
                System.err.println("USAGE: java MancalaService [port]");
                System.exit(1);
            }
        }

        // set up an HTTP server to listen on the selected port
        try {
            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            HttpServer server = HttpServer.create(addr, 1);

            server.createContext("/move.html", new MoveHandler());
            server.createContext("/hint.html", new HintHandler());

            server.start();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.err.println("Could not start server");
        }
    }

    static MancalaBoard stringToMancalaBoard(String str) {
        StringTokenizer tok = new StringTokenizer(str, ";");
        String mancala = tok.nextToken();
        String[] stringBoard = mancala.split(",");

        try {
            // convert to int board
            int[] board = new int[stringBoard.length];
            for (int i = 0; i < stringBoard.length; i++) {
                board[i] = Integer.parseInt(stringBoard[i]);
            }

            int p1 = Integer.parseInt(tok.nextToken());
            int p2 = Integer.parseInt(tok.nextToken());
            boolean p1Turn = Boolean.parseBoolean(tok.nextToken());

            return new MancalaBoard(board, p1, p2, p1Turn);

        } catch (NumberFormatException e) {
            System.err.println("error parsing");
        }
        return null;
    }

    /**
     * An HTTP handler for roll requests.
     */
    public static class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            // we assume the query encodes the current state as n, n, n;p1;p2;p1turn;move
            // where n represents the number of stones in the pile at that index
            // p1 & p2 represent the number of stones in each mancala
            // p1turn is true or false, depending on which player's turn it is
            // move is the bucket the player chose

            System.err.println(ex.getRequestURI());
            String q = ex.getRequestURI().getQuery();

            StringBuilder reponse = new StringBuilder();

            // decode the string
            StringTokenizer tok = new StringTokenizer(q, ";");
            if (tok.countTokens() != 5) {
                sendResponse(ex, error(q, "malformed state"));
            } else {
                MancalaBoard mancalaBoard = MancalaService.stringToMancalaBoard(q);
                int move = Integer.parseInt(q.substring(q.lastIndexOf(";") + 1));

                boolean p1Turn = mancalaBoard.getP1Turn();

                if ((!p1Turn && move > 5) || (p1Turn && move < 6)) {
                    sendResponse(ex, error(q, "selected wrong side"));
                } else {
                    // move is legal!

                    if (p1Turn) {
                        move -= 6;
                    } else {
                        move = 11 - move;
                    }
                    mancalaBoard = Mancala.move(move, mancalaBoard);

                    boolean isGameOver = mancalaBoard.isGameOver();

                    StringBuilder newState = new StringBuilder();
                    if (isGameOver) {
                        for (int i = 0; i < 12; i++) {
                            newState.append("0,");
                        }
                    } else {
                        for (int n : mancalaBoard.getBoard()) {
                            newState.append(n + ",");
                        }
                    }
                    // build the response object
                    Map<String, String> response = new HashMap<String, String>();
                    response.put("state", q);
                    response.put("mancala", newState.toString());
                    response.put("turn", "" + (mancalaBoard.getP1Turn() ? 1 : 2));
                    response.put("p1", "" + mancalaBoard.getP1());
                    response.put("p2", "" + mancalaBoard.getP2());
                    response.put("over", "" + isGameOver);
                    response.put("winner", (mancalaBoard.getWinner() > 0 ? "1" : "2"));

                    sendResponse(ex, response);
                }
            }
        }
    }

    /**
     * An HTTP handler for hint requests.
     */
    public static class HintHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            System.err.println(ex.getRequestURI());
            String q = ex.getRequestURI().getQuery();
            StringTokenizer tok = new StringTokenizer(q, ";");

            MancalaBoard mancalaBoard = MancalaService.stringToMancalaBoard(q);

            if (mancalaBoard == null) {
                error(q, "not correct string");
            }

            int hint = Mancala.chooseMove(mancalaBoard);
            if (mancalaBoard.getP1Turn())
                hint = hint + 6;
            else
                hint = 11 - hint;

            Map<String, String> response = new HashMap<String, String>();
            response.put("state", q);
            response.put("hint", "" + hint);

            sendResponse(ex, response);
        }
    }

    /**
     * Returns a map containing key-value pairs for the given state and message.
     *
     * @param state   a string
     * @param message a string
     * @return a map containing key-value pairs for state and message
     */
    private static Map<String, String> error(String state, String message) {
        Map<String, String> result = new HashMap<String, String>();

        result.put("state", state);
        result.put("message", message);

        return result;
    }

    /**
     * Sends a JSON object as a response in the given HTTP exchange.  Each key-value pair
     * in the given map will be copied to the JSON object.
     *
     * @param ex   an HTTP exchange
     * @param info a non-empty map
     */
    private static void sendResponse(HttpExchange ex, Map<String, String> info) throws IOException {
        // write the response as JSON
        StringBuilder response = new StringBuilder("{");
        for (Map.Entry<String, String> e : info.entrySet()) {
            response.append("\"").append(e.getKey()).append("\":")
                    .append("\"").append(e.getValue()).append("\",");
        }
        response.deleteCharAt(response.length() - 1); // remove last ,
        response.append("}"); // close JSON

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.toString().getBytes();
        ex.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseBytes.length);
        ex.getResponseBody().write(responseBytes);
        ex.close();
    }
}
