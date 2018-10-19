package com.hch.hooney.avaappproject.ListPack.RemoteDevice;

public class RemoteDeviceDAO {
    private String deviceName;
    private String deviceMacAddress;
    private boolean deviceUseFlag;
    private String deviceServiceUUID;

    public RemoteDeviceDAO() {
        this.deviceName = null;
        this.deviceMacAddress = null;
        this.deviceUseFlag = false;
        this.deviceServiceUUID = null;
    }

    public RemoteDeviceDAO(String deviceName, String deviceMacAddress, boolean deviceUseFlag,
                           String deviceServiceUUID) {
        this.deviceName = deviceName;
        this.deviceMacAddress = deviceMacAddress;
        this.deviceUseFlag = deviceUseFlag;
        this.deviceServiceUUID = deviceServiceUUID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void setDeviceMacAddress(String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }

    public boolean isDeviceUseFlag() {
        return deviceUseFlag;
    }

    public void setDeviceUseFlag(boolean deviceUseFlag) {
        this.deviceUseFlag = deviceUseFlag;
    }

    public String getDeviceServiceUUID() {
        return deviceServiceUUID;
    }

    public void setDeviceServiceUUID(String deviceServiceUUID) {
        this.deviceServiceUUID = deviceServiceUUID;
    }
}
