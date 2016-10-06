import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Publisher {
	private TopicConnection conn = null;
	private TopicSession session = null;
	private TopicPublisher publisher = null;

	public Publisher() throws JMSException, NamingException {
		TopicConnectionFactory tcf = (TopicConnectionFactory) InitialContext.doLookup("jms/RemoteConnectionFactory");
		Topic topic = InitialContext.doLookup("jms/topic/Topic");

		conn = tcf.createTopicConnection("root", "root");
		session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
		conn.start();
		publisher = session.createPublisher(topic);
	}

	public void send(String text) throws JMSException, NamingException {
		TextMessage tm = session.createTextMessage(text);
		publisher.publish(tm);
	}

	public void stop() throws JMSException {
		publisher.close();
		conn.stop();
		session.close();
		conn.close();
	}
}
