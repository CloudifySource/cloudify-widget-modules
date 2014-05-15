package cloudify.widget.website.initializer;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

public class EmbeddedJetty {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedJetty.class);
    private static final int DEFAULT_PORT = 8011;
    private static final String CONTEXT_PATH = "/";
    private static final String CONFIG_LOCATION = "classpath:context.xml";
    private static final String MAPPING_URL = "/*";
    private static final String DEFAULT_PROFILE = "dev";

    private static String configLocation;

    public static void main(String[] args) throws Exception {
        if( !ArrayUtils.isEmpty( args ) ){
            configLocation = args[ 0 ];
        }
        new EmbeddedJetty().startJetty(getPortFromArgs(args));
    }

    private static int getPortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.valueOf(args[0]);
            } catch (NumberFormatException ignore) {
            }
        }
        logger.debug("No server port configured, falling back to {}", DEFAULT_PORT);
        return DEFAULT_PORT;
    }

    private void startJetty(int port) throws Exception {
        logger.debug("Starting server at port {}", port);
        Server server = new Server(port);
        server.setHandler(getServletContextHandler(getContext()));
        server.start();
        logger.info("Server started at port {}", port);
        server.join();
    }

    private static ServletContextHandler getServletContextHandler(WebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath(CONTEXT_PATH);
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(context));
//        contextHandler.setResourceBase(new ClassPathResource("webapp").getURI().toString());
        return contextHandler;
    }

    private static WebApplicationContext getContext() {
        XmlWebApplicationContext context = new XmlWebApplicationContext();
//        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        String[] configLocations = configLocation == null ?
                new String[]{ CONFIG_LOCATION } : new String[]{ CONFIG_LOCATION, configLocation };
        System.out.println("Setting:" + configLocation);

        context.setConfigLocations(configLocations);

        context.getEnvironment().setDefaultProfiles(DEFAULT_PROFILE);
        return context;
    }

}
