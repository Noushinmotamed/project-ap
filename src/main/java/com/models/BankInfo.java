package com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

@Embeddable
public class BankInfo {
    @Column
    private String bankName;
    @Column( unique = true)
    private String accountNumber;
    public BankInfo(String bankName, String accountNumber) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }
    public BankInfo() {}
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

}
