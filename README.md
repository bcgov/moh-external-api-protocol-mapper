[![img](https://img.shields.io/badge/Lifecycle-Retired-d45500)](https://github.com/bcgov/repomountie/blob/master/doc/lifecycle-badges.md)

This repository contains a proof-of-concept Keycloak plug-in that adds extra claims to an OIDC token retrieved from an external REST API.

## Installation

1. Build the project by running `mvn package`.
2. Copy the resulting JAR to Keycloak's `standalone/deployments/` directory.

The JAR will be hot deployed, so Keycloak can be running or not.

## Configuration

The plugin is a custom OIDC Protocol Mapper, and it is configured like other mappers.

1. Create a new client in Keycloak or use an existing one.
2. Go to the "Mappers" tab.
3. Click the "Create" button.
4. In the "Mapper Type" drop-down, choose "External API".
5. Configure the mapper, referring to the onscreen tooltips as needed.

## Demoing

The plugin will execute whenever a token is generated. A fast way to get a token is to enable service accounts on the Keycloak client and then execute:  
  
```
curl -s http://<your-keycloak-url>/auth/realms/master/protocol/openid-connect/token -d grant_type=client_credentials -d client_id=<client_id> -d client_secret=<client_secret>
```

The resulting JWT token can be decoded at https://jwt.io, but remember that a token is a credential and this is a public third-party website.

Another option is to deploy an example application capable of obtaining and inspecting tokens. I recommend Keycloak's demo [JavaScript Example](https://github.com/keycloak/keycloak/tree/master/examples/js-console). You don't need to follow the steps given there, you just need to grab the `index.html` and `keycloak.json` files and deploy them anywhere handy. You will of course need to configure a public client on Keycloak and enter your settings in `keycloak.json`.

## Example

In the screenshot below, we configure the custom mapper to retrieve a JSON attribute named "origin" from the https://httpbin.org/get API, and set it as a claim named "special":

![image](https://user-images.githubusercontent.com/1767127/104387913-fc7d0980-54ec-11eb-9ba5-8d1402b043a1.png)

In the screenshot below, we inspect the token in Keycloak's [JavaScript Example]:

![image](https://user-images.githubusercontent.com/1767127/104388165-82995000-54ed-11eb-8a02-06fd177fc4cc.png)

## Private SPI

During plugin installation, you may see this warning in the Keycloak log:  
  
```
WARN [org.keycloak.services] (ServerService Thread Pool -- 65) KC-SERVICES0047: oidc-customprotocolmapper (com.cgi.CustomOIDCProtocolMapper) is implementing the internal SPI protocol-mapper. This SPI is internal and may change without notice
```

The `protocol-mapper` SPI has been available since at least Keycloak 4, and it's used by built-in mappers, so it's probably fairly stable. 

## Other documentation

* If you need examples, the [source code for the built-in mappers](https://github.com/keycloak/keycloak/tree/master/services/src/main/java/org/keycloak/protocol/oidc/mappers) is on GitHub.
* Keycloak's documentation on Server Development, in particular for [Service Provider Interfaces](https://www.keycloak.org/docs/latest/server_development/index.html#_providers).
* [Education's implementation of a custom protocol mapper](https://github.com/bcgov/EDUC-KEYCLOAK-SOAM/blob/master/extensions/services/src/main/java/ca/bc/gov/educ/keycloak/soam/mapper/SoamProtocolMapper.java). Similar to this proof-of-concept, it also retrieves attributes from an external API. Unlike this simpler example, Education's external API is accessed using the client credentials grant.
* The SSO team's implementation of [a custom authenticator to match new users to existing ones](https://github.com/bcgov/ocp-sso/blob/master/extensions/services/src/main/java/com/github/bcgov/keycloak/IdpCreateUserIfUniqueAuthenticator.java).
* [A blog post describing how to implement a custom protocol mapper](https://medium.com/@pavithbuddhima/how-to-add-custom-claims-to-jwt-tokens-from-an-external-source-in-keycloak-52bd1ff596d3).

## Notes

* Prior to Keycloak 7, Keycloak supported a mapper type called a "Script Mapper" that allowed administrators to define custom mappers right in the Admin GUI using Javascript. The ability to define scripts in the Admin GUI is [deprecated](https://www.keycloak.org/docs/latest/server_development/index.html#using-keycloak-administration-console-to-upload-scripts), but the ability to implement custom OIDC protocol mappers in Javascript is [still supported](https://www.keycloak.org/docs/latest/server_development/index.html#_script_providers).
