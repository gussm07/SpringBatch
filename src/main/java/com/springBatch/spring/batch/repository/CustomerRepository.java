package com.springBatch.spring.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springBatch.spring.batch.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer>{

}
