package com.example.swoos.util;



import com.example.swoos.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Component
public class JWTUtils implements Serializable {
    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = (long) 12 * 60 * 60 * 1000;

    public static final long JWT_REFRESH_TOKEN_VALIDITY = (long)13 * 60 * 60 * 1000;

    @Value("${jwt.secret}")
    private String secret;

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getEmail(String token) {
        return extractValueFromToken(token, Constant.EMAIL,secret);
    }
    public String getId(String token) {
        return extractValueFromTokenForUserId(token, Constant.USERID,secret);
    }
    private String extractValueFromTokenForUserId(String token, String id, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().get(id).toString();
    }

    private String extractValueFromToken(String token, String email, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().get(email).toString();
    }

    // retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(User user, HttpSession session) {
        Map<String,Object> claims=new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("mobile", user.getMobileNumber());
        claims.put("id", user.getId());
        claims.put("roll",user.getApplicationRole().getRoleName());
        session.setAttribute("getId", user.getId());
        try {
            return getAccessToken(claims, user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getAccessToken(Map<String, Object> claims, String userName) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userName)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                    .signWith(SignatureAlgorithm.HS256, secret).compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String refreshToken(String token, User user) {
        final Claims claims = extractAllClaims(token);
        claims.setIssuedAt(new Date(System.currentTimeMillis()));
        claims.setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESH_TOKEN_VALIDITY));
        claims.put("email", user.getEmail());
        claims.put("mobile", user.getMobileNumber());
        claims.put("id", user.getId());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public UsernamePasswordAuthenticationToken getAuthentication(final UserDetails userDetails) {

        final List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}

