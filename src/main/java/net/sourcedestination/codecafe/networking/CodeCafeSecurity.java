package net.sourcedestination.codecafe.networking;

import net.sourcedestination.codecafe.structure.ProfileController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.logging.Logger;

@EnableWebSecurity
public class CodeCafeSecurity extends WebSecurityConfigurerAdapter {
    private final Logger logger = Logger.getLogger(WebSecurityConfigurerAdapter.class.getCanonicalName());

    @Value("${ldap.url}")
    private String ldapURL;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    @Value("${ldap.user.dn}")
    private String ldapUserDn;

    @Value("${authentication}")
    private String authentication;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/css/**", "/index", "/test").permitAll()
                .antMatchers("/exercises/**").authenticated()
                .antMatchers("/chapters/**").authenticated()
                .antMatchers("/user/**").authenticated()
                .and()
       //         .csrf().disable()
                .formLogin();
    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        if(authentication.equals("ldap")) {
            logger.info("Setting up LDAP authentication");
            auth
                    .ldapAuthentication()
                    .contextSource()
                    .url(ldapURL + "/" + ldapBaseDn)
                    .and()
                    .userDnPatterns(ldapUserDn);
        } else { // testing logins
            logger.info("Setting up test authentication");
            PasswordEncoder pe = new BCryptPasswordEncoder();
            auth.inMemoryAuthentication().passwordEncoder(pe)
                    .withUser("user").password(pe.encode("password")).roles("USER");
        }
    }
}
