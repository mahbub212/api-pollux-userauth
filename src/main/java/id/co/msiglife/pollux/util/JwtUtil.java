package id.co.msiglife.pollux.util;
 
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Component;

import id.co.msiglife.pollux.entity.User;
import id.co.msiglife.pollux.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private String secret = StaticValue.SECRET_KEY_LOGIN_TOKEN;
    private String expirationTime = StaticValue.EXP_TIME_LOGIN_TOKEN;
    private Key key = Keys.hmacShaKeyFor(secret.getBytes());;

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            System.out.println("/// Expiration : "+expiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            return true; // Set to true if token is expired
        }
    }

    public String generate(User user, UserRole userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role_id", userRole.getRoleId());
        claims.put("user_id", user.getUserId());
        return doGenerateToken(claims, user.getUsername());
    }
    
    private String doGenerateToken(Map<String, Object> claims, String username) {
        long expirationTimeLong = Long.parseLong(expirationTime);
        final Date createdDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(createdDate.getTime() + expirationTimeLong));
        calendar.set(Calendar.HOUR_OF_DAY, 3); // Set hour to 03
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date expirationDate = calendar.getTime();

        // make expiration date at 03.00 am of the next day on jdk 1.8
        System.out.println("Token will expire at : " + expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }
    
    public Integer getRoleIdFromJwtToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role_id", Integer.class);
    }

}
