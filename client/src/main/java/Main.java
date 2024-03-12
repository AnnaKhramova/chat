import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    private static String name = "Неизвестный";

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
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nВыберите действие:\n" +
                    "0. Изменить имя\n" +
                    "1. Подключиться к чату\n" +
                    "2. Завершить\n" +
                    "Ваш выбор: ");
            int command = scanner.nextInt();
            scanner.nextLine();
            switch (command) {
                case 0:
                    System.out.print("Введите имя: ");
                    name = scanner.nextLine();
                    System.out.println("Имя успешно изменено");
                    break;
                case 1:
                    try (Socket clientSocket = new Socket("localhost", Settings.get().getPort());
                         PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ) {
                        while (true) {
                            System.out.print("\nВведите сообщение или \"/exit\" для выхода: ");
                            String comm = scanner.nextLine();
                            if ("/exit".equals(comm)) {
                                break;
                            } else {
                                String message = "[" + DateTimeFormatter.ofPattern("MM-dd-yy hh:mm").format(LocalDateTime.now()) + "] " + name + ": " + comm;
                                writer.println(message);
                                if (createFile("file.log")) {
                                    try (FileWriter fileWriter = new FileWriter("file.log", true)) {
                                        fileWriter.write(message);
                                        writer.flush();
                                    } catch (IOException ex) {
                                        System.out.println(ex.getMessage());
                                    }
                                }
                            }
                            System.out.println(reader.readLine());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case 2:
                    return;
                default:
                    System.out.println("Такой операции нет");
                    break;
            }
        }
    }

    public static boolean createFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    return true;
                }
            } catch (IOException ex) {
                System.out.println("Ошибка: Не удалось записать лог");
            }
        }
        return false;
    }
}
