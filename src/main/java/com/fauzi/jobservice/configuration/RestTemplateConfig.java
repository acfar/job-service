package com.fauzi.jobservice.configuration;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    @Bean
    public SSLContext sslContext()
            throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException { // config ssl context to allow ssl connection
        return new SSLContextBuilder()
                .loadTrustMaterial(null, (arg0, arg1) -> true)
                .build();
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager(
            final SSLContext sslContext) {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
                new PoolingHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                                .register(
                                        "http",
                                        PlainConnectionSocketFactory
                                                .INSTANCE) // register plain connection socket for http
                                .register( // allow all ssl connection for https
                                        "https",
                                        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                                .build());

        poolingHttpClientConnectionManager.setDefaultSocketConfig(
                SocketConfig.custom().setSoTimeout(10000).build());
        poolingHttpClientConnectionManager.setMaxTotal(100);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(20);
        poolingHttpClientConnectionManager.setValidateAfterInactivity(
                5000);
        poolingHttpClientConnectionManager.closeIdleConnections(0, TimeUnit.SECONDS);
        poolingHttpClientConnectionManager.closeExpiredConnections();
        return poolingHttpClientConnectionManager;
    }

    @Bean
    public CloseableHttpClient httpClient(
            final SSLContext sslContext,
            final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        return HttpClients.custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultSocketConfig(poolingHttpClientConnectionManager.getDefaultSocketConfig())
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(10000)
                                .setConnectionRequestTimeout(10000)
                                .setSocketTimeout(10000)
                                .build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(sslContext)
                .setSSLSocketFactory(
                        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .setRetryHandler(
                        (exception, executionCount, context) -> {
                            if (executionCount > 3) {
                                return false;
                            }
                            return exception instanceof NoHttpResponseException;
                        })
                .build();
    }

    @Bean
    public RestTemplate restTemplate(final CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setHttpClient(httpClient);
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(10000);
        httpComponentsClientHttpRequestFactory.setConnectTimeout(10000);
        httpComponentsClientHttpRequestFactory.setReadTimeout(10000);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(
                new BufferingClientHttpRequestFactory(httpComponentsClientHttpRequestFactory));
        restTemplate.setErrorHandler(new ErrorHandler());
        return restTemplate;
    }

    private static class ErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(@NonNull ClientHttpResponse clientHttpResponse) {
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
            // conversion logic for decoding conversion
            ByteArrayInputStream arrayInputStream = (ByteArrayInputStream) clientHttpResponse.getBody();
            Scanner scanner = new Scanner(arrayInputStream, StandardCharsets.UTF_8);
            scanner.useDelimiter("\\Z");
        }
    }
}
