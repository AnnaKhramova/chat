import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static class Settings {
        private static Settings instance = null;

        private Settings() {}

        public static Settings get() {
            if (instance == null) instance = new Settings();
            return instance;
        }
        public Integer getPort() {
            String port = parseXML("settings.xml");
            return Integer.parseInt(port);
        }

        private static String parseXML(String fileName) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new File(fileName));
                Element element = doc.getDocumentElement();
                return element.getElementsByTagName("port").item(0).getTextContent();
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("localhost", Settings.get().getPort());
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            writer.println("Anna");
            System.out.println(reader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
