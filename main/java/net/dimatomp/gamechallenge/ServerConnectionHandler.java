package net.dimatomp.gamechallenge;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Map;

import ru.ifmo.ctddev.games.messages.InventoryMessage;
import ru.ifmo.ctddev.games.messages.JoinMessage;
import ru.ifmo.ctddev.games.messages.MoveMessage;
import ru.ifmo.ctddev.games.messages.MoveResponseMessage;
import ru.ifmo.ctddev.games.messages.StartMessage;
import ru.ifmo.ctddev.games.messages.StoreMessage;
import ru.ifmo.ctddev.games.messages.UserVote;
import ru.ifmo.ctddev.games.messages.VoteMessage;
import ru.ifmo.ctddev.games.messages.VoteResponseMessage;
import ru.ifmo.ctddev.games.messages.VotesInformationResponseMessage;
import ru.ifmo.ctddev.games.state.InventoryItem;
import ru.ifmo.ctddev.games.state.Item;
import ru.ifmo.ctddev.games.state.Poll;

import static net.dimatomp.gamechallenge.GameDatabaseColumns.*;

public class ServerConnectionHandler extends Service {
    private static final String TAG = "ServerConnectionHandler";
    private static final IO.Options options = new IO.Options() {{
        reconnection = false;
        /*
        reconnectionAttempts = 5;
        reconnectionDelay = 4000;
        */
        forceNew = true;
    }};
    Socket socket;
    StartMessage startInfo;
    ObjectMapper mapper;
    private ServerConnectionBinder connectionBinder = new ServerConnectionBinder();

    @Override
    public IBinder onBind(Intent intent) {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JsonOrgModule());
        }
        return connectionBinder;
    }

    @Override
    public void onDestroy() {
        if (socket != null)
            socket.disconnect();
        Log.v(TAG, "Service instance destroyed");
        super.onDestroy();
    }

    enum ConnectionStatus {IDLE, JOINING_IN, FAILED_TO_JOIN, SUCCESS}

    public class ServerConnectionBinder extends Binder {
        private ConnectionStatus connectionStatus = ConnectionStatus.IDLE;
        private LoginForm loginForm;
        private GameField gameField;

        void showLoginErrorMessage(final int res) {
            if (loginForm != null)
                loginForm.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginForm.showError(res);
                    }
                });
        }

        void refreshGameField(final int loaderId) {
            if (gameField != null)
                gameField.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gameField.updateInfo(loaderId);
                    }
                });
        }

        public void setLoginForm(LoginForm loginForm) {
            this.loginForm = loginForm;
        }

        public void setGameField(GameField field) {
            this.gameField = field;
        }

        public boolean isConnecting() {
            return connectionStatus == ConnectionStatus.JOINING_IN;
        }

        public void logIn(String serverAddress, final String userName) {
            if (socket != null) {
                Log.w(TAG, "There already was another Socket.io connection - forgetting about it.");
                socket = null;
            }
            connectionStatus = ConnectionStatus.JOINING_IN;
            try {
                socket = IO.socket(serverAddress, options);
            } catch (URISyntaxException e) {
                connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                showLoginErrorMessage(R.string.error_bad_server_address);
                return;
            }
            socket.on("start", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    if (loginForm == null)
                        return;
                    startInfo = mapper.convertValue(args[0], StartMessage.class);
                    if (startInfo.getSuccess()) {
                        connectionStatus = ConnectionStatus.SUCCESS;
                        loginForm.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginForm.startGame();
                            }
                        });
                        socket.emit("vote_information");
                        socket.emit("get_inventory");
                    } else {
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(R.string.error_server_refused);
                    }
                }
            }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(TAG, "Connected to the server, sending join request.");
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        JoinMessage message = new JoinMessage(userName);
                        socket.emit("join_request", mapper.convertValue(message, JSONObject.class));
                    }
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        if (loginForm == null)
                            return;
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(R.string.error_check_connection);
                    }
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        if (loginForm == null)
                            return;
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(R.string.error_connection_timeout);
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(TAG, "Disconnecting.");
                }
            }).on("move_response", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    if (gameField == null)
                        return;
                    final MoveResponseMessage message = mapper.convertValue(args[0], MoveResponseMessage.class);
                    gameField.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameField.handleMoveResponse(message);
                        }
                    });
                }
            }).on("vote_information", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    GameDatabase.cleanupTable(ServerConnectionHandler.this, POLLS_TABLE);
                    GameDatabase.cleanupTable(ServerConnectionHandler.this, VOTE_OPTIONS);
                    VotesInformationResponseMessage message = mapper.convertValue(args[0], VotesInformationResponseMessage.class);
                    for (Poll poll : message.getPolls()) {
                        UserVote ansNumber = message.getVoted().get(poll.getId());
                        GameDatabase.addPoll(ServerConnectionHandler.this, poll, ansNumber);
                    }
                    refreshGameField(GameField.LOADER_POLLS);
                }
            }).on("new_vote", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Poll poll = mapper.convertValue(args[0], Poll.class);
                    GameDatabase.addPoll(ServerConnectionHandler.this, poll, null);
                    refreshGameField(GameField.LOADER_POLLS);
                }
            }).on("inventory", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    InventoryMessage items = mapper.convertValue(args[0], InventoryMessage.class);
                    GameDatabase.cleanupTable(ServerConnectionHandler.this, INVENTORY_TABLE);
                    for (InventoryItem item: items.getInventory().values())
                        GameDatabase.addInvItem(ServerConnectionHandler.this, item);
                    refreshGameField(GameField.LOADER_INVENTORY);
                }
            }).on("store", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    StoreMessage message = mapper.convertValue(args[0], StoreMessage.class);
                    GameDatabase.cleanupTable(ServerConnectionHandler.this, STORE_TABLE);
                    if (message.isSuccess())
                        for (Item item: message.getItems().values())
                            GameDatabase.addStoreItem(ServerConnectionHandler.this, item);
                    refreshGameField(GameField.LOADER_STORE);
                }
            });

            socket.connect();
        }

        public void sendVoteMessage(final String pollName, final String optionName, final int amount) {
            Cursor pollInfo = GameDatabase.getPollDataByName(ServerConnectionHandler.this, pollName);
            int pollId = pollInfo.getInt(pollInfo.getColumnIndex(GameDatabaseColumns._ID));
            socket.once("vote_response", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    VoteResponseMessage message = mapper.convertValue(args[0], VoteResponseMessage.class);
                    if (message.getSuccessful())
                        GameDatabase.chooseOption(ServerConnectionHandler.this, pollName, optionName, amount);
                }
            });
            VoteMessage message = new VoteMessage(pollId, optionName, amount);
            socket.emit("vote", mapper.convertValue(message, JSONObject.class));
        }

        public void sendMoveRequest(int dx, int dy) {
            MoveMessage message = new MoveMessage(dx, dy);
            socket.emit("move_request", mapper.convertValue(message, JSONObject.class));
        }

        public void requestStore() {
            socket.emit("get_store");
        }

        public void logOut() {
            socket.disconnect();
        }

        public StartMessage getStartInfo() {
            return startInfo;
        }
    }
}
