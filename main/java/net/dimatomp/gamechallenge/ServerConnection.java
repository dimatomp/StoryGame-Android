package net.dimatomp.gamechallenge;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by dimatomp on 07.09.14.
 */
public class ServerConnection {
    private String serverName, userName;
    private Socket socket;

    public ServerConnection(String serverName, final String userName, final LoginForm loginForm) {
        this.userName = userName;
        this.serverName = serverName;
        try {
            socket = IO.socket(serverName);
        } catch (URISyntaxException e) {
            loginForm.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginForm.showError(R.string.error_bad_server_address);
                }
            });
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    socket.emit("join_request", new JSONObject().put("userName", userName));
                } catch (JSONException e) {
                    loginForm.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginForm.showError(R.string.error_json_exception_send);
                        }
                    });
                    socket.disconnect();
                }
            }
        }).on("start", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                loginForm.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginForm.startGame(ServerConnection.this, args);
                    }
                });
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                loginForm.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginForm.showError(R.string.error_check_connection);
                    }
                });
                socket.disconnect();
            }
        });
        socket.connect();
    }

    public void logOut() {
        socket.disconnect();
    }
}
