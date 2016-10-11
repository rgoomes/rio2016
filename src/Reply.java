import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.InvalidDestinationRuntimeException;

public class Reply {
	private ConnectionFactory cf;
	private Destination d;
	private JMSContext jc;

	public Reply() throws JMSException, NamingException {
		cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		d = InitialContext.doLookup("jms/queue/Queue");
		jc = cf.createContext("root", "root");
	}

	public Destination send(String text, Boolean to_temp_queue, Destination temp_destination) {
		Destination replyto = null;

		try {
			JMSProducer jp = jc.createProducer();
			TextMessage msg = jc.createTextMessage();
			msg.setText(text);

			if(to_temp_queue)
				jp.send(temp_destination, msg);
			else {
				replyto = jc.createTemporaryQueue();
				msg.setJMSReplyTo(replyto);
				jp.send(d, msg);
			}
		} catch (JMSException e) {
			System.out.println("Reply::send exception");
		} catch (InvalidDestinationRuntimeException e){
			System.out.println("Reply::send exception: invalid temporary queue");
		}

		return replyto;
	}

	public TextMessage receive(Boolean from_temp_queue, Destination temp_destination) {
		JMSConsumer mc;

		if(from_temp_queue){
			mc = jc.createConsumer(temp_destination);
			return (TextMessage) mc.receive(10000 /* 10 second timeout */ );
		} else {
			mc = jc.createConsumer(d);
			return (TextMessage) mc.receive();
		}
	}

	public void close(){
		jc.close();
	}
}
