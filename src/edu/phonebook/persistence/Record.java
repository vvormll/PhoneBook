package edu.phonebook.persistence;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Record {
    private long contactId;
    private String contactName;
    private Set<String> phoneNumbers;
    private String address;
    private String additionalInfo;
    private String label;

    public Record(long contactId, String contactName, String phoneNumber) {
        this.contactId = contactId;
        this.contactName = contactName;
        phoneNumbers = new HashSet<String>();
        phoneNumbers.add(phoneNumber);
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public Record(long contactId, String contactName, Collection<String> phoneNumbers) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.phoneNumbers = new HashSet<String>();
        this.phoneNumbers.addAll(phoneNumbers);
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Set<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Set<String> phNumbers) {
        phoneNumbers = phNumbers;
    }

    public void addPhoneNumber(String phoneNumber) {
        // TODO check with regexp
        phoneNumbers.add(phoneNumber);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
