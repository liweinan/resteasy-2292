package io.weli.jersey.test.resteasy2292;

import jakarta.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FollowRedirectsTest extends JerseyTest {
    @Path("/followTest")
    public static class RedirectResource {
        @GET
        public String get() {
            return "GET";
        }

        @GET
        @Path("redirect1")
        public Response redirect1() {
            return Response.status(302).location(
                    UriBuilder.fromResource(RedirectResource.class).path(RedirectResource.class, "redirect2").build())
                    .build();
        }

        @GET
        @Path("redirect2")
        public Response redirect2() {
            return Response.status(302).location(UriBuilder.fromResource(RedirectResource.class).build()).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RedirectResource.class);
    }


    private static class RedirectTestFilter implements ClientResponseFilter {
        public static final String RESOLVED_URI_HEADER = "resolved-uri";

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
            if (responseContext instanceof ClientResponse) {
                ClientResponse clientResponse = (ClientResponse) responseContext;
                responseContext.getHeaders().putSingle(RESOLVED_URI_HEADER, clientResponse.getResolvedRequestUri().toString());
            }
        }
    }

    @Test
    public void testDoFollow() {
        Response r = target("followTest/redirect1")
                .register(RedirectTestFilter.class).request().get();
        assertEquals(200, r.getStatus());
        assertEquals("GET", r.readEntity(String.class));
        assertEquals(
                UriBuilder.fromUri(getBaseUri()).path(RedirectResource.class).build().toString(),
                r.getHeaderString(RedirectTestFilter.RESOLVED_URI_HEADER));
    }

    @Test
    public void testDontFollow() {
        WebTarget t = target("followTest/redirect1");
        t.property(ClientProperties.FOLLOW_REDIRECTS, false);
        assertEquals(302, t.request().get().getStatus());
    }
}
