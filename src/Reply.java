import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Reply {
	private ConnectionFactory cf;
	private Destination d;
	private Connection c;
	private Session s;

	public Reply() throws JMSException, NamingException {
		cf = InitialContext.doLookup("jms/RemoteConnectionFactory");
		d = InitialContext.doLookup("jms/queue/Queue");

		c = (Connection) cf.createConnection("root", "root");
		c.start();
		s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public Destination send(String text, Boolean to_temp_queue, Destination temp_destination) {
		Destination replyto = null;

		try {
			TextMessage msg = s.createTextMessage();
			MessageProducer mp;

			if(to_temp_queue)
				mp = s.createProducer(temp_destination);
			else {
				mp = s.createProducer(d);
				replyto = s.createTemporaryQueue();
				msg.setJMSReplyTo(replyto);
			}

			msg.setText(text);
			mp.send(msg);
		} catch (JMSException e) {
			System.out.println("Reply::send Exception");
		}

		return replyto;
	}

	public TextMessage receive(Boolean from_temp_queue, Destination temp_destination) {
		try {
			MessageConsumer mc;

			if(from_temp_queue)
				mc = s.createConsumer(temp_destination);
			else
				mc = s.createConsumer(d);

			return (TextMessage) mc.receive();
		} catch (JMSException e) {
			System.out.println("Reply::receive Exception");
		}

		return null;
	}

	public void close(){
		try {
			c.close();
		} catch (JMSException e) {
			System.out.println("Reply::close Exception");
		}
	}
}
