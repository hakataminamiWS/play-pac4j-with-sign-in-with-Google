package store;

import javax.inject.Singleton;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.serializer.JavaSerializer;

import org.pac4j.play.store.PlayCookieSessionStore;
import org.pac4j.play.store.DataEncrypter;

import org.pac4j.oidc.profile.OidcProfileDefinition;

import java.util.Map;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * for clear non used profile attributes, override clearUserProfiles of PlayCookieSessionStore.
 *
 */
@Singleton
public class ModPlayCookieSessionStore extends PlayCookieSessionStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModPlayCookieSessionStore.class);

    public ModPlayCookieSessionStore(final DataEncrypter dataEncrypter){super(dataEncrypter);};

    @Override
    @SuppressWarnings("unchecked")
    protected Object clearUserProfiles(Object value) {
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) super.clearUserProfiles(value);
        profiles.forEach((name, profile) -> {
            final Map<String, Object> removedAttributes = profile.getAttributes();
            // remain OidcProfile's name, picture
            removedAttributes.remove(OidcProfileDefinition.NAME);
            removedAttributes.remove(OidcProfileDefinition.PICTURE);
            for (String key : removedAttributes.keySet()) {
                LOGGER.debug("Remove attribute: {} ", key);
                profile.removeAttribute(key);
            }
            }
        );
        return profiles;
    }
}