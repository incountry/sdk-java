package com.incountry.residence.sdk.oauth;

/**
 * OAuth2 token accessor. Method {@link OauthTokenAccessor#getToken()} is invoked on HTTP interaction with InCountry storage service.
 */
@FunctionalInterface
public interface OauthTokenAccessor {

    /**
     * Getting a token for HTTP interactions with InCountry storage service.
     *
     * @return token as a String
     */
    String getToken();
}
