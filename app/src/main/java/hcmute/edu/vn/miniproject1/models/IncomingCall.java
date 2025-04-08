package hcmute.edu.vn.miniproject1.models;

public class IncomingCall {
    private String phoneNumber;
    private String callType;
    private String callDate;
    private String duration;

    public IncomingCall(String phoneNumber, String callType, String callDate) {
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDate = callDate;
        this.duration = ""; // Mặc định là chuỗi rỗng
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCallType() {
        return callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}