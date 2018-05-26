package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "trucks")
public final class Truck extends AbstractDocument {
    private String accountId;
    private String deviceId;
}
