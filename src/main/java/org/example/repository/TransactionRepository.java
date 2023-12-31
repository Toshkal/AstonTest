package org.example.repository;

import org.example.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface TransactionRepository extends JpaRepository<Transactions, UUID> {
    List<Transactions> findAllByAccountFromNumber(String accountNumber);
}
