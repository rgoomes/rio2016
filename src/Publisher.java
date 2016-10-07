import javax.jms.Topic;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Publisher {
	private Topic topic;
	private JMSContext jc;
	private JMSProducer publisher;

	public Publisher() throws NamingException {
		ConnectionFactory cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		topic = InitialContext.doLookup("jms/topic/Topic");
		jc = cf.createContext("root", "root");
		publisher = jc.createProducer();
	}

	public void send(String text) {
		TextMessage tm = jc.createTextMessage(text);
		publisher.send(topic, tm);
	}

	public void stop() {
		jc.close();
	}
}
