package org.apache.geronimo.gshell.remote.client.auth;

import java.security.Principal;
import java.io.Serializable;

import org.apache.geronimo.gshell.common.tostring.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.common.tostring.ToStringStyle;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class ClientPrincipal
    implements Principal, Serializable
{
    private static final long serialVersionUID = 1;

    private final String name;
    
    private final Object identity;

    public ClientPrincipal(final String name, final Object identity) {
        assert name != null;
        assert identity != null;
        
        this.name = name;
        this.identity = identity;
    }

    public String getName() {
        return name;
    }

    public Object getIdentity() {
        return identity;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
