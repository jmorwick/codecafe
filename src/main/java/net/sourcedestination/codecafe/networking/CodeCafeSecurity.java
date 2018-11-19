package net.sourcedestination.codecafe.networking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class CodeCafeSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/css/**", "/index").permitAll()
                .antMatchers("/exercises/**").hasRole("USER")
                .antMatchers("/chapters/**").hasRole("USER")
                .antMatchers("/user/**").hasRole("USER")
                .and()
                .csrf().disable()
                .formLogin();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder pe = new BCryptPasswordEncoder();
        auth.inMemoryAuthentication().passwordEncoder(pe)
                .withUser("user").password(pe.encode("password")).roles("USER");
    }
}