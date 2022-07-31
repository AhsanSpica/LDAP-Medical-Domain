package com.test.springldaptest.springldaptest;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
              // .antMatchers("/managers").hasRole("MANAGERS") for spring security role
              //  .antMatchers("/employees").hasRole("EMPLOYEES")
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin()
                //.loginPage("/newlogin")
                //.permitAll() //particularly with antmatchers ("authenticate") special jwt method
                .and().exceptionHandling().accessDeniedPage("/accessDenied")
//                .and()
//                .logout()
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        ;
//          .authorizeRequests()
//                .antMatchers("/yourstuff/**").permitAll()
//                .antMatchers("/your/protectedstuff/**").authenticated()
//                .and()
//                .httpBasic()
//                .permitAll();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
               .userDnPatterns("uid={0},ou=people")
               // .userDnPatterns("")
                .groupSearchBase("ou=groups")
                .groupSearchFilter("uniqueMember={0}")
               // .groupSearchSubtree(true)
                .contextSource()
                .url("ldap://localhost:8389/dc=springframework,dc=org")
                //.url("ldap://192.168.10.21:636/dc=springframework,dc=org")
                .and()
                .passwordCompare()
                .passwordEncoder(new BCryptPasswordEncoder())  //depends on how ldif is defined
                 .passwordAttribute("userPassword")
             ;


//        auth.authenticationProvider(new ActiveDirectoryLdapAuthenticationProvider("DOMAINNAME","LDAP SERVER URI"));
    }
}
