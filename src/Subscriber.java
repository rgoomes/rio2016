import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.ConnectionFactory;
import javax.jms.TextMessage;
import javax.jms.JMSConsumer;
import javax.jms.InvalidClientIDRuntimeException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Subscriber {
	private JMSContext jc;
	private JMSConsumer subscriber;

	public Subscriber(String id) throws NamingException, InvalidClientIDRuntimeException {
		ConnectionFactory cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		Topic topic = InitialContext.doLookup("jms/topic/Topic");
		jc = cf.createContext("root", "root");
		jc.setClientID(id);
		subscriber = jc.createDurableConsumer(topic, "sub");
	}

	public String recv() throws JMSException {
		Message msg = subscriber.receive();
		return ((TextMessage) msg).getText();
	}

	public void stop() {
		subscriber.close();
		jc.close();
	}
}
