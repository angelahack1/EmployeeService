package com.enterprise.soap.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Employee data-transfer object.
 * <p>
 * JAXB annotations control XML marshalling / unmarshalling for SOAP payloads.
 * {@code @XmlAccessorType(FIELD)} tells JAXB to bind fields directly —
 * the most predictable and widely-used strategy.
 * </p>
 */
@XmlRootElement(name = "Employee", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Employee", namespace = "", propOrder = {"id", "firstName", "lastName", "email", "department"})
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(nillable = true)
    private Long id;

    @XmlElement(nillable = true)
    private String firstName;

    @XmlElement(nillable = true)
    private String lastName;

    @XmlElement(nillable = true)
    private String email;

    @XmlElement(nillable = true)
    private String department;

    // ── No-arg constructor (required by JAXB) ──────────────────────────
    public Employee() {
    }

    // ── All-args constructor (convenience) ─────────────────────────────
    public Employee(Long id, String firstName, String lastName,
                    String email, String department) {
        this.id         = id;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.department = department;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee{id=" + id
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", department='" + department + '\''
                + '}';
    }
}