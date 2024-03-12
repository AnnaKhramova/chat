import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final String SETTINGS_FILE = "settings.xml";

    public static class Settings {
        private static Settings instance = null;

        private Settings() {
        }

        public static Settings get() {
            if (instance == null) instance = new Settings();
            return instance;
        }

        public void setPort(String fileName, Integer port) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new File(fileName));
                Element element = doc.getDocumentElement();
                Node node = element.getElementsByTagName("port").item(0);
                node.setTextContent(port.toString());
                DOMSource domSource = new DOMSource(doc);
                StreamResult streamResult = new StreamResult(new File(fileName));
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(domSource, streamResult);
            } catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
                e.printStackTrace();
            }
        }

        public Integer getPort() {
            String port = parseXML(SETTINGS_FILE);
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
        Settings.get().setPort(SETTINGS_FILE, 8080);
        try (ServerSocket serverSocket = new ServerSocket(Settings.get().getPort())) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    final String clientName = in.readLine();
                    log(clientName + " подключился. Порт: " + clientSocket.getPort() + "\n");
                    while (true) {
                        final String message = in.readLine();
                        if ("exit".equals(message)) {
                            log(clientName + " вышел из чата.\n");
                            break;
                        }
                        log(message);
                        System.out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void log(String log) {
        createDir("log");
        if (createDir("log/server")) {
            if (createFile("log/server/file.log")) {
                try (FileWriter fileWriter = new FileWriter("log/server/file.log", true)) {
                    fileWriter.write(log);
                    fileWriter.flush();
                } catch (IOException ex) {
                    System.out.println("Ошибка: Не удалось записать лог");
                }
            }
        }
    }

    public static boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()){
            if (dir.mkdir()) {
                return true;
            } else {
                System.out.println("Ошибка: Директория не создана");
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean createFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    return true;
                }
            } else {
                return true;
            }
        } catch (IOException ex) {
            System.out.println("Ошибка: Не удалось создать файл логирования");
        }
        return false;
    }
}
