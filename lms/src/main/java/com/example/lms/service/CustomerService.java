package com.example.lms.service;

import com.example.lms.entity.Customer;

import jakarta.xml.bind.JAXBException;
import java.util.Optional;

public interface CustomerService {
    Customer subscribeCustomer(String customerNumber) throws JAXBException;
    Optional<Customer> findByCustomerNumber(String customerNumber);
    // This method would normally call the CBS to get KYC data
    String getCustomerKycData(String customerNumber) throws JAXBException;
}
