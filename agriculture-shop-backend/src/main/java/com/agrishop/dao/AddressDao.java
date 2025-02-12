package com.agrishop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.agrishop.model.Address;

@Repository
public interface AddressDao extends JpaRepository<Address, Integer> {

}
