package com.agrishop.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.agrishop.model.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {
	

}
