package com.easygaadi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;


@EqualsAndHashCode(of = { "_id" })
@Getter
@Setter
public abstract class AbstractDocument {

    public static final String KEY_ID = "_id";

    public static final String KEY_ATTRIBUTES = "attrs";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_UPDATED_AT = "updatedAt";

    @Id
    @Field(KEY_ID)
    private String id;

    @CreatedDate
    @Field(KEY_CREATED_AT)
    private Date createdAt;

    @Version
    private Long version;

    @LastModifiedDate
    @Field(KEY_UPDATED_AT)
    private Date updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Field(KEY_ATTRIBUTES)
    private Map<String, Object> attrs = new HashMap<>();

    public void merge(final Object copy) throws Exception {
        BeanUtils.copyProperties(copy, this, getNullPropertyNames(copy));
    }

    public void merge(final Object copy, boolean ignoreNullValues) throws Exception {
        if(ignoreNullValues) {
            BeanUtils.copyProperties(copy, this, getNullPropertyNames(copy));
        } else {
            BeanUtils.copyProperties(copy, this);
        }
    }

    private String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


}
