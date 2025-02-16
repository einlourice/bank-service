package com.rabobank.bankservice.repository;

import com.rabobank.bankservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<List<Transaction>> findAllByAccountId(Long accountId);
}
