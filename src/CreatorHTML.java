import javax.jms.JMSException;
import javax.jms.InvalidClientIDRuntimeException;
import javax.naming.NamingException;

public class CreatorHTML {

	public static void main(String args[]) {
		try {
			Subscriber subscriber = new Subscriber("html_summary_creator");

			while(true){
				String xml_file = subscriber.recv();
				Boolean xmlValid = Util.validXML(xml_file);
				System.out.println((xmlValid ? "Valid" : "Invalid") + " XML file received");

				if(xmlValid)
					Util.writeXML(xml_file, "html" + String.valueOf(System.currentTimeMillis()) + ".xml");
			}
		} catch(JMSException | NamingException | NullPointerException e1){
			System.out.println("CreatorHTML::main exception: wildfly server or topic are down. exiting..");
			return;
		} catch(InvalidClientIDRuntimeException e2){
			System.out.println("CreatorHTML::main exception: your clientid is in use. please wait..");
			return;
		}
	}
}
