package com.lea.transport.model;

import java.util.Objects;

/** Parent / emergency contact submitted with each pupil in the September CSV upload. */
public class ParentContact {
    private final String contactId;
    private String name;
    private String email;
    private String emergencyPhone;

    public ParentContact(String contactId, String name, String email, String emergencyPhone) {
        this.contactId = Objects.requireNonNull(contactId);
        this.name = Objects.requireNonNull(name);
        this.email = email;
        this.emergencyPhone = emergencyPhone;
    }

    public String getContactId() { return contactId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String p) { this.emergencyPhone = p; }

    @Override
    public String toString() {
        return String.format("%s <%s> (emergency: %s)", name, email, emergencyPhone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParentContact)) return false;
        return contactId.equals(((ParentContact) o).contactId);
    }

    @Override
    public int hashCode() { return Objects.hash(contactId); }
}
