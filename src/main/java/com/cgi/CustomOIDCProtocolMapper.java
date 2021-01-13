package com.cgi;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * An OIDC protocol mapper that retrieves data from an HTTP JSON API and adds it as a claim to the token(s).
 */
public class CustomOIDCProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "oidc-customprotocolmapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    private static final String URL_NAME = PROVIDER_ID + ".url";
    private static final String ATT_NAME = PROVIDER_ID + ".name";

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        ProviderConfigProperty property1 = new ProviderConfigProperty();
        property1.setName(URL_NAME);
        property1.setLabel("URL");
        property1.setType(ProviderConfigProperty.STRING_TYPE);
        property1.setHelpText("URL of the resource to retrieve an attribute from.");
        configProperties.add(property1);

       ProviderConfigProperty property2 = new ProviderConfigProperty();
        property2.setName(ATT_NAME);
        property2.setLabel("Attribute");
        property2.setType(ProviderConfigProperty.STRING_TYPE);
        property2.setHelpText("Name of the JSON attribute to retrieve.");
        configProperties.add(property2);

        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomOIDCProtocolMapper.class);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "External API";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "Retrieve attribute from external API.";
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {

        String url = mappingModel.getConfig().get(URL_NAME);
        String attName = mappingModel.getConfig().get(ATT_NAME);
        if (url == null || attName == null) {
            return;
        }

        String attValue = retrieveAttribute(url, attName);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, attValue);
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    static String retrieveAttribute(String url, String attName) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("User-Agent", CustomOIDCProtocolMapper.class.getSimpleName())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonReader reader = Json.createReader(new StringReader(response.body()));
            return reader.readObject().getString(attName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


}