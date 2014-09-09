package net.dimatomp.gamechallenge;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class ServerConnectionHandler extends Service {
    Socket socket;
    JSONObject startInfo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return connectionBinder;
    }

    private ServerConnectionBinder connectionBinder = new ServerConnectionBinder();

    private static final String TAG = "ServerConnectionHandler";
    private static final IO.Options options = new IO.Options() {{
        reconnectionAttempts = 6;
        reconnectionDelay = 10000;
        forceNew = true;
    }};

    enum ConnectionStatus {JOINING_IN, FAILED_TO_JOIN, SUCCESS}

    @Override
    public void onDestroy() {
        if (socket != null)
            socket.disconnect();
        Log.v(TAG, "Service instance destroyed");
        super.onDestroy();
    }

    public class ServerConnectionBinder extends Binder {
        private ConnectionStatus connectionStatus;

        void showLoginErrorMessage(final LoginForm loginForm, final int res) {
            loginForm.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginForm.showError(res);
                }
            });
        }

        public void logIn(final LoginForm form, String serverAddress, final String userName) {
            if (socket != null) {
                Log.w(TAG, "There already was another Socket.io connection - forgetting about it.");
                socket = null;
            }
            connectionStatus = ConnectionStatus.JOINING_IN;
            try {
                socket = IO.socket(serverAddress, options);
            } catch (URISyntaxException e) {
                connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                showLoginErrorMessage(form, R.string.error_bad_server_address);
                stopSelf();
            }
            socket.once("start", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    startInfo = (JSONObject) args[0];
                    try {
                        if (startInfo.getBoolean("accepted")) {
                            connectionStatus = ConnectionStatus.SUCCESS;
                            form.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    form.startGame();
                                }
                            });
                        } else {
                            connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                            showLoginErrorMessage(form, R.string.error_server_refused);
                            stopSelf();
                        }
                    } catch (JSONException exception) {
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(form, R.string.error_json_exception_receive);
                        stopSelf();
                    }
                }
            }).once(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        try {
                            socket.emit("join_request", new JSONObject().put("userName", userName));
                        } catch (JSONException e) {
                            connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                            showLoginErrorMessage(form, R.string.error_json_exception_send);
                            stopSelf();
                        }
                    }
                }
            }).once(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(form, R.string.error_check_connection);
                        stopSelf();
                    }
                }
            }).once(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (connectionStatus == ConnectionStatus.JOINING_IN) {
                        connectionStatus = ConnectionStatus.FAILED_TO_JOIN;
                        showLoginErrorMessage(form, R.string.error_connection_timeout);
                        stopSelf();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.i(TAG, "Disconnecting.");
                    stopSelf();
                }
            });

            socket.connect();
        }

        public void setGameField(final GameField field) {
            socket.listeners("move_response").clear();
            socket.listeners("vote_information").clear();
            socket.listeners("vote_result").clear();
            socket.listeners("new_vote").clear();

            socket.on("move_response", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    field.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            field.handleMoveResponse((JSONObject) args[0]);
                        }
                    });
                }
            });
        }

        public boolean sendMoveRequest(int direction) {
            try {
                socket.emit("move_request", new JSONObject().put("direction", direction));
            } catch (JSONException e) {
                return false;
            }
            return true;
        }

        public JSONObject getStartInfo() { // TODO Add an activity as a parameter
            return startInfo;
        }
    }
}
