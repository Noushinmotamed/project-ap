package com.models;

import com.google.gson.annotations.SerializedName;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;

@Embeddable
public class BankInfo {
    @Column
    @SerializedName("bank_name")
    private String bankName;
    @Column
    @SerializedName("account_number")
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
