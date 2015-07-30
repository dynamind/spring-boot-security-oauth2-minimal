# Client

This is what you need to know. Plus some less pertinent but still interesting bits. 

## Spring Security

We're using the client part of the Spring Security OAuth 2.0 framework. 
Since the framework depends on Spring Security, it is dragged in as a transitive dependency. 
Spring Boot auto-configuration notices this, which then locks down your entire application with a random password.
 
That's not what we want. 

We just want to access an OAuth 2.0 protected resource, and use the login form of the authorization server.
Luckily, there's a simple solution to this. Just set `security.basic.enabled` to `false` in `application.yml`.
This simply tells Spring Boot auto-configuration (specifically `SpringBootWebSecurityConfiguration`) to setup a request matcher
which always returns false - effectively disabling security.

*Note: it's not possible to just exclude the dependency. You're free to try and learn why.* 

I've seen other clever tricks to deal with this, such as this one, where the reply from the resource server is used
to construct an artificial authentication token 
(from [Authenticating with Reddit OAuth2 and Spring Security](http://www.baeldung.com/spring-security-oauth2-authentication-with-reddit)):

    UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(node.get("name").asText(),
                    restTemplate.getAccessToken().getValue(),
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    SecurityContextHolder.getContext().setAuthentication(auth);

## OAuth2RestTemplate

`OAuth2RestTemplate` extends `RestTemplate` and makes it very simple to access OAuth 2.0 protected resources. 
You just provide it with your client credentials, desired scopes, tell it where the authorize and token endpoints are,
and it basically handles the tedious bits of managing access tokens for you. 
 
The various configuration details can be found in `application.yml` and are injected at runtime using the  
`@Value` annotation.

## JsonNode

A nice trick with `RestTemplate` is to use `JsonNode.class` as the response type (when consuming JSON resources). This
is a "catch-all" type that allows you to focus on the plumbing before you get into the modeling of resource types.

## Statefulness

The Cross-Site Request Forgery (CSRF) features op Spring Security require the client to remember the value of a 
`state` parameter. Basically it generates and sends a random string and checks if it is returned by the authorization server.

When you develop on one domain (localhost) like I did with these three applications, you'll find that one application
may overwrite the session cookie of another. This made the client 'forget' about the value of 'state' and caused it
to flag the reply as a possible CSRF attack. I resolved this by assigning the client to the context path `/client`.

So, remember this: context paths are important when dealing with cookies on a shared domain. 


