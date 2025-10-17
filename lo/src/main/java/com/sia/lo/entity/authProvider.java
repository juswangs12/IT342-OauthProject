package com.sia.lo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_providers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerUserId"})
})
public class authProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name = "user_id")
    private user user;

    @Enumerated(EnumType.STRING)
    private Provider provider; // GOOGLE or GITHUB

    private String providerUserId;
    private String providerEmail;

    // getters/setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public user getUser() {
        return user;
    }

    public void setUser(user user) {
        this.user = user;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }
}

