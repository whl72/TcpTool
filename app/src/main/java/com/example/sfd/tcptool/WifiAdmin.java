package com.example.sfd.tcptool;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.sfd.tcptool.MainActivity.WIFI_CONNECT_SUCCESS;

/**
 * Created by SFD on 2018/2/1.
 */

public class WifiAdmin {

    private String TAG = "WifiAdmin";

    private int WIFI_STATE_DISABLING    = 0;
    private int WIFI_STATE_DISABLED     = 1;
    private int WIFI_STATE_ENABLING     = 2;
    private int WIFI_STATE_ENABLED      = 3;
    private int WIFI_STATE_UNKNOWN      = 4;

    public static int level;

    private WifiManager mWifiManger;
    private WifiInfo mWifiInfo;
    //扫描出的WiFi列表
    private List<ScanResult> mWifiList;
    //网络连接列表
    private List<WifiConfiguration> mWifiConfList;
    WifiManager.WifiLock mWifiLock;

    public WifiAdmin(Context context){
        mWifiManger = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManger.getConnectionInfo();
    }

    public void openWifi(Context context){
        if(!mWifiManger.isWifiEnabled()){
            mWifiManger.setWifiEnabled(true);
        }else if(mWifiManger.getWifiState() == 2){
            Toast.makeText(context, "wifi正在开启中...",
                    Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "wifi已经开启！",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // 关闭WIFI
    public void closeWifi(Context context) {
        if (mWifiManger.isWifiEnabled()) {
            mWifiManger.setWifiEnabled(false);
        }else if(mWifiManger.getWifiState() == 1){
            Toast.makeText(context,"亲，Wifi已经关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else if (mWifiManger.getWifiState() == 0) {
            Toast.makeText(context,"亲，Wifi正在关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"请重新关闭", Toast.LENGTH_SHORT).show();
        }
    }

    //查询WiFi当前状态
    public void checkState(Context context){
        switch (mWifiManger.getWifiState()){
            case 0:
                Toast.makeText(context, "wifi正在关闭",
                    Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(context, "wifi已经关闭",
                        Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(context, "wifi正在开启",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, "没有获取到WiFi状态",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    //锁定WiFiLock
    public void acquireWifiLock(){
        mWifiLock.acquire();
    }

    //解锁WiFiLock
    public void releaseWifiLock(){
        if(mWifiLock.isHeld()){
            mWifiLock.acquire();
        }
    }

    //获得配置好的网络
    public List<WifiConfiguration> getConfigList(){
        return mWifiConfList;
    }

    //指定配置好的网络进行连接
    public void connectConfiguration(int index){
        if(index > mWifiConfList.size()){
            return;
        }
        mWifiManger.enableNetwork(mWifiConfList.get(index).networkId,
                true);
    }

    public void startWifiScan(Context context){
        mWifiManger.startScan();
        //得到扫描结果
        List<ScanResult> results = mWifiManger.getScanResults();
        //得到配置好的网络连接
        mWifiConfList = mWifiManger.getConfiguredNetworks();
        if(results == null){
            switch (mWifiManger.getWifiState()){
                case 3:
                    Toast.makeText(context, "当前区域没有无线网络",
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(context, "WiFi正在开启，" +
                            "请稍后点击",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "wifi未开启，" +
                            "无法扫描", Toast.LENGTH_SHORT).show();
                    break;
            }
        }else {
            mWifiList = new ArrayList();
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (ScanResult item : mWifiList) {
                    if (item.SSID.equals(result.SSID) && item.capabilities.equals(result.capabilities)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mWifiList.add(result);
                }
            }
        }
    }

    //得到网络列表
    public List<ScanResult> getWifiList(){
        return mWifiList;
    }

    //查看扫描结果
    public StringBuilder lookUpScan(){
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0;i < mWifiList.size(); i++){
            stringBuilder.append("Index" + new Integer(i+1).
                    toString() + ":");
            stringBuilder.append(mWifiList.get(i).toString());
            stringBuilder.append("/n");
        }

        return stringBuilder;
    }

    //得到Mac地址
    public String getMacAddress(){
        return (mWifiInfo == null) ? "NULL" :
                mWifiInfo.getMacAddress();
    }

    //得到接入点的BSSID
    public String getBSSID(){
        return (mWifiInfo == null) ? "NULL" :
                mWifiInfo.getBSSID();
    }

    //得到IP地址
    public int getIPAddress(){
        return (mWifiInfo == null) ? 0 :mWifiInfo.getIpAddress();
    }

    //得到连接的ID
    public int getNetworkID(){
        return (mWifiInfo == null) ? 0 :mWifiInfo.getNetworkId();
    }

    //得到WiFiInfo的所有信息
    public String getWifiInfo(){
        return (mWifiInfo == null) ? "NULL" :
                mWifiInfo.toString();
    }

    //添加一个网络并连接
    public void addNetwork(WifiConfiguration wcg){
        int wcgID = mWifiManger.addNetwork(wcg);
        boolean b = mWifiManger.enableNetwork(wcgID, true);
        Log.d(TAG, "a--"+wcgID);
        Log.d(TAG, "b--"+b);

        if(b == true){
            MyMessage.sendMyMessage(WIFI_CONNECT_SUCCESS);
//            Toast.makeText(MyApplication.getContext(), "连接AP成功",
//                    Toast.LENGTH_SHORT).show();
        }
    }

    //断开指定ID的网络
    public void dissconnectWifi(int netID){
        mWifiManger.disableNetwork(netID);
        mWifiManger.disconnect();
    }
    public void removeWifi(int netID){
        dissconnectWifi(netID);
        mWifiManger.removeNetwork(netID);
    }

    //然后是一个实际应用方法，只验证过没有密码的情况：

    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if(tempConfig != null) {
            mWifiManger.removeNetwork(tempConfig.networkId);
        }

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            config.hiddenSSID = true;
//            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+Password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration IsExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = mWifiManger.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }
}
