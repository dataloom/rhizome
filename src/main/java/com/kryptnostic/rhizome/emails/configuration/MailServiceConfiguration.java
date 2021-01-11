package com.kryptnostic.rhizome.emails.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryptnostic.rhizome.configuration.Configuration;
import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.SimpleConfigurationKey;

public final class MailServiceConfiguration implements Configuration {
    private static final long         serialVersionUID     = 2393163076245402143L;
    protected static final ConfigurationKey key            = new SimpleConfigurationKey( "mail.yaml" );
    protected static final String     SMTP_AUTH_PROPERTY   = "smtp-auth";
    protected static final String     TTLS_ENABLE_PROPERTY = "ttls-enable";
    protected static final String     SMPTP_HOST_PROPERTY  = "smtp-host";
    protected static final String     SMTP_PORT_PROPERTY   = "smtp-port";
    protected static final String     USERNAME_PROPERTY    = "username";
    protected static final String     PASSWORD_PROPERTY    = "password";

    protected final String            smtpAuth;
    protected final String            startTtlsEnable;
    protected final String            smtpHost;
    protected final String            smtpPort;
    protected final String            username;
    protected final String            password;

    @JsonCreator
    public MailServiceConfiguration(
            @JsonProperty( SMTP_AUTH_PROPERTY ) String smtpAuth,
            @JsonProperty( TTLS_ENABLE_PROPERTY ) String startTtlsEnable,
            @JsonProperty( SMPTP_HOST_PROPERTY ) String smtpHost,
            @JsonProperty( SMTP_PORT_PROPERTY ) String smtpPort,
            @JsonProperty( USERNAME_PROPERTY ) String username,
            @JsonProperty( PASSWORD_PROPERTY ) String password ) {
        this.smtpAuth = smtpAuth;
        this.startTtlsEnable = startTtlsEnable;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
    }

    @JsonProperty( SMTP_AUTH_PROPERTY )
    public String getSmtpAuth() {
        return smtpAuth;
    }

    @JsonProperty( TTLS_ENABLE_PROPERTY )
    public String getStartTtlsEnable() {
        return startTtlsEnable;
    }

    @JsonProperty( SMPTP_HOST_PROPERTY )
    public String getSmtpHost() {
        return smtpHost;
    }

    @JsonProperty( SMTP_PORT_PROPERTY )
    public String getSmtpPort() {
        return smtpPort;
    }

    @JsonProperty( USERNAME_PROPERTY )
    public String getUsername() {
        return username;
    }

    @JsonProperty( PASSWORD_PROPERTY )
    public String getPassword() {
        return password;
    }

    public static ConfigurationKey key() {
        return key;
    }
    
    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key;
    }
}
