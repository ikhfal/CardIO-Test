package com.idemia.tec.jkt.cardiotest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markusbernhardt.proxy.ProxySearch;
import com.idemia.tec.jkt.cardiotest.response.RandomQuoteResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.List;

@Service
public class RandomQuoteService {

    Logger logger = Logger.getLogger(RandomQuoteService.class);

    public RandomQuoteResponse getRandomQuote() throws IOException {
        ProxySearch proxySearch = new ProxySearch();
        proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
        proxySearch.addStrategy(ProxySearch.Strategy.JAVA);
        proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
        ProxySelector proxySelector = proxySearch.getProxySelector();

        ProxySelector.setDefault(proxySelector);
        URI home = URI.create("google.com"); // to detect which proxy and port is currently being used
//        logger.info("ProxySelector: " + proxySelector);
//        logger.info("URI: " + home);
        List<Proxy> proxyList = proxySelector.select(home);
        if (proxyList != null && !proxyList.isEmpty()) {
            for (Proxy proxy : proxyList) {
//                logger.info(proxy);
                SocketAddress address = proxy.address();
                if (address instanceof InetSocketAddress) {
                    String host = ((InetSocketAddress) address).getHostName();
                    String port = Integer.toString(((InetSocketAddress) address).getPort());
                    System.setProperty("http.proxyHost", host);
                    System.setProperty("http.proxyPort", port);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
//        try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
        try (CloseableHttpClient client = HttpClients.createSystem()) {
            HttpGet request = new HttpGet("http://api.quotable.io/random?maxLength=40");
            return client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), RandomQuoteResponse.class));
        }
    }

}
