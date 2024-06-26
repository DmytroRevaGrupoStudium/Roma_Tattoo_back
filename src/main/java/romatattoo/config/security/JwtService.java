package romatattoo.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import romatattoo.entities.UserTienda;
import romatattoo.repositories.UserTiendaRepository;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService implements UserDetailsService {

    private static final String SECRET_KEY = "a2fe162bb7a7b0e56d413cc696a64f243d3e55ad4ba65608ef77ddd73e03ba44";

    private final UserTiendaRepository userTiendaRepository;

    @Autowired
    public JwtService(UserTiendaRepository userTiendaRepository) {
        this.userTiendaRepository = userTiendaRepository;
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Obtener el rol del usuario
        String userRole = getUserRoleFromUserDetails(userDetails);

        // Agregar el rol del usuario como una afirmación adicional
        extraClaims.put("rol", userRole);

        // Construir el token JWT con las afirmaciones adicionales
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String getUserRoleFromUserDetails(UserDetails userDetails) {
        // Obtener los roles del usuario desde userDetails
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Verificar si el usuario tiene el rol de ADMIN
        if (authorities.contains(new SimpleGrantedAuthority(Role.ADMIN.name()))) {
            return Role.ADMIN.name();
        } else {
            return Role.USER.name();
        }
    }

    public String getUserName(String token){
        return getClaim(token, Claims::getSubject);
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims=getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[]KeyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(KeyBytes);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String modifyTokenExpiration(String token, long expirationTimeInMillis) {
        // Parsear el token utilizando JwtParserBuilder
        Jws<Claims> parsedToken = Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token);

        // Reconstruir el token con la nueva fecha de expiración
        return Jwts.builder()
                .setClaims(parsedToken.getBody())
                .setExpiration(new Date(expirationTimeInMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserTienda userTienda = userTiendaRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return userTienda;
    }

}
