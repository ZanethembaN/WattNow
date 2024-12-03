package wethinkcode.stage;

import com.google.common.annotations.VisibleForTesting;
import io.javalin.Javalin;
import io.javalin.http.Context;
import wethinkcode.loadshed.common.transfer.StageDO;
import wethinkcode.loadshed.spikes.TopicSender;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import javax.jms.JMSException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * I provide a REST API that reports the current loadshedding "stage". I provide two endpoints:
 * <dl>
 * <dt>GET /stage
 * <dd>report the current stage of loadshedding as a JSON serialisation of a {@code StageDO} data/transfer object
 * <dt>POST /stage
 * <dd>set a new loadshedding stage/level by POSTing a JSON-serialised {@code StageDO} instance as the body of the
 * request.
 * </ul>
 */
public class StageService {

    public static final int DEFAULT_STAGE = 0; // no loadshedding. Ha!

    public static int DEFAULT_PORT = 7001;

    public static final String MQ_TOPIC_NAME = "stage";

    public static Map<StageDO, String> stages = Map.of(
        new StageDO(1), "Stage 1 allows for up to 1000 MW of the national load to be shed.",
        new StageDO(2), "Stage 2 allows for up to 2000 MW of the national load to be shed.",
        new StageDO(3), "Stage 3 allows for up to 3000 MW of the national load to be shed.",
        new StageDO(4), "Stage 4 allows for up to 4000 MW of the national load to be shed.",
        new StageDO(5), "Stage 5 allows for up to 5000 MW of the national load to be shed.",
        new StageDO(6), "Stage 6 allows for up to 6000 MW of the national load to be shed.",
        new StageDO(7), "Stage 7 allows for up to 7000 MW of the national load to be shed.",
        new StageDO(8), "Stage 8 allows for up to 8000 MW of the national load to be shed."
    );

    // private TopicSender topicSender = new TopicSender();

    public static String ESKOM_STAGE_ENDPOINT = "https://loadshedding.eskom.co.za/LoadShedding/GetStatus";

    
    private int loadSheddingStage;
    
    private Javalin server;

    StageDO currentStage;
    
    private int servicePort;
    
    private TopicSender topicSender = new TopicSender();
    ScheduledExecutorService scheduler;
    
    @VisibleForTesting
    public
    StageService initialise(){
        return initialise( DEFAULT_STAGE );
    }

    @VisibleForTesting
    StageService initialise( int initialStage ){
        loadSheddingStage = initialStage;
        assert loadSheddingStage >= 0;
        topicSender.run();
        
        setupScheduleExecutor();
        server = initHttpServer();
        return this;
    }
    
    public void start(){
        start( DEFAULT_PORT );
    }
    
    @VisibleForTesting
    void start( int networkPort ){
        DEFAULT_PORT = networkPort;
        run();
    }
    
    public void stop(){
        server.stop();
    }
    
    public void run(){
        server.start( DEFAULT_PORT );
    }
    
    private Javalin initHttpServer(){
        return Javalin.create()
        .before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            ctx.header("Access-Control-Allow-Headers", "Content-Type");
        })
        .get( "/stage", this::getCurrentStage );
    }

    private Context getCurrentStage( Context ctx ){
        HttpResponse<String> stage_response = null;
        try {
            stage_response = Unirest.get(ESKOM_STAGE_ENDPOINT).asString();
            System.out.println(stage_response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ctx.json( new StageDO( getStageFromResponse(stage_response) ) );
    }

    private int getStageFromResponse(HttpResponse<String> response) {
        return Integer.parseInt(response.getBody().toString());
    }
    
    // private Context setNewStage( Context ctx ){
    //     final StageDO stageData = ctx.bodyAsClass( StageDO.class );
    //     final int newStage = stageData.getStage();
    //     if( newStage >= 0 && newStage <= 8 ){
    //         loadSheddingStage = newStage;
    //         broadcastStageChangeEvent( ctx );
    //         ctx.status( HttpStatus.OK );
    //     }else{
    //         ctx.status( HttpStatus.BAD_REQUEST );
    //     }
    //     return ctx.json( new StageDO( loadSheddingStage ) );
    // }

    private void setupScheduleExecutor() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            () -> makeStageRequest(), 0, 5, TimeUnit.SECONDS
        );
    }

    private void makeStageRequest() {
        HttpResponse<String> stage_response = null;
        try {
            stage_response = Unirest.get(ESKOM_STAGE_ENDPOINT).asString();
            currentStage = new StageDO(Integer.parseInt(stage_response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void broadcastStageChangeEvent( Context ctx ){
        try {
            topicSender.sendMessages(ctx.body());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    public static void main( String[] args ){
        final StageService svc = new StageService().initialise();
        svc.start();
    }
}
