import javax.jms.JMSException;
import javax.naming.NamingException;

public class CreatorHTML {

	public static void main(String args[]) {

		while(true){
			try {
				Subscriber client = new Subscriber("html_creator");
				String xml_file = client.recv();
				System.out.println("XML file received");
				Util.writeXML(xml_file, "html_creator.xml");
				System.out.println("XML file valid? " + (Util.validXML("html_creator.xml") ? "yes" : "no"));
				client.stop();
			} catch(JMSException | NamingException | NullPointerException e){
				System.out.println("Crawler::Crawler exception: WildFly Server or Topic is Down. Exiting..");
				return;
			}
		}
	}
}
