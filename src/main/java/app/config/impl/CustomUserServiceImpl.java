/**
 * 
 */
package app.config.impl;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import app.chat.domain.STATUS;
import app.chat.exception.EntityNotFoundException;
import app.chat.model.User;
import app.chat.repositories.UserRepository;
import app.chat.service.UserService;
import lombok.RequiredArgsConstructor;

/**
 *@author JONATHAN 
 */
@RequiredArgsConstructor
@Service
public class CustomUserServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final UserService userService;

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email);
//        if(user != null) {
//            System.out.println("Loaded User: Email=" + user.getEmail() + ", ID=" + user.getId());
//            return new CustomUserDetails(user);
//            
//        }
//        throw  new UsernameNotFoundException("User not found with email: " + email);
//        
//    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = null;
	try {
	    user = userService.getUserByEmail(email);
	} catch (EntityNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus().equals(STATUS.ACTIVE), // isEnabled
                true, // isAccountNonExpired
                true, // isCredentialsNonExpired
                true, // isAccountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()))
        );
    }
}

