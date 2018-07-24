package com.easygaadi.dao;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "accounts")
public final class Account extends AbstractDocument {
    private boolean routeConfigEnabled;
    private String userName;

}
