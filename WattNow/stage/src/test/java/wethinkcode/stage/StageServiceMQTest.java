package wethinkcode.stage;

import java.util.concurrent.SynchronousQueue;
import javax.jms.*;

import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.*;
import wethinkcode.loadshed.common.transfer.StageDO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("expensive")
public class StageServiceMQTest {

    public static final int TEST_PORT = 7777;
    private static StageService server;
    private static ActiveMQConnectionFactory factory;
    private static Connection mqConnection;

    // SynchronousQueue to capture messages from the listener
    private static SynchronousQueue<StageDO> resultCatcher;

    @BeforeAll
    public static void startInfrastructure() throws JMSException {
        startMsgQueue();
        startStageSvc();
        resultCatcher = new SynchronousQueue<>();  // Initialize the queue to capture messages
    }

    @AfterAll
    public static void cleanup() throws JMSException {
        server.stop();
        if (mqConnection != null) {
            mqConnection.close();
        }
    }

    @BeforeEach
    public void connectMqListener() throws JMSException {
        // Create the JMS connection and session
        mqConnection = factory.createConnection();
        final Session session = mqConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination dest = session.createTopic(StageService.MQ_TOPIC_NAME);

        // Create a message consumer for the topic
        final MessageConsumer receiver = session.createConsumer(dest);

        // Define the MessageListener
        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                // Handle the message as required for your tests
                try {
                    if (message instanceof ObjectMessage) {
                        ObjectMessage objectMessage = (ObjectMessage) message;
                        // Process the object
                        StageDO stage = (StageDO) objectMessage.getObject();
                        // Log the received stage to see if we are receiving the message
                        System.out.println("Received stage: " + stage.getStage());  // Add logging here
                        resultCatcher.put(stage); // Put the received stage in the result catcher
                    }
                } catch (JMSException | InterruptedException e) {
                    e.printStackTrace();
                    fail("Failed to process message");
                }
            }
        };

        // Set the listener for the message consumer
        receiver.setMessageListener(listener);

        // Start the connection
        mqConnection.start();
    }


    @AfterEach
    public void closeMqConnection() throws JMSException {
        if (mqConnection != null) {
            mqConnection.close();
            mqConnection = null;
        }
    }

    @Test
    public void sendMqEventWhenStageChanges() throws InterruptedException, JMSException {
        // Get the initial stage from the service
        final HttpResponse<StageDO> startStage = Unirest.get(serverUrl() + "/stage").asObject(StageDO.class);
        assertEquals(HttpStatus.OK, startStage.getStatus());

        final StageDO initialData = startStage.getBody();
        final int newStage = initialData.getStage() + 1;

        // Change the stage via the REST API
        final HttpResponse<JsonNode> changeStage = Unirest.post(serverUrl() + "/stage")
                .header("Content-Type", "application/json")
                .body(new StageDO(newStage))
                .asJson();
        assertEquals(HttpStatus.OK, changeStage.getStatus());

        // Wait for the message to be received, with a timeout to avoid indefinite blocking
        StageDO receivedStage = resultCatcher.poll(30, java.util.concurrent.TimeUnit.SECONDS);  // Timeout after 30 seconds
        if (receivedStage == null) {
            fail("Failed to receive stage change message in time.");
        }

        // Verify the stage in the message matches the new stage
        assertEquals(newStage, receivedStage.getStage());
    }


    private static void startMsgQueue() throws JMSException {
        // Initialize ActiveMQ connection factory
        factory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    }

    private static void startStageSvc() {
        // Initialize and start the StageService
        server = new StageService().initialise();
        server.start(TEST_PORT);
    }

    private String serverUrl() {
        return "http://localhost:" + TEST_PORT;
    }
}
