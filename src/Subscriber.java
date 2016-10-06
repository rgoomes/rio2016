import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Subscriber {
	TopicConnection conn = null;
	TopicSession session = null;
	TopicSubscriber subscriber = null;
	Topic topic = null;

	public Subscriber(String id) throws JMSException, NamingException {
		TopicConnectionFactory tcf = (TopicConnectionFactory) InitialContext.doLookup("jms/RemoteConnectionFactory");
		topic = InitialContext.doLookup("jms/topic/Topic");

		conn = tcf.createTopicConnection("root", "root");
		conn.setClientID(id);		
		session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		conn.start();
		subscriber = session.createDurableSubscriber(topic, "sub");
	}

	public String recv() throws JMSException, NamingException {
		Message msg = subscriber.receive();
		return ((TextMessage)msg).getText();
	}

	public void stop() throws JMSException {
		subscriber.close();
		conn.stop();
		session.close();
		conn.close();
	}
}
