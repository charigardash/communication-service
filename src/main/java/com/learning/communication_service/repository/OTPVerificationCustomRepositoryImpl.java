package com.learning.communication_service.repository;

import com.learning.communication_service.dbEntity.OTPVerification;
import com.learning.communication_service.enums.OTPType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class OTPVerificationCustomRepositoryImpl implements OTPVerificationCustomRepository{

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void invalidatePreviousOtps(String identifier, OTPType type) {
        Query query = new Query();
        query.addCriteria(Criteria.where("identifier").is(identifier)
                .and("type").is(type)
                .and("used").is(false));
        Update update = new Update().set("used", true);
        mongoTemplate.updateMulti(query, update, OTPVerification.class);
    }
}
