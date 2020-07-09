package com.incountry.residence.sdk.tools.http.impl;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractHttpRequestCreator {
    private static final Logger LOG = LogManager.getLogger(AbstractHttpRequestCreator.class);

    private static final String MSG_ERR_URL = "URL error";
    private static final String MSG_ERR_SERVER_REQUES = "Server request error: %s";
    private static final String MSG_ERR_NULL_BODY = "Body can't be null";

    private static final String POST = "POST";
    private static final String GET = "GET";

    protected HttpRequestBase createRequest(String url, String method, String body) throws UnsupportedEncodingException, StorageServerException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ex) {
            throw new StorageServerException(MSG_ERR_URL, ex);
        }

        if (method.equals(POST)) {
            if (body == null) {
                LOG.error(MSG_ERR_NULL_BODY);
                throw new StorageServerException(String.format(MSG_ERR_SERVER_REQUES, method), new StorageClientException(MSG_ERR_NULL_BODY));
            }
            HttpPost request = new HttpPost(uri);
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
            return request;
        } else if (method.equals(GET)) {
            return new HttpGet(uri);
        } else {
            return new HttpDelete(uri);
        }
    }
}
