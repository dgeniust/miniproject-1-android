package hcmute.edu.vn.miniproject1.models;

public class BlacklistContact {
    private String phoneNumber;
    private String name;
    private long dateAdded;

    public BlacklistContact() {
    }

    public BlacklistContact(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.dateAdded = System.currentTimeMillis();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BlacklistContact contact = (BlacklistContact) obj;
        return phoneNumber != null ? phoneNumber.equals(contact.phoneNumber) : contact.phoneNumber == null;
    }

    @Override
    public int hashCode() {
        return phoneNumber != null ? phoneNumber.hashCode() : 0;
    }
}
