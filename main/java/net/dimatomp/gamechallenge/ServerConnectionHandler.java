package net.dimatomp.gamechallenge;

import android.app.Service;
import android.content.Intent;
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

import ru.ifmo.ctddev.games.messages.JoinMessage;
import ru.ifmo.ctddev.games.messages.MoveMessage;
import ru.ifmo.ctddev.games.messages.MoveResponseMessage;
import ru.ifmo.ctddev.games.messages.StartMessage;
import ru.ifmo.ctddev.games.messages.UserVote;
import ru.ifmo.ctddev.games.messages.VotesInformationResponseMessage;
import ru.ifmo.ctddev.games.state.Poll;

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

    enum ConnectionStatus {IDLE, JOINING_IN, FAILED_TO_JOIN, SUCCESS, VOTES_RETRIEVED}

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
                    if (connectionStatus == ConnectionStatus.SUCCESS) {
                        PollDatabase.dropEverything(ServerConnectionHandler.this);
                        VotesInformationResponseMessage message = mapper.convertValue(args[0], VotesInformationResponseMessage.class);
                        for (Poll poll : message.getPolls()) {
                            UserVote ansNumber = message.getVoted().get(poll.getId());
                            PollDatabase.addPoll(ServerConnectionHandler.this, poll, ansNumber);
                        }
                        connectionStatus = ConnectionStatus.VOTES_RETRIEVED;
                        if (gameField != null)
                            gameField.updateVoteInfo();
                    }
                }
            }).on("new_vote", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    socket.emit("vote_information");
                }
            });

            socket.connect();
        }

        public boolean isVotesRetrieved() {
            return connectionStatus == ConnectionStatus.VOTES_RETRIEVED;
        }

        public void sendMoveRequest(int dx, int dy) {
            MoveMessage message = new MoveMessage(dx, dy);
            socket.emit("move_request", mapper.convertValue(message, JSONObject.class));
        }

        public StartMessage getStartInfo() {
            return startInfo;
        }
    }
}
