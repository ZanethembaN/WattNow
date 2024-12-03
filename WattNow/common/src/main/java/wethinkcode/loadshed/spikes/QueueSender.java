package wethinkcode.loadshed.spikes;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * I am a small "maker" app for receiving MQ messages from the Stage Service.
 */
public class QueueSender implements Runnable
{
    private static long NAP_TIME = 2000; //ms

    public static final String MQ_URL = "tcp://localhost:61616";

    public static final String MQ_USER = "admin";

    public static final String MQ_PASSWD = "admin";

    public static final String MQ_QUEUE_NAME = "stage";

    public static void main( String[] args ){
        final QueueSender app = new QueueSender();
        app.setArgs(args);
        app.run();
    }

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
            sendAllMessages( new String[]{ "{ \"stage\":4 }" } );

        }catch( JMSException erk ){
            throw new RuntimeException( erk );
        }finally{
            closeResources();
        }
        System.out.println( "Bye..." );
    }

    private void sendAllMessages( String[] messages ) throws JMSException {
        MessageProducer producer = session.createProducer(session.createQueue(MQ_QUEUE_NAME));

        for (String string : messages) {
            TextMessage msg = session.createTextMessage(string);
            producer.send(msg);
        }
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
