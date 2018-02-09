package com.example.sfd.tcptool;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity
        implements View.OnClickListener{

    private String TAG = "WifiCommunication";
    private final String AP_IP_ADDR = "192.168.4.1";
    private final int AP_IP_PORT = 5050;
    public static final int WIFI_CONNECT_SUCCESS = 1;
    public final int RECEIVE_AP_DATA = 10;
    public final int SEND_AP_DATA = 11;

    private Button btnScan;
    private Button btnCheck;
    private Button btnOpen;
    private Button btnClose;
    private Button btnSend;
    private Button btnConSever;
    private TextView textReceive;
    private EditText edtSend;
    private EditText edtSeverIp;
    private EditText edtSeverPort;
    private ListView mListView;
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    public int level;
    protected String ssid;

    private IntentFilter intentFilter;
    private WifiReceiver mWifiStateReceiver;
    public static Handler mHandler;
    private static TcpClient tcpClient = null;
    ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_communication);

        getWindow().setSoftInputMode
                ( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mWifiAdmin = new WifiAdmin(MyApplication.getContext());
        initViews();
        //必须调用该方法，否则滚动条无法拖动
        textReceive.setMovementMethod(new ScrollingMovementMethod());

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case WIFI_CONNECT_SUCCESS:
//                        tcpClient = new TcpClient(AP_IP_ADDR.toString(),
//                                AP_IP_PORT);
//                        exec.execute(tcpClient);
                        break;
                    case SEND_AP_DATA:
                        textReceive.append("Tx: "+msg.obj.toString()+"\r\n");
                        break;
                    case RECEIVE_AP_DATA:
                        textReceive.append("Rx: "+msg.obj.toString()+"\r\n");
                        break;
                    default:
                        break;
                }
            }
        };

        intentFilter = new IntentFilter();
        mWifiStateReceiver = new WifiReceiver();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.EXTRA_WIFI_STATE);
        intentFilter.addAction("tcpClientReceiver");

        registerReceiver(mWifiStateReceiver, intentFilter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int i, long l) {
//                ssid = mWifiList.get(i).SSID;
//                mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssid, "0", 1));
//                Toast.makeText(MyApplication.getContext(), "准备连接:"+ssid,
//                        Toast.LENGTH_SHORT).show();

                ssid = mWifiList.get(i).SSID;

                AlertDialog.Builder dialog = new AlertDialog.Builder
                        (MainActivity.this);
                dialog.setTitle("WIFI登录");
                LayoutInflater layoutInflater = LayoutInflater.from
                        (MainActivity.this);
                final View myLogin = layoutInflater.inflate
                        (R.layout.my_login_view, null);
                dialog.setView(myLogin);

                EditText loginAccountEt = (EditText) myLogin.
                        findViewById(R.id.text_ssid);
                loginAccountEt.setText(ssid);

                dialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface,
                                                int i) {
                                EditText loginPasswordEt = (EditText) myLogin.
                                        findViewById(R.id.edit_key);
                                String password = loginPasswordEt.
                                        getText().toString();
                                if(password.equals("")){
                                    mWifiAdmin.addNetwork(mWifiAdmin.
                                            createWifiInfo(ssid, "0", 1));
                                }else {
                                    mWifiAdmin.addNetwork(mWifiAdmin.
                                            createWifiInfo(ssid, loginPasswordEt.
                                                    getText().toString(), 3));
                                }
                            }
                        });
                dialog.setNegativeButton("取消", new DialogInterface.
                        OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
            }
        });
    }

    private void initViews(){
        btnScan = (Button) findViewById(R.id.btn_scan_wifi);
        btnCheck = (Button) findViewById(R.id.btn_check_wifi);
        btnOpen = (Button) findViewById(R.id.btn_open_wifi);
        btnClose = (Button) findViewById(R.id.btn_close_wifi);
        btnSend = (Button) findViewById(R.id.send_data_button);
        btnConSever = (Button) findViewById(R.id.btn_connect_sever);
        mListView = (ListView) findViewById(R.id.list_view_device);
        edtSend = (EditText) findViewById(R.id.send_edit);
        textReceive = (TextView) findViewById(R.id.text_receive);
        edtSeverIp = (EditText) findViewById(R.id.edit_sever_ip);
        edtSeverPort = (EditText) findViewById(R.id.edit_sever_port);
        btnScan.setOnClickListener(this);
        btnCheck.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnConSever.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_scan_wifi:
                mWifiAdmin.startWifiScan(MyApplication.getContext());
                mWifiList=mWifiAdmin.getWifiList();
                if(mWifiList!=null){
                    mListView.setAdapter(new MyAdapter(this,mWifiList));
                    new Utility().setListViewHeightBasedOnChildren(mListView);
                }
                break;
            case R.id.btn_check_wifi:
                mWifiAdmin.checkState(MyApplication.getContext());
                break;
            case R.id.btn_open_wifi:
                mWifiAdmin.openWifi(MyApplication.getContext());
                break;
            case R.id.btn_close_wifi:
                mWifiAdmin.closeWifi(MyApplication.getContext());
                break;
            case R.id.send_data_button:
                Message message = Message.obtain();
                message.what = SEND_AP_DATA;
                message.obj = edtSend.getText().toString();
                mHandler.sendMessage(message);
                exec.execute(new Runnable() {
                    @Override
                    public void run() {
                        tcpClient.send(edtSend.getText().toString());
                    }
                });

                break;
            case R.id.btn_connect_sever:
//                tcpClient = new TcpClient(AP_IP_ADDR,
//                        AP_IP_PORT);
                tcpClient = new TcpClient(edtSeverIp.getText().toString(),
                        getPort(edtSeverPort.getText().toString()));
                exec.execute(tcpClient);
                break;
            default:
                break;
        }
    }

    /*设置listview的高度*/
    public class Utility {
        public void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                return;
            }
            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }
    }

    class WifiReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra
                        (WifiManager.EXTRA_NETWORK_INFO);
                if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
//                    Toast.makeText(MyApplication.getContext(), "wifi disconnected!",
//                            Toast.LENGTH_SHORT).show();
                }
                else if(info.getState().equals(NetworkInfo.State.CONNECTED)){

                    WifiManager wifiManager = (WifiManager)
                            context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    //获取当前wifi名称
                    Toast.makeText(MyApplication.getContext(), "连接到网络 "
                                    + wifiInfo.getSSID(),
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if(intent.getAction().equals("tcpClientReceiver")){
                String msg = intent.getStringExtra("tcpClientReceiver");
                Message message = Message.obtain();
                message.what = RECEIVE_AP_DATA;
                message.obj = msg;
                mHandler.sendMessage(message);
//                Toast.makeText(MyApplication.getContext(), "收到模块数据",
//                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int getPort(String msg){
        if (msg.equals("")){
            msg = "5050";
        }
        return Integer.parseInt(msg);
    }

}
