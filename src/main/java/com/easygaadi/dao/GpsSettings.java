package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "gpssettings")
public final class GpsSettings extends AbstractDocument{
    private int minStopTime;
    private ObjectId accountId;
}
