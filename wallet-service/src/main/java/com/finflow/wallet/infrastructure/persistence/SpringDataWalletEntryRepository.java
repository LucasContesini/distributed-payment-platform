package com.finflow.wallet.infrastructure.persistence;

import com.finflow.wallet.domain.wallet.WalletEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataWalletEntryRepository extends JpaRepository<WalletEntry, UUID> {}
