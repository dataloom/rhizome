package com.kryptnostic.rhizome.pods

import com.kryptnostic.rhizome.configuration.RhizomeConfiguration
import com.kryptnostic.rhizome.configuration.jetty.JettyConfiguration

/**
 * @author Drew Bailey (drew@openlattice.com)
 */
interface RootConfigurationSource {

    fun rhizomeConfiguration(): RhizomeConfiguration

    fun jettyConfiguration(): JettyConfiguration
}