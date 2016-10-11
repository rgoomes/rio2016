import java.util.Scanner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public class MedalRequester {

	public static class Response implements Runnable {
		String request;

		Response (String request){
			this.request = request;
		}

		public void run() {
			Reply reply;

			try {
				reply = new Reply();
			} catch(JMSException | NamingException e){
				System.out.println("MedalRequester.Response::main exception: exiting..");
				return;
			}

			Destination temp_queue = reply.send(request, false, null);
			TextMessage response = reply.receive(true, temp_queue);

			try {
				System.out.println(response == null ? /* has timeout been reached? */
						("timeout reached for request: " + request) : response.getText());
			} catch (JMSException e) {
				System.out.println("MedalRequester.Response::main exception: exiting..");
			}

			reply.close();
		}
	}

	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);

		while(true){
			String request = stdin.nextLine();

			if(request.length() > 0){
				Thread t = new Thread(new MedalRequester.Response(request));
				t.start();
			}
		}
	}
}
