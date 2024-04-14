package com.lifepill.possystem.config;

import com.lifepill.possystem.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.lifepill.possystem.entity.enums.Role.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    AuthenticationProvider authenticationProvider;
    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.antMatchers("/lifepill/v1/auth/**").permitAll()
                                .antMatchers("/lifepill/v1/test/**","lifepill/v1/contact/**","lifepill/v1/notices/**").permitAll()
                                //.antMatchers("/lifepill/v1/admin/**").hasAnyRole(OWNER_READ.name(), CASHIER.name())
                                .antMatchers("/lifepill/v1/admin/**").hasRole(OWNER.name())
                                //.antMatchers( "/lifepill/v1/admin/**").permitAll()
                                .antMatchers("/lifepill/v1/cashierNew/**").hasRole(CASHIER.name())
                               // .antMatchers(POST, "/lifepill/v1/cashierNew/**").hasAnyAuthority(CASHIER_CREATE.name(),OWNER_CREATE.name())
                                .antMatchers("lifepill/v1/branch/**").hasAnyRole(OWNER.name())
                                .antMatchers("lifepill/v1/employer/**").hasAnyRole(OWNER.name(), MANAGER.name())
                                .antMatchers("lifepill/v1/cashier/**").hasAnyRole(CASHIER.name(), MANAGER.name(), OWNER.name())
                                .antMatchers("lifepill/v1/owner/**").hasRole(OWNER.name())
                                .antMatchers("lifepill/v1/branch-manager/**").hasAnyRole(MANAGER.name(), OWNER.name())
                                .antMatchers("/lifepill/v1/item-Category/**").hasAnyRole(OWNER.name(), MANAGER.name(), CASHIER.name())
                                .antMatchers("/lifepill/v1/item/**").hasAnyRole(OWNER.name(), MANAGER.name(), CASHIER.name())
                                .antMatchers("/lifepill/v1/order/**").hasAnyRole(OWNER.name(), MANAGER.name(), CASHIER.name())
                                .antMatchers("/lifepill/v1/supplierCompanies/**").hasAnyRole(OWNER.name(), MANAGER.name(), CASHIER.name())
                                .antMatchers("/lifepill/v1/supplier/**").hasAnyRole(OWNER.name(), MANAGER.name(), CASHIER.name())
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
