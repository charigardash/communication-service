package com.learning.communication_service.factory;

import org.springframework.stereotype.Service;

@Service
public interface CommunicationChannelService {

    void sendOTP(String key, String otp);
}
