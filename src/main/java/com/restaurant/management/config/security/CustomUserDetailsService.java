package com.restaurant.management.config.security;

import com.restaurant.management.config.security.prevent_fuzz.LoginAttemptService;
import com.restaurant.management.model.Customer;
import com.restaurant.management.model.Employee;
import com.restaurant.management.service.CustomerService;
import com.restaurant.management.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException, LockedException {

        if (loginAttemptService.isIpBlocked()) {
            String remainingLockTimeMessage = loginAttemptService.getFormattedIpLockoutDurationRemaining();

            throw new LockedException("Access from your IP address has been temporarily blocked due to too many failed login attempts. Please try again in " + remainingLockTimeMessage + ".");
        }

        System.out.println("Attempting to load user by email (login):::" + email + " (IP not currently blocked)");

        Employee employee = employeeService.getEmployeeByEmail(email);
        if (employee != null) {
            return User.builder()
                    .username(employee.getEmail())
                    .password(employee.getPassword())
                    .roles(employee.getPosition())
                    .build();
        }

        Optional<Customer> customerOptional = customerService.getCustomerByEmail(email);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            return User.builder()
                    .username(customer.getEmail())
                    .password(customer.getPassword())
                    .roles("CUSTOMER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}