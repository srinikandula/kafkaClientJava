package com.easygaadi.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, String> {

    List<Account> findByRouteConfigEnabled(boolean status);
}
