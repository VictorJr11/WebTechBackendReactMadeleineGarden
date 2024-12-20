package ReactMadeleine.Garden.implementation;


import ReactMadeleine.Garden.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return authorities associated with the user
        return null; // TODO: implement this
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Adjust as needed
    }
}

