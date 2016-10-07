import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class Crawler {

	public Crawler() {}

	public void sendXML(Body body) {
		Publisher client = null;
		Boolean retry = false;
		int wait_secs = 10;

		do {
			try {
				client = new Publisher();
				retry = false;
			} catch (NamingException e1) {
				System.out.println("Crawler::sendXML exception: wildFly server or topic are down. waiting..");
				try { Thread.sleep(wait_secs * 1000); } catch (InterruptedException e2) {}
				wait_secs += wait_secs;
				retry = true;
			}
		} while(retry);

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
			String xml_msg = Util.asString(jaxbContext, body);
			client.send(xml_msg);
		} catch (JAXBException e) {
			System.out.println("Crawler::sendXML exception");
		}
	}

	public Body getXML(Document doc) {
		Elements medals = doc.select(".table-medals");
		Elements countries = doc.select(".table-medal-countries__link-table");
		ArrayList<String> codes = new ArrayList<String>(), names = new ArrayList<String>();

		for (Element row : countries) {
			Element code = row.select("td").get(1);
			Element name = row.select("td").get(2);

			codes.add(code.text());
			names.add(name.text());
		}

		Body body = new Body();

		String type = "";
		String[] medal_types = { "Gold", "Silver", "Bronze" };

		for (int i = 0; i < medals.size(); i++) {
			Element table = medals.get(i);

			Body.Country country = new Body.Country();
			country.setCode(codes.get(i));
			country.setName(names.get(i));

			for (Element row : table.select("tr")) {
				Body.Country.Medal medal = new Body.Country.Medal();
				Elements tds = row.select("td");

				// has medal type changed?
				String medal_type_field = tds.get(0).text();
				if (Arrays.asList(medal_types).contains(medal_type_field))
					type = medal_type_field;

				medal.setType(type);
				medal.setSport(tds.get(1).text());
				medal.setCategory(tds.get(2).text());
				medal.setAthlete(tds.get(3).text());

				country.getMedal().add(medal);
			}

			body.getCountry().add(country);
		}

		return body;
	}

	public Document getDocument() {
		try {
			return Jsoup.connect("https://www.rio2016.com/en/medal-count-country").get();
		} catch (IOException e) {
			System.out.println("Crawler::getDocument exception: exiting..");
		}

		return null;
	}

	public static void main(String[] args){
		Crawler cl = new Crawler();
		Document doc = cl.getDocument();

		if(doc == null)
			System.out.println("FAIL");
		else {
			Body body = cl.getXML(doc);
			cl.sendXML(body);
			System.out.println("OK");
		}
	}
}
