package com.ankk.ecommerce.securite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConf extends WebSecurityConfigurerAdapter {

    @Autowired
    public UserDetailsServiceImp userDetailsServiceImp;
    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImp).passwordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence charSequence) {
                return charSequence.toString();
            }

            @Override
            public boolean matches(CharSequence charSequence, String s) {
                return true;
            }
        });
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //
        http.cors().and().csrf().disable()
                .authorizeRequests().antMatchers(
                "/authentification","/getmotifs","/suppraccount",
                "/authmobileusermac/**",
                "/getmobileAllProduits","/getmobileallsousproduits","/getarticlesbasedoniddet",
                "/getmobileAllCommunes", "/managecustomer","/sendbooking",
                "/getmobileallsousproduitsbyidprd","/getmobileallsousproduitsarticles",
                "/getmobilealldetailsarticles","/getmobilearticlesBasedonLib",
                "/getmobilepromotedarticles","/getmobilerecentarticles","/lookforuserrequest",
                "/sendmobilecomment","/authenicatemobilecustomer","/getarticledetails",
                "/getmobilehistoricalcommande","/getcustomercommandearticle",
                "/getmobilealldetailsbyidspr","/getmobilearticleinformationbyidart",
                "/lookforwhatuserrequested",
                        "/enregistrerPartenaire",
                "/getarticledetailspanier","/deleteaccountfromphone",
                "/v3/**","/swagger-ui**","/swagger-ui/**"
                ).permitAll().anyRequest().authenticated()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
