import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.InvalidClientIDRuntimeException;
import javax.naming.NamingException;
import java.util.concurrent.Semaphore;

public class MedalKeeper {

	static Body body = null;
	static Semaphore mutex = new Semaphore(1);

	public static class CrawlerListener implements Runnable {
		Boolean failed = false;

		public void run() {
			try{
				Subscriber client = new Subscriber("medal_keeper");

				while(true){
					String xml_file = client.recv();
					Boolean xmlValid = Util.validXML(xml_file);
					System.out.println((xmlValid ? "Valid" : "Invalid") + " XML file received");

					// mutex
					try {
						mutex.acquire();
						try {
							if(xmlValid)
								body = Util.unmarshallXML(xml_file);
							else
								// FIXME: not sure if it is supposed to invalidate old valid XML files
								body = null;
						} finally {
							mutex.release();
						}
					} catch(InterruptedException ie) {}
				}
			} catch(JMSException | NamingException | NullPointerException e1){
				System.out.println("CrawlerListener::run exception: wildfly server is down. exiting..");
				this.failed = true;
			} catch(InvalidClientIDRuntimeException e2){
				System.out.println("CrawlerListener::run exception: your clientid is in use. wait..");
				this.failed = true;
			}
		}
	}

	public static class RequesterListener implements Runnable {

		public int getTotalMedalsByCountry(String country_str, Boolean is_code){
			List<Body.Country> countries_list = body.getCountry();

			for(int i = 0; i < countries_list.size(); i++){
				Body.Country country = countries_list.get(i);

				if( (is_code && country.getCode().toLowerCase().equals(country_str.toLowerCase()))
						|| (!is_code && country.getName().toLowerCase().equals(country_str.toLowerCase())) )
					return country.getMedal().size();
			}

			return 0;
		}

		public int getTotalMedalsByTypeByCountry(String country_str, String type, Boolean is_code){
			List<Body.Country> countries_list = body.getCountry();

			for(int i = 0; i < countries_list.size(); i++){
				Body.Country country = countries_list.get(i);

				if( (is_code && country.getCode().toLowerCase().equals(country_str.toLowerCase()))
						|| (!is_code && country.getName().toLowerCase().equals(country_str.toLowerCase())) ){

					int total_medals_by_type = 0;
					for(int j = 0; j < country.getMedal().size(); j++){
						Body.Country.Medal medal = country.getMedal().get(j);
						if(medal.getType().toLowerCase().equals(type))
							total_medals_by_type++;
					}

					return total_medals_by_type;
				}
			}

			return 0;
		}

		public String getMedalistsByCountry(String country_str, String type, Boolean is_code){
			List<Body.Country> countries_list = body.getCountry();

			for(int i = 0; i < countries_list.size(); i++){
				Body.Country country = countries_list.get(i);

				if( (is_code && country.getCode().toLowerCase().equals(country_str.toLowerCase()))
						|| (!is_code && country.getName().toLowerCase().equals(country_str.toLowerCase())) ){

					String medalists = "";
					for(int j = 0; j < country.getMedal().size(); j++){
						Body.Country.Medal medal = country.getMedal().get(j);
						medalists += "\n" + medal.getAthlete();
					}

					return medalists;
				}
			}

			return "";
		}

		public String getMedalistsByTypeByCountry(String country_str, String type, Boolean is_code){
			List<Body.Country> countries_list = body.getCountry();

			for(int i = 0; i < countries_list.size(); i++){
				Body.Country country = countries_list.get(i);

				if( (is_code && country.getCode().toLowerCase().equals(country_str.toLowerCase()))
						|| (!is_code && country.getName().toLowerCase().equals(country_str.toLowerCase())) ){

					String medalists = "";
					for(int j = 0; j < country.getMedal().size(); j++){
						Body.Country.Medal medal = country.getMedal().get(j);
						if(medal.getType().toLowerCase().equals(type))
							medalists += "\n" + medal.getAthlete();
					}

					return medalists;
				}
			}

			return "";
		}

		public String getMedalsListByCountry(String country_str, Boolean is_code){
			List<Body.Country> countries_list = body.getCountry();

			for(int i = 0; i < countries_list.size(); i++){
				Body.Country country = countries_list.get(i);

				if( (is_code && country.getCode().toLowerCase().equals(country_str.toLowerCase()))
						|| (!is_code && country.getName().toLowerCase().equals(country_str.toLowerCase())) ){

					String medalists = "";
					for(int j = 0; j < country.getMedal().size(); j++){
						Body.Country.Medal medal = country.getMedal().get(j);
						medalists += "\n" + medal.getType() + " - " + medal.getAthlete() + " - " + medal.getSport() + " - " + medal.getCategory();
					}

					return medalists;
				}
			}

			return "";
		}

		public String request_selector(String request){
			String[] tokens = request.split(" ");

			if(tokens.length <= 0)
				return "Invalid request.";

			else if(tokens.length == 1)

				// input : USA
				// output: (total number of medals)
				return request + " total medals: " + getTotalMedalsByCountry(request, tokens[0].length() == 3);

			else if(tokens.length == 2)

				// input : USA medalists
				// output: (a list of athletes/teams)
				if(tokens[1].toLowerCase().equals("medalists"))
					return tokens[0] + " medalists: "
						+ getMedalistsByCountry(tokens[0], tokens[1], tokens[0].length() == 3);

				// input : USA medals
				// output: (a list of USA medals)
				else if(tokens[1].toLowerCase().equals("medals"))
					return tokens[0] + " medals: "
						+ getMedalsListByCountry(tokens[0], tokens[0].length() == 3);

				// input : USA gold
				// output: (total number of gold medals)
				else
					return tokens[0] + " total " + tokens[1] + " medals: "
						+ getTotalMedalsByTypeByCountry(tokens[0], tokens[1], tokens[0].length() == 3);

			else if(tokens.length == 3 && tokens[2].toLowerCase().equals("medalists"))

				// input : USA bronze medalists
				// output: (a list of USA athletes/teams that got a bronze medal)
				return tokens[0] + " " + tokens[1] + " medalists: "
					+ getMedalistsByTypeByCountry(tokens[0], tokens[1], tokens[0].length() == 3);

			return "Invalid request.";
		}

		public void run() {
			while(true){
				try {
					String response = "";
					Reply reply = new Reply();
					TextMessage request = reply.receive(false, null);
					System.out.println("Request: " + request.getText());

					// mutex
					try {
						mutex.acquire();
						try {
							if(body != null)
								response = request_selector(request.getText()) + "\n";
							else
								response = "";
						} finally {
							mutex.release();
						}
					} catch(InterruptedException ie) {}

					reply.send(response, true, request.getJMSReplyTo());
					try { Thread.sleep(100); } catch (InterruptedException e) {}
					reply.close();
				} catch(NamingException | JMSException | NullPointerException e){
					System.out.println("RequesterListener::run exception: wildfly server is down. exiting..");
					return;
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		MedalKeeper.CrawlerListener mcl = new MedalKeeper.CrawlerListener();
		Thread t1 = new Thread(mcl);
		t1.start();

		MedalKeeper.RequesterListener mrl = new MedalKeeper.RequesterListener();
		Thread t2 = new Thread(mrl);
		t2.start();

		// join thread to wait for it to complete
		t1.join();

		// if thread 1 failed for some reason don't wait for thread 2
		if(mcl.failed == true)
			System.exit(-1);

		// join thread to wait for it to complete
		t2.join();
	}
}
