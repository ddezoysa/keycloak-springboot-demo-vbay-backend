# Securing Spring Boot REST APIs with Keycloak

**Author**: Dinuth De Zoysa, Senior Architect - Technology

## Why we need an IAM tool?

We used to write our own user management code:

- Users, Credentials encryption/hashing, User Roles, User Groups, …
- LDAP / Active Directory / Social Logins integration
- Single Sing-on between multiple applications utilizing same users and groups, Cross-product authorized connections.
- Lot of code maintenance

Benefits of using Identity and Access Management tools:

- **User management**: IAM tools help organizations manage user accounts, authentication, and authorization. It can be challenging to keep track of user access and permissions manually, especially in large applications or organizations.
- **Centralized management**: IAM tools allow organizations and applications to centralize the management of user accounts, access policies, and permissions. This can help reduce administrative overhead and make it easier to manage access control policies across different applications and systems.
- **Compliance requirements**: Many industries have regulations and compliance requirements that mandate strong IAM controls to protect sensitive information. Failing to implement adequate IAM controls can result in costly fines, legal liabilities, and damage to the organization's reputation.
- **Scalability**: As an organization/application grows, managing access to systems and resources can become more complicated. IAM tools can provide scalable solutions to help organizations manage access as they expand.
- **User experience**: Good IAM tools can improve the user experience by providing seamless authentication and authorization processes, reducing the need for users to remember multiple usernames and passwords.

## Keycloak Overview

Keycloak is an open-source identity and access management solution which makes it easy to secure modern applications and services with little to no code.

- Single-Sign On: Login once to multiple applications
- Standard Protocols: OpenID Connect, OAuth 2.0 and SAML 2.0
- LDAP and Active Directory: Connect to existing user directories
- Social Login: Easily enable social login
- Identity Brokering: OpenID Connect or SAML 2.0 IdPs
- Centralized Management: For admins and users
- Adapters: Secure applications and services easily
- High Performance: Lightweight, fast and scalable
- Clustering: For scalability and availability
- Themes: Customize look and feel
- Extensible: Customize through code
- Password Policies: Customize password policies

## Run Keycloak locally

### Download Keycloak:

Download Keycloak Server ZIP from: https://www.keycloak.org/downloads and extract.

```bash
# Navigate to Keycloak bin folder
cd keycloak-21.1.0/bin

# View kc.sh help
./kc.sh --help

# Run Keycloak
./kc.sh start-dev
```

Access Keycloak Admin Console: http://localhost:8080/

## Keycloak Concepts and Terms

### Realm:

A realm is a space where you manage objects, including users, applications, roles, and groups. A user belongs to and logs into a realm. One Keycloak deployment can define, store, and manage as many realms as there is space for in the database.

### Users:

Users are entities that are able to log into your system. They can have attributes associated with themselves like email, username, address, phone number, and birthday. They can be assigned group membership and have specific roles assigned to them.

### Groups:

Groups manage groups of users. Attributes can be defined for a group. You can map roles to a group as well. Users that become members of a group inherit the attributes and role mappings that group defines.

### Clients:

Clients are entities that can request Keycloak to authenticate a user. Most often, clients are applications and services that want to use Keycloak to secure themselves and provide a single sign-on solution. Clients can also be entities that just want to request identity information or an access token so that they can securely invoke other services on the network that are secured by Keycloak.

### Roles:

Roles identify a type or category of user. admin, user, manager, and employee are all typical roles that may exist in an organization. Applications often assign access and permissions to specific roles rather than individual users as dealing with users can be too fine-grained and hard to manage.

### Client Roles:

Clients can define roles that are specific to them. This is basically a role namespace dedicated to the client.

### Composite Roles:

A composite role is a role that can be associated with other roles. For example a superuser composite role could be associated with the sales-admin and order-entry-admin roles. If a user is mapped to the superuser role they also inherit the sales-admin and order-entry-admin roles.

More: https://www.keycloak.org/docs/latest/server_admin/#core-concepts-and-terms

## Keycloak Token Endpoint

Generate User Token:

```bash
curl --location --request POST 'http://localhost:8080/realms/vBay/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=vbay-backend' \
--data-urlencode 'client_secret=xxxxxxxxxxxxxx' \
--data-urlencode 'username=myuser' \
--data-urlencode 'password=mypassword’
```

```json
{
  "access_token": "<JWT>",
  "expires_in": 3600,
  "refresh_expires_in": 3600,
  "refresh_token": "<JWT>",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "xxxxx",
  "scope": "email profile"
}
```

## Keycloak Client Adapters

- Keycloak client adapters are libraries that make it very easy to secure applications and services with Keycloak.
- With less boilerplate code Keycloak can be integrated with many platforms and frameworks.
- Ref: https://www.keycloak.org/docs/latest/securing_apps/

## SpringBoot Keycloak Demo

### App Initialize

Create a SpringBoot App:

- Go to https://start.spring.io/
- Generate SpringBoot app with following configurations
  - Maven, Java, SpringBoot 2.7.11, Jar, Java 17
  - Dependencies: Spring Web, DevTools, Lombok, Actuator
- Open the Generated code in your favourite editor

Run SpringBoot App

- SpringBoot runs in default 8080 which will conflict with Keycloak port
- Change the server port to 8090 by editing the application.properties.
  ```properties
  server.port = 8090
  ```
- Run the SpringBoot app
  ```bash
  mvn spring-boot:run
  ```
- Check http://localhost:8090/actuator

### Create few API endpoints

Create a `controller/TestController.java`.

```java
package com.hive.vbaybackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

  @RequestMapping(value = "/anonymous", method = RequestMethod.GET)
  public ResponseEntity<String> getAnonymous() {
    return ResponseEntity.ok("Hello Anonymous");
  }

  @RequestMapping(value = "/myProducts", method = RequestMethod.GET)
  public ResponseEntity<String[]> getMyProducts() {
  String[] products = { "TV", "Laptop", "Keyboard", "Mouse" };
    return ResponseEntity.ok(products);
  }
}
```

Test Endpoints

- GET http://localhost:8090/test/anonymous
- GET http://localhost:8090/test/myProducts

### Keycloak Integration

1. Maven Dependencies and Dependency Management

   Update pom.xml

   ```xml
   <dependencies>
     <dependency>
       <groupId>org.keycloak</groupId>
       <artifactId>keycloak-spring-boot-starter</artifactId>
     </dependency>
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
     </dependency>
     ...
   </dependencies>

   <dependencyManagement>
     <dependencies>
       <dependency>
         <groupId>org.keycloak.bom</groupId>
         <artifactId>keycloak-adapter-bom</artifactId>
         <version>21.1.0</version>
         <type>pom</type>
         <scope>import</scope>
       </dependency>
     </dependencies>
   </dependencyManagement>
   ```

2. Update application.properties

   ```properties
   keycloak.realm = vBay
   keycloak.auth-server-url = http://localhost:8080/
   keycloak.ssl-required = external
   keycloak.resource = vbay-backend
   keycloak.credentials.secret = xxxxxxxxxxx
   keycloak.use-resource-role-mappings = false
   ```

3. Create `config/UserData.java`

   Create `UserData.java` in `config` package which will be used to map Keycloak token’s claims.

   ```java
   package com.hive.vbaybackend.config;

   import lombok.AllArgsConstructor;
   import lombok.Data;

   @Data
   @AllArgsConstructor
   public class UserData {
     private String username;
     private String name;
   }
   ```

4. Create config/KeycloakSecurityConfig.java

   ```java
   package com.hive.vbaybackend.config;

   import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
   import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
   import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.context.annotation.Bean;
   import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
   import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
   import org.springframework.security.config.annotation.web.builders.HttpSecurity;
   import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
   import org.springframework.security.core.session.SessionRegistry;
   import org.springframework.security.core.session.SessionRegistryImpl;
   import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
   import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

   @KeycloakConfiguration
   @EnableGlobalMethodSecurity(jsr250Enabled = true)
   public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

     // Registers the KeycloakAuthenticationProvider with the authentication manager.
     @Autowired
     public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
       KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
       keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
       auth.authenticationProvider(keycloakAuthenticationProvider);
     }

     // Defines the session authentication strategy.
     @Bean
     @Override
     protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
       return new RegisterSessionAuthenticationStrategy(buildSessionRegistry());
     }

     @Bean
     protected SessionRegistry buildSessionRegistry() {
       return new SessionRegistryImpl();
     }

     @Override
     protected void configure(HttpSecurity http) throws Exception {
       super.configure(http);
       http
         .authorizeRequests()
         .anyRequest().permitAll();
     }
   }
   ```

5. Create config/KeycloakConfiguration.java

   ```java
   package com.hive.vbaybackend.config;

   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.keycloak.adapters.KeycloakConfigResolver;
   import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;

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
   ```

### Update API endpoints with RBAC

Update controller/TestController.java

```java
package com.hive.vbaybackend.controller;

import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.hive.vbaybackend.config.UserData;

@RestController
@RequestMapping("/test")
public class TestController {

  @Autowired
  UserData userData;

  @RequestMapping(value = "/anonymous", method = RequestMethod.GET)
  public ResponseEntity<String> getAnonymous() {
    return ResponseEntity.ok("Hello Anonymous");
  }

  @RequestMapping(value = "/myProducts", method = RequestMethod.GET)
  @RolesAllowed("seller")
  public ResponseEntity<String[]> getMyProducts(@RequestHeader String Authorization) {
    String[] products = { "TV", "Laptop", "Keyboard", "Mouse" };
    return ResponseEntity.ok(products);
  }

  @RequestMapping(value = "/user", method = RequestMethod.GET)
  @RolesAllowed({ "admin", "buyer", "seller" })
  public ResponseEntity<String> getUser(@RequestHeader String Authorization) {
    System.out.println(userData.toString());
    return ResponseEntity.ok("Hello " + userData.getName());
  }

}
```

Test Endpoints

- Try following endpoints with/without Authorization header
  ```
  Authorization: Bearer <Access_Token>
  ```
  - GET http://localhost:8090/test/anonymous
  - GET http://localhost:8090/test/myProducts
  - GET http://localhost:8090/test/user
