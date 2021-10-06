package store;

import javax.inject.Singleton;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.serializer.JavaSerializer;

import org.pac4j.play.store.PlayCookieSessionStore;
import org.pac4j.play.store.DataEncrypter;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * A session store which only uses the Play Session cookie for storage, allowing for a stateless backend.
 *
 * @author 
 * @since 6.1.0
 */
@Singleton
public class ModPlayCookieSessionStore extends PlayCookieSessionStore {

    public ModPlayCookieSessionStore(final DataEncrypter dataEncrypter){super(dataEncrypter);};

    @Override
    @SuppressWarnings("unchecked")
    protected Object clearUserProfiles(Object value) {
        final LinkedHashMap<String, CommonProfile> profiles = (LinkedHashMap<String, CommonProfile>) super.clearUserProfiles(value);
        profiles.forEach((name, profile) -> {
            final Map<String, Object> removedAttributes = profile.getAttributes();
            // remain OidcProfile's name, picture
            removedAttributes.remove("name");
            removedAttributes.remove("picture");
            for (String key : removedAttributes.keySet()) {
                profile.removeAttribute(key);
            }
            }
        );
        return profiles;
    }
}