import java.util.List;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public class MedalKeeper {

	static Body body = null;

	public static class CrawlerListener implements Runnable {
		@Override
		public void run() {
			try{
				Subscriber client = new Subscriber("medal_keeper");

				while(true){
					String xml_file = client.recv();
					System.out.println("XML file received");
					Util.writeXML(xml_file, "keeper.xml");
					Boolean xmlValid = Util.validXML("keeper.xml");
					System.out.println("XML file valid? " + (xmlValid ? "yes" : "no"));

					if(xmlValid)
						body = Util.unmarshalXMLstring(xml_file);
					else
						body = null;
				}
			} catch(JMSException | NamingException | NullPointerException e){
				System.out.println("CrawlerListener::run exception: WildFly Server is down. Exiting..");
				return;
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

				// input : USA gold
				// output: (total number of gold medals)
				else
					return tokens[0] + " total " + tokens[1] + " medals: "
						+ getTotalMedalsByTypeByCountry(tokens[0], tokens[1], tokens[0].length() == 3);

			else if(tokens.length == 3 && tokens[2].toLowerCase().equals("medalists"))

				// input : USA gold medalists
				// output: (a list of USA athletes/teams that got a gold medal)
				return tokens[0] + " " + tokens[1] + " medalists: "
					+ getMedalistsByTypeByCountry(tokens[0], tokens[1], tokens[0].length() == 3);

			return "Invalid request.";
		}

		@Override
		public void run() {
			while(true){
				try {
					Reply reply = new Reply();
					TextMessage request = reply.receive(false, null);
					System.out.println("Request: " + request.getText());

					String response;
					if(body != null)
						response = request_selector(request.getText());
					else
						response = "";

					reply.send(response + "\n", true, request.getJMSReplyTo());
					try { Thread.sleep(1000); } catch (InterruptedException e) {}
					reply.close();
				} catch(NamingException | JMSException | NullPointerException e){
					System.out.println("RequesterListener::run exception: WildFly Server is down. Exiting..");
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

		t1.join();
		t2.join();
	}
}
