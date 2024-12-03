package wethinkcode.loadshed.spikes;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service.
 */
public class TopicSender implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static final String MQ_URL = "tcp://localhost:61616";

    public static final String MQ_USER = "admin";

    public static final String MQ_PASSWD = "admin";

    public static final String MQ_TOPIC_NAME = "stage";

    public static void main( String[] args ){
        final TopicSender app = new TopicSender();
        app.cmdLineMsgs = args;
        app.run();
    }

    private boolean running = true;

    private String[] cmdLineMsgs;

    private Connection connection;

    private Session session;

    @Override
    public void run(){
            try{
                final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory( MQ_URL );
                connection = factory.createConnection( MQ_USER, MQ_PASSWD );
                connection.start();
                session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
                // sendMessages( line.length() == 0
                //     ? new String( "{ \"stage\":17 }" )
                //     : line );
            }catch( JMSException erk ){
                // throw new RuntimeException( erk );
                closeResources();
                System.out.println( "Bye..." );
            }
    }

    public void sendMessages( String messages ) throws JMSException {
        MessageProducer producer = session.createProducer(session.createTopic(MQ_TOPIC_NAME));

        // for (String string : messages) {
            TextMessage msg = session.createTextMessage(messages);
            System.out.println("Sent msg: "+msg.getText());
            producer.send(msg);
        // }
    }

    private void closeResources(){
        try{
            if( session != null ) session.close();
            if( connection != null ) connection.close();
        }catch( JMSException ex ){
            // wut?
        }
        session = null;
        connection = null;
    }

    public void setArgs(String[] args) {
        this.cmdLineMsgs = args;
    }

}
