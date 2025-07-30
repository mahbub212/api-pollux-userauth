package id.co.msiglife.pollux.util;
 
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Component;

import id.co.msiglife.pollux.entity.User;
import id.co.msiglife.pollux.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

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

    public String generate(User user, List<UserRole> userRoles) {
        Map<String, Object> claims = new HashMap<>();
        List<Integer> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        claims.put("role_id", roleIds);
        claims.put("user_id", user.getUserId());
        return doGenerateToken(claims, user.getUsername());
    }
    
    private String doGenerateToken(Map<String, Object> claims, String username) {
        long expirationTimeLong = Long.parseLong(expirationTime);
        final Date createdDate = new Date();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(StaticValue.TIMEZONE));
        calendar.setTimeInMillis(createdDate.getTime() + expirationTimeLong);
        calendar.set(Calendar.HOUR_OF_DAY, 3); // Set hour to 03
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date expirationDate = calendar.getTime();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }
    
    public List<Integer> getRoleIdFromJwtToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        List<Object> rawList = claims.get("role_id", List.class);
        List<Integer> roleIds = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof Number) {
                roleIds.add(((Number) item).intValue());
            }
        }

        return roleIds;
    }

    public String getUsernameFromJwtToken(String token) {
        String username = getAllClaimsFromToken(token).getSubject();
        return username;
    }

}
