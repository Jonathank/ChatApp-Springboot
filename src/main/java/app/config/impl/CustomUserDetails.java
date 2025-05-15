package app.config.impl;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import app.chat.domain.STATUS;
import app.chat.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final User user;

   

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

 
    @Override
    public boolean isEnabled() {
        boolean enabled = user.getStatus().equals(STATUS.ACTIVE);
        System.out.println("User enabled status: " + enabled + " for email: " + user.getEmail());
        return enabled;
    }

    // Add getters for custom fields
    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }
}
