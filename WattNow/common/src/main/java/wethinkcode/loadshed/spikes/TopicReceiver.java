package wethinkcode.loadshed.spikes;

import wethinkcode.loadshed.common.mq.MQ;
import org.apache.activemq.ActiveMQConnectionFactory;
import wethinkcode.loadshed.common.transfer.StageDO;

import javax.jms.*;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service by
 * subscribing to a Topic.
 */
public class TopicReceiver implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static final String MQ_TOPIC_NAME = "stage";

    public static void main( String[] args ){
        final TopicReceiver app = new TopicReceiver();
        app.run();
    }

    private boolean running = true;

    private Connection connection;


    @Override
    public void run(){
        setUpMessageListener();
        while( running ){
            System.out.println( "Still doing stufff..." );
            snooze();
        }
        closeConnection();
        System.out.println( "Bye..." );
    }

    private void setUpMessageListener(){
        try{
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ.URL );
            connection = factory.createConnection( MQ.USER, MQ.PASSWD );

            final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            final Destination dest = session.createTopic( MQ_TOPIC_NAME ); // <-- NB: Topic, not Queue!

            final MessageConsumer receiver = session.createConsumer( dest );
            receiver.setMessageListener( new MessageListener(){
                @Override
                public void onMessage( Message m ){
                    try {
                        if (m instanceof TextMessage) {
                            String body = ((TextMessage) m).getText();
                            if ("SHUTDOWN".equals(body)) {
                                running = false;
                            }
                            System.out.println("Message received: "+body);
                        } else {
                            System.out.println("Message is not of TextMessage");
                        }
                    } catch (JMSException e) {
                        System.out.println("Unexpected message type: "+m.getClass());
                        e.printStackTrace();
                    }
                }
            }
            );
            connection.start();

        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }
    }

    private void snooze(){
        try{
            Thread.sleep( NAP_TIME );
        }catch( InterruptedException eek ){
            // meh...
        }
    }

    private void closeConnection(){
        if( connection != null ) try{
            connection.close();
        }catch( JMSException ex ){
            // meh
        }
    }

}
