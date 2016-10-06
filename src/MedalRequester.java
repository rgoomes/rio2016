import java.util.Scanner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public class MedalRequester {

	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);

		while(true){
			try {
				String request = stdin.nextLine();

				if(request.length() > 0){
					Reply reply = new Reply();
					Destination temp_queue = reply.send(request, false, null);
					TextMessage response = reply.receive(true, temp_queue);
					System.out.println(response.getText());
					reply.close();
				}
			} catch(JMSException | NamingException e){
				System.out.println("MedalRequester::main Exception: Exiting..");
				return;
			}
		}
	}
}
