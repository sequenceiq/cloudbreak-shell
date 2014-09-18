package com.sequenceiq.cloudbreak.shell.configuration;

import java.util.Map;

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

    @Value("${cloudbreak.host:localhost}")
    private String host;

    @Value("${cloudbreak.port:8080}")
    private String port;

    @Value("${cloudbreak.user:user@seq.com}")
    private String user;

    @Value("${cloudbreak.password:test123}")
    private String password;

    @Value("${identity.host:localhost}")
    private String identityServerHost;

    @Value("${identity.port:8888}")
    private String identityServerPort;

    @Value("${cmdfile:}")
    private String cmdFile;

    @Bean
    CloudbreakClient createCloudbreakClient() {
        String token = getToken(identityServerHost, identityServerPort, user, password);
        return new CloudbreakClient(host, port, token);
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

    private String getToken(String identityServerHost, String identityServerPort, String user, String password) {
        String identityServerUrl = String.format("http://%s:%s", identityServerHost, identityServerPort);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("credentials", String.format("{\"username\":\"%s\",\"password\":\"%s\"}", user, password));

        String token = null;
        try {
            ResponseEntity<String> authResponse = restTemplate.exchange(
                    String.format("%s/oauth/authorize?response_type=token&client_id=%s", identityServerUrl, CLIENT_ID),
                    HttpMethod.POST,
                    new HttpEntity<Map>(requestBody, headers),
                    String.class
            );
            if (HttpStatus.FOUND == authResponse.getStatusCode() && authResponse.getHeaders().get("Location") != null) {
                String location = authResponse.getHeaders().get("Location").get(0);
                String[] parts = location.split("#|&|=");
                token = parts[2];
            } else {
                System.out.println("Couldn't get an access token from the identity server, check its configuration! "
                        + "Perhaps cloudbreak_shell is not autoapproved?");
                System.out.println("Response from identity server: ");
                System.out.println("Headers: " + authResponse);
                throw new TokenUnavailableException("Wrong response from identity server.");
            }
        } catch (ResourceAccessException e) {
            System.out.println("Error occurred while trying to connect to identity server: " + e.getMessage());
            System.out.println("Check if your identity server is available and accepting requests on " + identityServerUrl);
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED == e.getStatusCode()) {
                System.out.println("Error occurred while getting token from identity server: " + e.getMessage());
                System.out.println("Check your username and password.");
            }
            System.out.println("Something unexpected happened, couldn't get token from identity server. Please check your configurations.");
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        } catch (Exception e) {
            System.out.println("Something unexpected happened, couldn't get token from identity server. Please check your configurations.");
            throw new TokenUnavailableException("Error occurred while getting token from identity server", e);
        }
        return token;
    }
}
