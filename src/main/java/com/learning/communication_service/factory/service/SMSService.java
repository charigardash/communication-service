package com.learning.communication_service.factory.service;

import com.learning.communication_service.factory.CommunicationChannelService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("SMS")
public class SMSService implements CommunicationChannelService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    //TODO
    @PostConstruct
    public void init(){
        Twilio.init(accountSid, authToken);
    }


    @Override
    public void sendOTP(String toPhoneNumber, String otp) {

        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    "Your OTP code is: " + otp + ". This code will expire in 5 minutes."
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP SMS",e);
        }
    }
}
