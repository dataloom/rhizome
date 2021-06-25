package com.kryptnostic.rhizome.configuration;

public final class ConfigurationConstants {
    private ConfigurationConstants() {
    }

    public static final class Profiles {
        public static final String AWS_CONFIGURATION_PROFILE        = "aws";
        public static final String AWS_TESTING_PROFILE              = "awstest";
        public static final String LOCAL_CONFIGURATION_PROFILE      = "local";
        public static final String KUBERNETES_CONFIGURATION_PROFILE = "kubernetes";
        public static final String POSTGRES_DB_PROFILE              = "postgres";

        private Profiles() {
        }
    }

    public static final class Environments {
        public static final String TEST_PROFILE         = "test";
        public static final String DEVELOPMENT_PROFILE  = "dev";
        public static final String PRODUCTION_PROFILE   = "prod";

        private Environments() {
        }
    }

    public static final class HZ {
        private HZ() {
        }

        public static final class MAPS {
            public static final String CONFIGURATION = "configurations";

            private MAPS() {
            }
        }
    }
}
