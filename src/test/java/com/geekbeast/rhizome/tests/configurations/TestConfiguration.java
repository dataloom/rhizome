package com.geekbeast.rhizome.tests.configurations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.kryptnostic.rhizome.configuration.Configuration;
import com.kryptnostic.rhizome.configuration.ConfigurationKey;
import com.kryptnostic.rhizome.configuration.SimpleConfigurationKey;

/**
 * @author Matthew Tamayo-Rios
 */
public class TestConfiguration implements Configuration {
    private static final long         serialVersionUID       = 129440984814569272L;

    protected static final ConfigurationKey key              = new SimpleConfigurationKey( "test.yaml" );

    protected static final String     REQUIRED_TEST_PROPERTY = "required";
    protected static final String     OPTIONAL_TEST_PROPERTY = "optional";

    protected final String            required;
    protected final Optional<String>  optional;

    @JsonCreator
    public TestConfiguration(
            @JsonProperty( REQUIRED_TEST_PROPERTY ) String required,
            @JsonProperty( OPTIONAL_TEST_PROPERTY ) Optional<String> optional ) {
        this.required = required;
        this.optional = optional;
    }

    public static ConfigurationKey key() {
        return key;
    }

    @Override
    @JsonIgnore
    public ConfigurationKey getKey() {
        return key;
    }

    @JsonProperty( REQUIRED_TEST_PROPERTY )
    public String getRequired() {
        return required;
    }

    @JsonProperty( OPTIONAL_TEST_PROPERTY )
    public Optional<String> getOptional() {
        return optional;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( optional == null ) ? 0 : optional.hashCode() );
        result = prime * result + ( ( required == null ) ? 0 : required.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( !( obj instanceof TestConfiguration ) ) {
            return false;
        }
        TestConfiguration other = (TestConfiguration) obj;
        if ( optional == null ) {
            if ( other.optional != null ) {
                return false;
            }
        } else if ( !optional.equals( other.optional ) ) {
            return false;
        }
        if ( required == null ) {
            if ( other.required != null ) {
                return false;
            }
        } else if ( !required.equals( other.required ) ) {
            return false;
        }
        return true;
    }
}
