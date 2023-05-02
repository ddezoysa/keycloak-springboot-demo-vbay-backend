package com.hive.vbaybackend.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class KeycloakConfiguration {

  @Bean
  public KeycloakConfigResolver KeycloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  @Bean
  public UserData userDetails() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    KeycloakPrincipal principal = (KeycloakPrincipal) auth.getPrincipal();
    KeycloakSecurityContext session = principal.getKeycloakSecurityContext();
    AccessToken accessToken = session.getToken();
    String username = accessToken.getPreferredUsername();
    String name = accessToken.getName();
    return new UserData(username, name);
  }
}
