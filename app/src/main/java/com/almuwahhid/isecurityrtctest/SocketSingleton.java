package com.almuwahhid.isecurityrtctest;

import android.content.Context;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketSingleton {

    private static final String SERVER_ADDRESS = "https://zainrtc.herokuapp.com/";
    //    private static final String SERVER_ADDRESS = SafeTravel.stringBaseChatURL();
    private static SocketSingleton instance;
    private Socket mSocket;
    private Context context;

    public SocketSingleton(Context context) {
        this.context = context;
        this.mSocket = getServerSocket();
    }

    public static SocketSingleton get(Context context) {
        if (instance == null) {
            instance = getSync(context);
        }
        instance.context = context;
        return instance;
    }

    private static synchronized SocketSingleton getSync(Context context) {
        if (instance == null) {
            instance = new SocketSingleton(context);
        }
        return instance;
    }

    public Socket getSocket() {
        return this.mSocket;
    }

    public Socket getServerSocket() {
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            mSocket = IO.socket(SERVER_ADDRESS, opts);
            return mSocket;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
