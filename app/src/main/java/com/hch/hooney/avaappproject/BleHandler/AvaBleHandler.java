package com.hch.hooney.avaappproject.BleHandler;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.AuthCodeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AvaBleHandler {
    private final String TAG = AvaBleHandler.class.getSimpleName();
    public final int REQUEST_ENABLE = 103;
    private final long SCAN_PERIOD = 30000;

    //Private Variable
    private Handler mHandler;
    private String res, disres;
    private Activity activity;

    //Public Variable
    public BluetoothAdapter bthA;
    public PackageManager pkm;
    public BluetoothLeScanner mLEScanner;
    public ScanSettings settings;
    public List<ScanFilter> filters;

    public BluetoothGatt mGatt;
    public BluetoothGattService nowService;
    public List<UUID> serviceUUIDList;
    public BluetoothGattCharacteristic authBGC;
    public BluetoothGattCharacteristic wifiBGC;
    public BluetoothGattCharacteristic ledBGC;
    public BluetoothGattCharacteristic radioBGC;

    private String beforeMacAddress;

    public String getRes() {
        return res;
    }
    public void setRes(String r){
        this.res = r;
    }
    public void setActivity(Activity ac){
        this.activity = ac;
    }

    public AvaBleHandler(BluetoothAdapter ba, PackageManager pkm, String bmAddress){
        this.bthA = ba;
        this.pkm = pkm;
        this.beforeMacAddress = bmAddress;
        this.mHandler = new Handler();
    }

    /**
     * Ble init
     */
    public boolean checkFeatureBLE(){
        if (pkm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        }
        return false;
    }

    public void onBluetooth(Activity activity){
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), this.REQUEST_ENABLE);
    }

    public boolean isOnBle(){
        return this.bthA.isEnabled();
    }

    /**
     * BLE 검색
     */
    /*API 21 이전 버전*/
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    Log.i(TAG, "device : " + device);
                    if(device.getName() != null){
                        if(device.getName().toLowerCase().contains("ava_rasp")){
                            Log.i(TAG, "Before API Search : " + device.toString());
                            Log.i(TAG, "Before API Search Name: " + device.getName());
                            connectToDevice(device);
                        }
                    }
                }
            };

    /*API 21 이후 버전*/
    private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(beforeMacAddress != null){
                //Mac Address Match....
                if(beforeMacAddress.equals(result.getDevice().getAddress())){
                    if(result.getDevice() != null){
                        if(result.getDevice().getName()!= null){
                            Log.d(TAG, "Device Name : " + result.getDevice().getName());
                            if(result.getDevice().getName().toLowerCase().contains("ava_rasp")){
                                beforeMacAddress = result.getDevice().getAddress();
                                serviceUUIDList = new ArrayList<>();

                                for(ParcelUuid item : result.getScanRecord().getServiceUuids()){
                                    UUID servicUUID = item.getUuid();
                                    if(!serviceUUIDList.contains(servicUUID)){
                                        serviceUUIDList.add(servicUUID);
                                    }
                                }

                                connectToDevice(result.getDevice());
                            }
                        }
                    }
                }
            }else{
                Log.i(TAG, "Scan Result : " + result.toString());
                if(result.getDevice() != null){
                    if(result.getDevice().getName()!= null){
                        Log.d(TAG, "Device Name : " + result.getDevice().getName());
                        if(result.getDevice().getName().toLowerCase().contains("ava_rasp")){
                            beforeMacAddress = result.getDevice().getAddress();
                            serviceUUIDList = new ArrayList<>();

                            for(ParcelUuid item : result.getScanRecord().getServiceUuids()){
                                UUID servicUUID = item.getUuid();
                                if(!serviceUUIDList.contains(servicUUID)){
                                    serviceUUIDList.add(servicUUID);
                                }
                            }

                            connectToDevice(result.getDevice());
                        }
                    }
                }
            }
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    /**
     * Gatt Call Back
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for(BluetoothGattService service : gatt.getServices()){
                if(serviceUUIDList.contains(service.getUuid())){
                    nowService = service;
                }
            }
            Log.d(TAG, "Service : " + nowService.getUuid());
            Log.d(TAG, "Service Size : " + nowService.getCharacteristics().size());
            authBGC = nowService.getCharacteristics().get(0);
            wifiBGC = nowService.getCharacteristics().get(1);
            ledBGC = nowService.getCharacteristics().get(2);
            radioBGC = nowService.getCharacteristics().get(3);

            if(authBGC != null && wifiBGC !=null && ledBGC !=null && radioBGC!=null){
                Log.d(TAG, "Ava Service Discovered...");
                Log.d(TAG, "Stop Scanning");
                AvaApp.saveMacAddress(activity, beforeMacAddress);
            }else{
                scanLeDevice(true);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d(TAG, "Write...");
            Log.d(TAG, "Status = " + status + " / " + BluetoothGatt.GATT_SUCCESS);
            if((status == BluetoothGatt.GATT_SUCCESS)){
                gatt.readCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.d(TAG, "onChangedCharacter");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            String read = byteToString(descriptor.getValue());
            if(read == null){
                Log.d(TAG, "Descriptor Read None...");
            }else{
                Log.d(TAG, "Response : " + read);
            }
        }
    };

    /**
     * BLE Support Tools
     */
    private String byteToString(byte[] input){
        if(input != null && input.length > 0){
            //hex byte to binary byte;
            StringBuilder stringBuilder = new StringBuilder(input.length);
            for(byte byteChar : input){
                stringBuilder.append(String.format("%02X", byteChar));
            }
            //binary byte to String Ascii
            String txt = stringBuilder.toString();
            byte[] txtInByte = new byte[txt.length()/2];
            int j = 0;
            for(int i = 0; i<txt.length(); i+=2){
                txtInByte[j++] = Byte.parseByte(txt.substring(i, i+2), 16);
            }
            return new String(txtInByte);
        }
        return null;
    }

    public void connectToDevice( BluetoothDevice device) {
        if (mGatt == null&& this.activity != null) {
            Log.d(TAG, "Connect to "+ device.getName());
            mGatt = device.connectGatt(this.activity, true, gattCallback);
            scanLeDevice(false);// find Ava
        }
    }

    public boolean BleScanning(){
        Log.d(TAG, "Call : BLE Scanning Method...");
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = bthA.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
        }else{
            return false;
        }
        scanLeDevice(true);

        return true;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        bthA.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                bthA.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                bthA.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    public boolean callBleSearch(){
        boolean flag = false;
        int count = 0;

        if(nowService !=null){
            Log.d(TAG, "already exist Service...");
            flag = true;
        }else{
            Log.d(TAG, "no exist Service...");

            if(mGatt != null){
                mGatt.connect();
            }else{
                BleScanning();
            }

            while(true){
                try{
                    Thread.sleep(500);
                    count++;
                }catch (InterruptedException e){
                    Log.e(TAG, "Thread.Sleep Exception...");
                    e.printStackTrace();
                }

                if(nowService != null){
                    flag = true;
                    break;
                }
                if(count > 60){
                    //1분 검색 시간 초과
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * BLE End Method
     */

    public void disConnected(){
        if(mGatt != null){
            mGatt.disconnect();
        }
    }

    public void stop(){
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    public void pause(){
        if (bthA != null && bthA.isEnabled()) {
            scanLeDevice(false);
        }
    }

    /**
     * Reader Writer Method
     */

    private void readCharacteristic( BluetoothGattCharacteristic characteristic){
        Log.d(TAG, "BLE Character : " + characteristic.getStringValue(0));
        this.res = characteristic.getStringValue(0);
    }

    public void getAuthKey(){
        this.res = null;
        mGatt.readCharacteristic(authBGC);
    }

    public void getWifiState(){
        this.res = null;
        mGatt.readCharacteristic(wifiBGC);
    }

    public boolean writeWifi(String msg){
        BluetoothGattCharacteristic characteristic = wifiBGC;
        if(msg != null){
            characteristic.setValue(msg.getBytes());
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return mGatt.writeCharacteristic(characteristic);
        }else{
            return false;
        }
    }

    public void getLedState(){
        this.res = null;
        mGatt.readCharacteristic(ledBGC);
    }

    public boolean writeLed(String msg){
        BluetoothGattCharacteristic characteristic = ledBGC;
        if(msg != null){
            characteristic.setValue(msg.getBytes());
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            return mGatt.writeCharacteristic(characteristic);
        }else{
            return false;
        }
    }

    public boolean sendDateToBLE(String msg, UUID uuid){
        BluetoothGattCharacteristic characteristic = nowService.getCharacteristic(uuid);

        if(msg != null){
            if(characteristic != null){
                characteristic.setValue(msg.getBytes());
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                return mGatt.writeCharacteristic(characteristic);
            }else{
                Log.e(TAG, "It isn't Connected BLE Server...");
            }
        }else{
            Log.e(TAG, "Empty BLE Send MSG...");
        }
        return false;
    }
}
