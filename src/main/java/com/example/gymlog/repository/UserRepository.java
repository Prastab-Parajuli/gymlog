package com.example.gymlog.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.example.gymlog.model.AppUser;

@RepositoryRestResource(exported = false)
/** Database access for users. Not exposed in the REST API, so password hashes stay private. */
public interface UserRepository extends CrudRepository<AppUser, Long> {

    AppUser findByUsername(String username);
}