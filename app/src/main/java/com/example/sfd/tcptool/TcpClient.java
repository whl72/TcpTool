package com.example.sfd.tcptool;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by SFD on 2018/2/5.
 */

public class TcpClient implements Runnable {
    private String TAG = "TcpClient";
    private String  serverIP = "192.168.88.141";
    private int serverPort = 1234;
    private PrintWriter pw;
    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private boolean isRun = true;
    byte buff[]  = new byte[4096];
    private String rcvMsg;
    private int rcvLen;

    private Socket socket;

    public TcpClient(String ip, int port){
        this.serverIP = ip;
        this.serverPort =port;
    }

    public void closeSelf(){
        isRun = false;
    }

    public void send(String msg){
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIP, serverPort);
            socket.setSoTimeout(5000);
            Log.d("FuncTcpClient", "ConnectedStatus : " + socket.isConnected());

            pw = new PrintWriter(socket.getOutputStream(), true);
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        while (isRun){
            try {
                rcvLen = dataInputStream.read(buff);
                rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                Log.i(TAG, "run: 收到消息:"+ rcvMsg);
                Intent intent =new Intent();
                intent.setAction("tcpClientReceiver");
                intent.putExtra("tcpClientReceiver",rcvMsg);
                MyApplication.getContext().sendBroadcast(intent);
                if (rcvMsg.equals("QuitClient")){
                    isRun = false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            pw.close();
            inputStream.close();
            dataInputStream.close();
            socket.close();
            if(socket.isClosed()){
                Toast.makeText(MyApplication.getContext(), "断开连接",
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MyApplication.getContext(), "断开失败",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
