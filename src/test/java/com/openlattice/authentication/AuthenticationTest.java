package com.openlattice.authentication;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.auth.UserInfo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.net.AuthRequest;
import com.auth0.spring.security.api.JwtAuthenticationProvider;
import com.auth0.spring.security.api.authentication.PreAuthenticatedAuthenticationJsonWebToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.RateLimiter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * This class is a sanity check to ensure that authentication is successfully working against auth0 server. The hard
 * coded credentials and secrets are only usable for testing.
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class AuthenticationTest {
    private static final Logger                           logger              = LoggerFactory
            .getLogger( AuthenticationTest.class );
    private static final String                           domain              = "openlattice.auth0.com";
    private static final String                           managementApiUrl    = "https://openlattice.auth0.com/api/v2/";
    private static final String                           issuer              = "https://openlattice.auth0.com/";
    private static final String                           audience            = "https://tests.openlattice.com";
    private static final String                           clientId            = "KTzgyxs6KBcJHB872eSMe2cpTHzhxS99";
    private static final String                           clientSecret        = "MK4CrccqOqI6WVkOiSJX6n2h3MLgGri0";
    private static final String                           signingAlgorithm    = "HS256";
    private static final boolean                          base64EncodedSecret = true;
    public static final  Auth0AuthenticationConfiguration authConfiguration   = new Auth0AuthenticationConfiguration(
            issuer, audience, clientSecret, Optional.of( base64EncodedSecret ), signingAlgorithm
    );
    public static final  Auth0Configuration               configuration       = new Auth0Configuration(
            domain,
            clientId,
            clientSecret,
            ImmutableSet.of( authConfiguration ),
            Optional.empty(),
            managementApiUrl );
    private static final LoadingCache<AuthenticationTestRequestOptions, Authentication> authentications;
    private static final LoadingCache<AuthenticationTestRequestOptions, TokenHolder>    accessTokens;
    private static final AuthenticationTestRequestOptions authOptions = new AuthenticationTestRequestOptions()
            .setUsernameOrEmail( "tests@openlattice.com" )
            .setPassword( "openlattice" );
    private static final JWTVerifier verifier;
    private static final AuthAPI     client          = new AuthAPI( domain, clientId, "" );
    private static final RateLimiter authRateLimiter = RateLimiter.create( 0.25 );

    static {
        try {
            verifier = forHS256( clientSecret, issuer, audience );
        } catch ( UnsupportedEncodingException e ) {
            throw new IllegalStateException( e );
        }
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider( clientSecret.getBytes( Charset.defaultCharset() ), issuer, audience );
        accessTokens = CacheBuilder.newBuilder()
                .build( new CacheLoader<AuthenticationTestRequestOptions, TokenHolder>() {
                    @Override public TokenHolder load( AuthenticationTestRequestOptions options ) throws Exception {
                        authRateLimiter.acquire();
                        AuthRequest authRequest = client
                                .login( options.getUsernameOrEmail(), options.getPassword(), options.getConnection() )
                                .setScope( options.getScope() )
                                .setAudience( "https://openlattice.auth0.com/userinfo" );
                        TokenHolder th = authRequest.execute();
                        String accessToken = th.getAccessToken();
                        String idToken = th.getIdToken();

                        logger.info( "Caching the following id token: {}", idToken );
                        logger.info( "Caching the following user info access token: {}", accessToken );
                        return th;
                    }
                } );

        authentications = CacheBuilder.newBuilder()
                .build( new CacheLoader<AuthenticationTestRequestOptions, Authentication>() {
                    @Override public Authentication load( AuthenticationTestRequestOptions options ) throws Exception {
                        AuthRequest authRequest = client
                                .login( options.getUsernameOrEmail(), options.getPassword(), options.getConnection() )
                                .setScope( options.getScope() )
                                .setAudience( audience );
                        authRateLimiter.acquire();
                        TokenHolder th = authRequest.execute();

                        String accessToken = th.getAccessToken();
                        logger.info( "Caching full access token: {}", accessToken );

                        Authentication auth = provider
                                .authenticate( PreAuthenticatedAuthenticationJsonWebToken.usingToken( accessToken ) );

                        logger.info( "Caching the following idToken: Bearer {}", auth.getCredentials() );
                        return auth;
                    }
                } );
    }

    @Test
    public void testLoadUserInfo() throws Auth0Exception {
        String accessToken = accessTokens();
        UserInfo userInfoRequest = client
                .userInfo( accessToken )
                .execute();
        Map<String, Object> d2 = userInfoRequest.getValues();
        Assert.assertTrue( d2.containsKey( "email" ) );
        Assert.assertTrue( d2.containsKey( "email_verified" ) );
        Assert.assertTrue( d2.containsKey( "nickname" ) );
        Assert.assertTrue( d2.containsKey( "picture" ) );
        Assert.assertTrue( d2.containsKey( "name" ) );
        Assert.assertTrue( d2.containsKey( "sub" ) );
        Assert.assertTrue( d2.containsKey( "updated_at" ) );
        logger.info( "User Info: {}", d2 );
    }

    public static Authentication getAuthentication( AuthenticationTestRequestOptions options ) {
        return authentications.getUnchecked( options );
    }

    public static Authentication authenticate() {
        return authentications.getUnchecked( authOptions );
    }

    public static TokenHolder tokenHolder( AuthenticationTestRequestOptions options ) {
        return accessTokens.getUnchecked( options );
    }

    public static TokenHolder tokenHolder() {
        return accessTokens.getUnchecked( authOptions );
    }

    public static String accessTokens() {
        return accessTokens.getUnchecked( authOptions ).getAccessToken();
    }

    public static Authentication refreshAndGetAuthentication( AuthenticationTestRequestOptions options ) {
        authentications.invalidate( options );
        return authentications.getUnchecked( options );
    }

    private static JWTVerifier forHS256( String secret, String issuer, String audience )
            throws UnsupportedEncodingException {
        return JWT.require( Algorithm.HMAC256( secret ) )
                .withIssuer( issuer )
                .withAudience( audience )
                .build();
    }

}