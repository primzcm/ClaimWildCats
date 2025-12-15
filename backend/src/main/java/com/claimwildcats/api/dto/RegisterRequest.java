package com.claimwildcats.api.dto;

public class RegisterRequest {

    private String name;
    private String username;
    private String email;
    private String idNumber;
    private String contactNumber;
    private String department;
    private String password;
    // getters and setters

    public void setName(String name){ this.name  = name;}
    public String getName(){
        return name;
    }

    public void setUsername(String username) { this.username = username; }
    public String getUsername(){
        return username;
    }

    public void setEmail(String email) { this.email = email; }
    public String getEmail(){
        return email;
    }

    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getIdNumber(){
        return idNumber;
    }

    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getContactNumber(){
        return contactNumber;
    }

    public void setDepartment(String department) { this.department = department; }
    public String getDepartment(){
        return department;
    }

    public void setPassword(String password) { this.password = password; }
    public String getPassword(){
        return password;
    }
}
