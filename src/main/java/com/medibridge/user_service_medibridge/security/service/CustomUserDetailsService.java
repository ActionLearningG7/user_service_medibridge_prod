package com.medibridge.user_service_medibridge.security.service;

import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsernameAndNotDeleted(emailOrUsername)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email or username: " + emailOrUsername));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use email as principal
                user.getPasswordHash(),
                user.getRole() != null
                        ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        : Collections.emptyList());
    }
}
