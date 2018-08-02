package com.easygaadi.dao;

import com.easygaadi.util.PreCondition;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "devices")
public final class Device extends AbstractDocument {
    private String imei;
    private DateTime lastHaltedTime;
    private ObjectId accountId;
}
