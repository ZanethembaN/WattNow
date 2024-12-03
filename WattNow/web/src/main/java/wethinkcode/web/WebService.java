package wethinkcode.web;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import wethinkcode.places.PlaceNameService;
import wethinkcode.schedule.ScheduleService;
import wethinkcode.stage.StageService;

/**
 * I am the front-end web server for the LightSched project.
 * <p>
 * Remember that we're not terribly interested in the web front-end part of this server, more in the way it communicates
 * and interacts with the back-end services.
 */
public class WebService {

    public static final int DEFAULT_PORT = 5050;

    public static final String STAGE_SVC_URL = "http://localhost:" + StageService.DEFAULT_PORT;
    public static final String PLACES_SVC_URL = "http://localhost:" + PlaceNameService.DEFAULT_PORT;
    public static final String SCHEDULE_SVC_URL = "http://localhost:" + ScheduleService.DEFAULT_PORT;

    private static final String TEMPLATES_DIR = "/templates/";

    private PlaceNameService placeAPI = new PlaceNameService();
    private StageService stageAPI = new StageService();
    private ScheduleService scheduleAPI = new ScheduleService();

    private Javalin server;
    private int servicePort;

    @VisibleForTesting
    WebService initialise(){
        JavalinThymeleaf.init(templateEngine());
        configureHttpClient();
        server = configureHttpServer();
        return this;
    }

    public void start(){
        start( DEFAULT_PORT );
    }

    @VisibleForTesting
    void start( int networkPort ){
        servicePort = networkPort;
        run();
    }

    public void stop(){
        server.stop();
    }

    public void run(){
        server.start( servicePort );
    }

    private void configureHttpClient(){
        placeAPI.initialise().start();
        stageAPI.initialise().start();
        scheduleAPI.initialise().start();
    }

    private Javalin configureHttpServer(){
        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add(TEMPLATES_DIR, Location.CLASSPATH);
                })
                .before(ctx -> {
                    ctx.header("Access-Control-Allow-Origin", "*");
                    ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                    ctx.header("Access-Control-Allow-Headers", "Content-Type");
                });
        Routes.configure(app);
        return app;
    }

    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATES_DIR);
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
    public static void main( String[] args ){
        String portStr = System.getenv("PORT");

        int port = (portStr != null) ? Integer.parseInt(portStr) : DEFAULT_PORT;

        final WebService svc = new WebService().initialise();
        svc.start(port);

    }
}
