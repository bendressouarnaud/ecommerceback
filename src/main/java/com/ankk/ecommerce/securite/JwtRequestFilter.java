package com.ankk.ecommerce.securite;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    UserDetailsServiceImp userDetailsServiceImp;
    @Autowired
    JwtUtil jwtUtil;
    String[] mobileApi = {"getmobcommercant", "getmobdetailtable", "getmoblouer",
            "getmobmagasin", "getmobnomenclature", "getmobnomenclature", "getmobparametrage",
            "getmobpayer", "getmobperiode", "getmobsituationzone", "getmobsuperficie",
            "getmobunitetaxe", "sendfcmtoken", "requestforprojection","sendPaiementCinetpay",
            "getappusers","getshoppayement","sendClientJournalier", "getclientsjournaliers",
            "checkDailyClientPayment", "enregistrerMobMagasin", "sendmagasingpsdata",
            "getusermairie"};

    //


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        try {

            if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                jwt = authorizationHeader.substring(7);
                username = jwtUtil.getUsernameFromToken(jwt);
                //
                //System.out.println("Username : "+username);
                //System.out.println("jwt : "+jwt);
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsServiceImp.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null,
                                    userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(
                            usernamePasswordAuthenticationToken);
                }

                //
            }
        }
        catch(ExpiredJwtException ex){
            String isRefreshToken = request.getHeader("isRefreshToken");
            String requestURL = request.getRequestURL().toString();
            //System.out.println("exception error : "+ex.toString());
            // Check if URI is allowed :
            boolean ulrExist = false;
            for(String url : mobileApi){
                if(requestURL.contains(url)) {
                    ulrExist = true;
                    break;
                }
            }

            // allow for Refresh Token creation if following conditions are true.
            if (isRefreshToken != null && isRefreshToken.equals("true") && ulrExist) {
                allowForRefreshToken(ex, request, jwt);
            }
            //else System.out.println("NOK  ");
            /*else request.setAttribute("exception", ex);*/
        }
        filterChain.doFilter(request, response);
    }


    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest request, String jwt) {

        // create a UsernamePasswordAuthenticationToken with null values.
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                null, null, null);
        // After setting the Authentication in the context, we specify
        // that the current user is authenticated. So it passes the
        // Spring Security Configurations successfully.
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        // Set the claims so that in controller we will be using it to create
        // new JWT
        request.setAttribute("claims", ex.getClaims());
        request.setAttribute("jwt", jwt);
    }
}