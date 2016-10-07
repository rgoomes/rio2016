import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.xml.sax.SAXException;

public class Util {
	public static String asString(JAXBContext jaxbContext, Body body) {
		StringWriter sw = new StringWriter();

		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(body, sw);
		} catch (JAXBException e) {
			System.out.println("Util::asString exception");
		}

		return sw.toString();
	}

	public static Body unmarshalXMLstring(String xml){
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(xml);
			return (Body) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			System.out.println("Util::unmarshalXMLstring exception");
		}

		return null;
	}

	public static void writeXML(String xml, String out_file){
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Body.class);
			Body body = unmarshalXMLstring(xml);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders",
					"\n<?xml-stylesheet type=\"text/xsl\" href=\"stylesheet.xsl\"?>");
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(body, new File(out_file));
		} catch (JAXBException e) {
			System.out.println("Util::writeXML exception");
		}
	}

	public static Boolean validXML(String xml_str){
		try {
			Source xmlFile = new StreamSource(new StringReader(xml_str));
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new File("medals.xsd"));
			Validator validator = schema.newValidator();

			try {
				validator.validate(xmlFile);
				return true;
			} catch (SAXException | IOException e) {
				return false;
			}
		} catch (SAXException e1) {
			System.out.println("Util::validateXML exception");
		}

		return false;
	}
}
