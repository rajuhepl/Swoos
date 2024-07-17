package com.example.swoos.configure;

import com.example.swoos.dto.DecodeTokenDTO;
import com.example.swoos.dto.MasterRoleDTO;
import com.example.swoos.dto.UserDTO;
import com.example.swoos.model.MasterRole;
import com.example.swoos.model.User;
import com.example.swoos.repository.MasterRoleRepository;
import com.example.swoos.repository.UserRepository;
import com.example.swoos.service.Authservice;
import com.example.swoos.util.Constant;
import com.example.swoos.util.JWTUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    JWTUtils jwtTokenUtil;

    @Value("${jwt.secret}")
    private String pk;

    @Autowired
    Authservice authservice;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    MasterRoleRepository masterRoleRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (shouldSkipFilter(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwtToken = extractJwtToken(request.getHeader("Authorization"));
            if (jwtToken != null) {
                processJwtToken(jwtToken, request);
            } else {
                throw new ServletException("JWT Token does not begin with Bearer String");
            }

            filterChain.doFilter(request, response);
        } catch (ServletException servletException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setHeader("Access-Control-Allow-Origin", "*"); // Set the appropriate origin or allow all for testing
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("statusCode", response.getStatus());
            errorResponse.put("statusMessage", servletException.getMessage());
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));

        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setHeader("Access-Control-Allow-Origin", "*"); // Set the appropriate origin or allow all for testing
            Map<String, Object> exceptionResponse = new HashMap<>();
            exceptionResponse.put("statusCode", response.getStatus());
            exceptionResponse.put("statusMessage", exception.getMessage());
            response.getWriter().write(new ObjectMapper().writeValueAsString(exceptionResponse));
        }

    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        return checker(request) || StringUtils.isBlank(request.getHeader(Constant.AUTHORIZATION));
    }

    private String extractJwtToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }

    private void processJwtToken(String jwtToken, HttpServletRequest request) throws ServletException {
        try {
            String username = getUsernameFromJwtToken(jwtToken);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = authservice.getUser(username);
                if (user != null) {
                    UserDetails userDetails = createUserDetails(username, user.getPassword());
                    if (Boolean.TRUE.equals(jwtTokenUtil.validateToken(jwtToken, userDetails))) {
                        authenticateWithJwtToken(userDetails, request);
                    } else {
                        throw new ServletException("Access Token is invalid");
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("Access Token has expired: " + e.getMessage());
            throw new ServletException("Access Token has expired");
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private String getUsernameFromJwtToken(String jwtToken) throws JsonProcessingException {
        DecodeTokenDTO dto = extractTokenDto(jwtToken);
        if (dto.getSub() != null) {
            Optional<User> user = userRepository.findByEmail(dto.getSub());
            user.ifPresent(u -> {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(u.getId());
                userDTO.setUsername(u.getUsername());
                userDTO.setFirstName(u.getFirstName());
                userDTO.setLastName(u.getLastName());
                userDTO.setEmail(u.getEmail());
                userDTO.setDob(u.getDob());
                userDTO.setCreatedAt(String.valueOf(u.getCreatedAt()));
                userDTO.setUpdatedAt(String.valueOf(u.getUpdatedAt()));
                Optional<MasterRole> masterRole = masterRoleRepository.findById(u.getApplicationRole().getId());
                masterRole.ifPresent(mr ->{
                    MasterRoleDTO masterRoleDTO = modelMapper.map(mr,MasterRoleDTO.class);
                    userDTO.setApplicationRole(masterRoleDTO);
                    UserContextHolder.setUserDto(userDTO);
                });
            });
            return dto.getSub();
        }
        return null;
    }

    private DecodeTokenDTO extractTokenDto(String jwtToken) throws JsonProcessingException {
        String[] split = jwtToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(split[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return objectMapper.readValue(payload, DecodeTokenDTO.class);
    }

    private UserDetails createUserDetails(String username, String password) {
        return new org.springframework.security.core.userdetails.User(username, password, new ArrayList<>());
    }

    private void authenticateWithJwtToken(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = jwtTokenUtil.getAuthentication(userDetails);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private boolean checker(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/actuator/health")) {
            return true;
        }
        if (request.getRequestURI().equalsIgnoreCase("/v3/api-docs/**")) {
            return true;
        }
        if (request.getRequestURI().equalsIgnoreCase("/user/save")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/v3/api-docs")) {
            return true;
        }
        if (request.getRequestURI().equalsIgnoreCase("/v3/api-docs/swagger-config")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/configuration")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/index.html")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/swagger-ui.css")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/index.css")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/swagger-initializer.js")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/swagger-ui-bundle.js")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/swagger-ui-standalone-preset.js")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/favicon-32x32.png")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/swagger-ui/favicon-16x16.png")) {
            return true;
        }
        if (request.getRequestURI().startsWith(Constant.USER_SIGNUP)) {
            return true;
        }
        if (request.getRequestURI().startsWith("/favicon.ico")) {
            return true;
        }
        if (request.getRequestURI().startsWith(Constant.LOGIN)) {
            return true;
        }
        if (request.getRequestURI().startsWith(Constant.REFRESH)) {
            return true;
        }
        if (request.getRequestURI().startsWith("/getHi")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/save")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/get")) {
            return true;
        }
        if (request.getRequestURI().startsWith("/create")) {
            return true;
        }
        return request.getRequestURI().startsWith("/update");
    }

}

