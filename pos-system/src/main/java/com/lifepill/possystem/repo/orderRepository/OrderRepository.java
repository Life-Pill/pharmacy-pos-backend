package com.lifepill.possystem.repo.orderRepository;

import com.lifepill.possystem.entity.Branch;
import com.lifepill.possystem.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@EnableJpaRepositories
public interface OrderRepository extends JpaRepository<Order,Long> {
    Collection<Object> findByBranchId(Branch branch);

}