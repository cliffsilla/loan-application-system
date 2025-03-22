package com.example.lms.service;

import com.example.lms.entity.Customer;
import com.example.lms.repository.CustomerRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MessageChannel cbsRequestChannel;

    @Override
    public Customer subscribeCustomer(String customerNumber) throws JAXBException {
        // Check if customer already exists
        Optional<Customer> existingCustomer = customerRepository.findByCustomerNumber(customerNumber);
        if (existingCustomer.isPresent()) {
            throw new RuntimeException("Customer already subscribed");
        }

        // Call CBS to get KYC data (using Spring Integration)
        String soapRequest = generateCustomerRequest(customerNumber);
        Message<String> message = MessageBuilder.withPayload(soapRequest).build();
        cbsRequestChannel.send(message);

        // Assuming the response is handled by a separate service activator and transformer
        // For now, let's mock the response
        CustomerResponse kycData = parseCustomerResponse("<CustomerResponse xmlns=\"http://credable.io/cbs/customer\">\n" +
                "    <customer>\n" +
                "      <createdAt>2024-01-26T12:00:00+00:00</createdAt>\n" +
                "      <customerNumber>"+customerNumber+"</customerNumber>\n" +
                "      <firstName>John</firstName>\n" +
                "      <lastName>Doe</lastName>\n" +
                "      <monthlyIncome>5000.0</monthlyIncome>\n" +
                "    </customer>\n" +
                "  </CustomerResponse>");

        // Create and save new customer
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        customer.setKycData(convertObjectToJSON(kycData)); // Store KYC data as JSON
        customer.setCreatedAt(OffsetDateTime.now());
        customer.setUpdatedAt(OffsetDateTime.now());

        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> findByCustomerNumber(String customerNumber) {
        return customerRepository.findByCustomerNumber(customerNumber);
    }

    @Override
    public String getCustomerKycData(String customerNumber) throws JAXBException {
        // This would normally call the CBS via SOAP to get KYC data
        // For now, we'll return a mock JSON string
        String soapRequest = generateCustomerRequest(customerNumber);
        Message<String> message = MessageBuilder.withPayload(soapRequest).build();
        cbsRequestChannel.send(message);

        // Mock response parsing
        CustomerResponse kycData = parseCustomerResponse("<CustomerResponse xmlns=\"http://credable.io/cbs/customer\">\n" +
                "    <customer>\n" +
                "      <createdAt>2024-01-26T12:00:00+00:00</createdAt>\n" +
                "      <customerNumber>"+customerNumber+"</customerNumber>\n" +
                "      <firstName>John</firstName>\n" +
                "      <lastName>Doe</lastName>\n" +
                "      <monthlyIncome>5000.0</monthlyIncome>\n" +
                "    </customer>\n" +
                "  </CustomerResponse>");

        return convertObjectToJSON(kycData);
    }

    private String generateCustomerRequest(String customerNumber) throws JAXBException {
        CustomerRequest request = new CustomerRequest();
        request.setCustomerNumber(customerNumber);

        JAXBContext jaxbContext = JAXBContext.newInstance(CustomerRequest.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        StringWriter sw = new StringWriter();
        marshaller.marshal(request, sw);

        return sw.toString();
    }

    private CustomerResponse parseCustomerResponse(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CustomerResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(xml);

        return (CustomerResponse) unmarshaller.unmarshal(reader);
    }

    private String convertObjectToJSON(CustomerResponse customerResponse) {
        // Implement your own logic to convert the CustomerResponse to JSON
        // For simplicity, we'll return a hardcoded JSON string
        return "{"
                + "\"firstName\": \"" + customerResponse.getCustomer().getFirstName() + "\","
                + "\"lastName\": \"" + customerResponse.getCustomer().getLastName() + "\","
                + "\"monthlyIncome\": " + customerResponse.getCustomer().getMonthlyIncome() + ","
                + "\"customerNumber\": \"" + customerResponse.getCustomer().getCustomerNumber() + "\","
                + "\"createdAt\": \"" + customerResponse.getCustomer().getCreatedAt() + "\""
                + "}";
    }

    // JAXB Classes for XML marshalling/unmarshalling

    @XmlRootElement(name = "CustomerRequest", namespace = "http://credable.io/cbs/customer")
    static class CustomerRequest {
        private String customerNumber;

        public String getCustomerNumber() {
            return customerNumber;
        }

        @XmlElement(namespace = "http://credable.io/cbs/customer")
        public void setCustomerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
        }
    }

    @XmlRootElement(name = "CustomerResponse", namespace = "http://credable.io/cbs/customer")
    static class CustomerResponse {
        private Customer customer;

        public Customer getCustomer() {
            return customer;
        }

        @XmlElement(namespace = "http://credable.io/cbs/customer")
        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        static class Customer {
            private String customerNumber;
            private String firstName;
            private String lastName;
            private double monthlyIncome;
            private String createdAt;

            public String getCustomerNumber() {
                return customerNumber;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setCustomerNumber(String customerNumber) {
                this.customerNumber = customerNumber;
            }

            public String getFirstName() {
                return firstName;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public String getLastName() {
                return lastName;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public double getMonthlyIncome() {
                return monthlyIncome;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setMonthlyIncome(double monthlyIncome) {
                this.monthlyIncome = monthlyIncome;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            @XmlElement(namespace = "http://credable.io/cbs/customer")
            public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
            }
        }
    }
}
