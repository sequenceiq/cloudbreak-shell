package com.sequenceiq.cloudbreak.shell.configuration;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.shell.CommandLine;
import org.springframework.shell.SimpleShellCommandLineOptions;
import org.springframework.shell.commands.ExitCommands;
import org.springframework.shell.commands.HelpCommands;
import org.springframework.shell.commands.ScriptCommands;
import org.springframework.shell.commands.VersionCommands;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;

/**
 * Spring bean definitions.
 */
@Configuration
public class ShellConfiguration {

    public static final String CLIENT_ID = "cloudbreak_shell";

    @Value("${cloudbreak.address:https://cloudbreak-api.sequenceiq.com}")
    private String cloudbreakAddress;

    @Value("${identity.address:https://identity.sequenceiq.com}")
    private String identityServerAddress;

    @Value("${sequenceiq.user:}")
    private String user;

    @Value("${sequenceiq.password:}")
    private String password;

    @Value("${cmdfile:}")
    private String cmdFile;

    @Bean
    CloudbreakClient createCloudbreakClient() throws Exception {
        if (!"".equals(user) && !"".equals(password)) {
            String token = getToken(identityServerAddress, user, password);
            return new CloudbreakClient(cloudbreakAddress, token);
        }
        return null;
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    HistoryFileNameProvider defaultHistoryFileNameProvider() {
        return new DefaultHistoryFileNameProvider();
    }

    @Bean(name = "shell")
    JLineShellComponent shell() {
        return new JLineShellComponent();
    }

    @Bean
    CommandLine commandLine() throws Exception {
        String[] args = cmdFile.length() > 0 ? new String[]{"--cmdfile", cmdFile} : new String[0];
        return SimpleShellCommandLineOptions.parseCommandLine(args);
    }

    @Bean
    ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean() {
        return new ThreadPoolExecutorFactoryBean();
    }

    @Bean
    ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    CommandMarker exitCommand() {
        return new ExitCommands();
    }

    @Bean
    CommandMarker versionCommands() {
        return new VersionCommands();
    }

    @Bean
    CommandMarker helpCommands() {
        return new HelpCommands();
    }

    @Bean
    CommandMarker scriptCommands() {
        return new ScriptCommands();
    }

    private String getToken(String identityServerAddress, String user, String password) throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("credentials", String.format("{\"username\":\"%s\",\"password\":\"%s\"}", user, password));

        String token = null;
        try {
            ResponseEntity<String> authResponse = restTemplate.exchange(
                    String.format("%s/oauth/authorize?response_type=token&client_id=%s", identityServerAddress, CLIENT_ID),
                    HttpMethod.POST,
                    new HttpEntity<Map>(requestBody, headers),
                    String.class
            );
            if (HttpStatus.FOUND == authResponse.getStatusCode() && authResponse.getHeaders().get("Location") != null) {
                token = parseTokenFromLocationHeader(authResponse);
            } else {
                System.out.println("Couldn't get an access token from the identity server, check its configuration! "
                        + "Perhaps cloudbreak_shell is not autoapproved?");
                System.out.println("Response from identity server: ");
                System.out.println("Headers: " + authResponse);
                throw new TokenUnavailableException("Wrong response from identity server.");
            }
        } catch (ResourceAccessException e) {
            System.out.println("Error occurred while trying to connect to identity server: " + e.getMessage());
            System.out.println("Check if your identity server is available and accepting requests on " + identityServerAddress);
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED == e.getStatusCode()) {
                System.out.println("Error occurred while getting token from identity server: " + identityServerAddress);
                System.out.println("Check your username and password.");
            } else {
                System.out.println("Something unexpected happened, couldn't get token from identity server. Please check your configurations.");
            }
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        } catch (Exception e) {
            System.out.println("Something unexpected happened, couldn't get token from identity server. Please check your configurations.");
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        }
        return token;
    }

    private String parseTokenFromLocationHeader(ResponseEntity<String> authResponse) {
        String location = authResponse.getHeaders().get("Location").get(0);
        String[] parts = location.split("#");
        String[] parameters = parts[1].split("&|=");
        for (int i = 0; i < parameters.length; i++) {
            String param = parameters[i];
            int nextIndex = i + 1;
            if ("access_token".equals(param) && !(nextIndex > parameters.length)) {
                return parameters[nextIndex];
            }
        }
        throw new TokenUnavailableException("Token could not be found in the 'Location' header.");
    }
}
