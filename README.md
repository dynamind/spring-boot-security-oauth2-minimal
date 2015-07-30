# OAuth2 with JWT

## Introduction
I've created this project to understand how to deal with clients accessing protected resource servers using a central authorization server. Apart from general OAuth 2.0 and JWT theory, I wanted to study how the following components interoperate:

- Spring Boot 1.3.0.M2 (with Spring 4.2.0.RC2)
- Spring Security 4.0.1.RELEASE
- Spring Security OAuth2 2.0.7.RELEASE
- Spring Security JWT 1.0.3.RELEASE

Spring Boot can be pretty magical, especially with regard to *auto-configuration*. While I'm a fan of *convention-over-configuration* development (I've used Grails for years), that doesn't mean it always makes sense. At least not at first, to newcomers like me.

I found that some of the documentation and examples tend to gloss over "minor details" which makes it hard to grasp why you need to do something, or when the framework takes care of it for you. It took me quite a few hours of trial-and-error, tracing execution paths, reading auto-configuration classes, failing with out-of-date examples, etc.

**References:**

- [https://github.com/spring-projects/spring-security-oauth/tree/master/tests/annotation]()
- [http://projects.spring.io/spring-security-oauth/docs/oauth2.html]()


## Running the example

You can import each application in IntelliJ IDEA and run them from there using the Gradle `bootRun` task. Alternatively, you can open three terminal sessions (or command prompts) and run `gradle bootRun` from each folder. 

*Note: You need to start the authorization server first, because the resource server contacts it during startup to obtain the public key used to verify JWT signatures.*

Open a browser to [http://localhost:8080/client/](). The client uses a `RestTemplate` to access a protected resource (running on [http://localhost:8082/api/]()) and discovers it is not authorized. It then redirects automatically to the [`/oauth/authorize`](http://localhost:8081/oauth/authorize) endpoint to start an *authorization code* flow. In the process it authenticates itself as a 'confidential' client. 

The authorization server redirects to its login page. You can use one of the following username/password combinations to login:

- `user:password` (has USER role)
- `admin:password` (has ADMIN and USER role)

After a successful login you are redirected to the [`/oauth/confirm_access`](http://localhost:8081/oauth/confirm_access) endpoint where you need to approve all the grants requested by the client.

The authorization server now redirects back to the client application on a pre-approved URI, with an *authorization code*. 

The client then accesses the authorization server on the [`/oauth/token`](http://localhost:8081/oauth/token) endpoint to exchange the access code with an access token. 

*This token is actually a JSON Web Token (JWT). If you want to see what's in it, visit [jwt.io](http://jwt.io/) and paste it in the Encoded section. You can find the token in the JSON outputted by the client as `details.tokenValue`.*

The client now retries the request to the resource server. The resource server accepts the JWT token and checks the signature using the authorization server's public key. There's no communication necessary between the resource server and the authorization server; that is one of the nice things about JWT. The JWT token also describes the user's roles, which are checked against the authorization requirements of the resource.

The client receives the resource (a JSON representation of the user principal) and dumps it to the browser (where you can also see the JSON Web Token).
