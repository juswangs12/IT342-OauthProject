package com.sia.lo.service;

import com.sia.lo.entity.authProvider;
import com.sia.lo.entity.user;
import com.sia.lo.repository.*;
import com.sia.lo.entity.Provider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class customOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final userRepository userRepo;
    private final authProviderRepository authRepo;

    public customOAuth2UserService(userRepository userRepo, authProviderRepository authRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId().toLowerCase(); // "google" or "github"
        Provider provider = registrationId.equals("google") ? Provider.GOOGLE : Provider.GITHUB;

        Map<String, Object> attrs = oauthUser.getAttributes();
        String providerUserId = getProviderUserId(registrationId, attrs);
        String email = getEmail(registrationId, attrs);
        String name = getName(registrationId, attrs);
        String avatar = getAvatarUrl(registrationId, attrs);

        if (providerUserId == null || email == null) throw new OAuth2AuthenticationException("Missing required user info");

        // find AuthProvider
        Optional<authProvider> opt = authRepo.findByProviderAndProviderUserId(provider, providerUserId);
        user user;
        if (opt.isPresent()) {
            user = opt.get().getUser();
            // update profile fields if changed
            boolean changed = false;
            if (!Objects.equals(user.getDisplayName(), name)) { user.setDisplayName(name); changed = true; }
            if (!Objects.equals(user.getAvatarUrl(), avatar)) { user.setAvatarUrl(avatar); changed = true; }
            if (changed) userRepo.save(user);
        } else {
            // first time login: create user + auth provider
            user = new user();
            user.setEmail(email);
            user.setDisplayName(name);
            user.setAvatarUrl(avatar);
            user.setCreatedAt(Instant.now());
            user = userRepo.save(user);

            authProvider ap = new authProvider();
            ap.setUser(user);
            ap.setProvider(provider);
            ap.setProviderUserId(providerUserId);
            ap.setProviderEmail(email);
            authRepo.save(ap);
        }

        // return a DefaultOAuth2User with ROLE_USER
        Map<String, Object> principalAttrs = new HashMap<>();
        principalAttrs.put("id", user.getId());
        principalAttrs.put("email", user.getEmail());
        principalAttrs.putAll(attrs);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                principalAttrs, "email");
    }

    // helper methods to extract id/email/name/avatar for Google and GitHub
    private String getProviderUserId(String reg, Map<String,Object> attrs) {
        if (reg.equals("google")) return (String) attrs.get("sub");
        if (reg.equals("github")) {
            Object id = attrs.get("id");
            return id == null ? null : String.valueOf(id);
        }
        return null;
    }
    private String getEmail(String reg, Map<String,Object> attrs) {
        if (reg.equals("google")) return (String) attrs.get("email");
        if (reg.equals("github")) {
            // GitHub may not return email in primary attributes (it might be null)
            return (String) attrs.get("email");
        }
        return null;
    }
    private String getName(String reg, Map<String,Object> attrs) {
        if (reg.equals("google")) return (String) attrs.get("name");
        if (reg.equals("github")) {
            String name = (String) attrs.get("name");
            if (name == null) return (String) attrs.get("login");
            return name;
        }
        return null;
    }
    private String getAvatarUrl(String reg, Map<String,Object> attrs) {
        if (reg.equals("google")) return (String) attrs.get("picture");
        if (reg.equals("github")) return (String) attrs.get("avatar_url");
        return null;
    }

}

