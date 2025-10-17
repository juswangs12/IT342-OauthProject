package com.sia.lo.repository;

import com.sia.lo.entity.Provider;
import com.sia.lo.entity.authProvider;
import com.sia.lo.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface authProviderRepository extends JpaRepository<authProvider, Long> {
    Optional<authProvider> findByProviderAndProviderUserId(Provider provider, String providerUserId);
    List<authProvider> findByUser(user user);
}

