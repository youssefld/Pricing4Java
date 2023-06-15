package com.featuretogglingjava;

public interface FeatureToggling {
    
    /**
     * Returns a JWT that contains not only the user authentication data, but also
     * the an object with the features' ids and their respective boolean values
     * representing if they are enabled or not.
     * @return {@link String} JWT Token
     */
    String generateUserToken();
}
