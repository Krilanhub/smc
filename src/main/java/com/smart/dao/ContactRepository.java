package com.smart.dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	// pagination....

	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
	
	@Query("from Contact as c where c.user.id =:userId and (c.name=:dataFilter or  c.email=:dataFilter or c.phone=:dataFilter)")
	public Page<Contact> findContactsByUserWithParams(@Param("userId")int userId, String dataFilter, Pageable pageable);

}