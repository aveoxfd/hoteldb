import javax.swing.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Main {
    static String mainButtonsNames[] = {
        "Create a new list",
        //"Edit an entry",
        //"Search",
        "View full DB"
    };

    private static int width = 500;
    private static int height = 500;
    private static JFrame frame = new JFrame("Hotelbd");
    private static Task taskManager = new Task(frame, 10);
    private static List<HotelList> hotelDatabase = new ArrayList<>();
    

    public static void main(String args[]){ //main menu
        // load DB from CSV if available
        try {
            java.nio.file.Path dbFile = Paths.get("db").resolve("hotels.csv");
            if (Files.exists(dbFile)) {
                hotelDatabase = CSVbd.load(dbFile.toString());
                System.out.println("Loaded " + hotelDatabase.size() + " records from db/hotels.csv");
            }
        } catch (IOException ex) {
            System.err.println("Failed to load DB CSV: " + ex.getMessage());
            ex.printStackTrace();
        }

        mainMenu();
    }
    private static void mainMenu(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width,height);
        frame.setVisible(true);
        frame.setLayout(null);

        JPanel mainMenu = new JPanel();
        mainMenu.setBounds(0,0,width,height);
        mainMenu.setLayout(null);
        mainMenu.setVisible(true);
        taskManager.addTask(mainMenu);
        frame.add(taskManager.getTask());

        int step = 0;
        for (String mainMenuButtonNames : mainButtonsNames){
            JButton button = new JButton(mainMenuButtonNames);
            button.setSize(200,50);
            button.setLocation(width/2 - 100, 50+step*50);

            if(mainMenuButtonNames=="Create a new list"){
                button.addActionListener(e -> {
                    taskManager.getTask().setVisible(false);
                    listRedactor();
                });
            }
            else if(mainMenuButtonNames=="Search"){
                button.addActionListener(e -> {
                    searchRoom();
                });
            }
            else if(mainMenuButtonNames=="View full DB"){
                button.addActionListener(e -> {
                    // build table model from in-memory DB
                    String[] cols = new String[]{
                        "ID","HotelName","City","AddressName","StreetNumber","HouseNumber","DoorNumber",
                        "AdminFirst","AdminSecond","AdminMiddle","AdminPhone","AdminPost",
                        "DirectorFirst","DirectorSecond","DirectorMiddle","DirectorPhone","DirectorPost"
                    };

                    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return true; // allow inline editing
                        }
                    };

                    for (HotelList h : hotelDatabase) {
                        if (h == null) continue;
                        String id = h.ID == null ? "" : h.ID;
                        String hotelName = h.hotelName == null ? "" : h.hotelName;
                        String city = h.hotelAddress != null && h.hotelAddress.cityName != null ? h.hotelAddress.cityName : "";
                        String addrName = h.hotelAddress != null && h.hotelAddress.addressName != null ? h.hotelAddress.addressName : "";
                        String streetNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.streetNumber) : "";
                        String houseNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.houseNumber) : "";
                        String doorNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.doorNumber) : "";

                        Person a = h.administator;
                        Person d = h.director;
                        String aFirst = a != null && a.firstName != null ? a.firstName : "";
                        String aSecond = a != null && a.secondName != null ? a.secondName : "";
                        String aMiddle = a != null && a.middleName != null ? a.middleName : "";
                        String aPhone = a != null && a.phoneNumber != null ? a.phoneNumber : "";
                        String aPost = a != null && a.post != null ? a.post : "";

                        String dFirst = d != null && d.firstName != null ? d.firstName : "";
                        String dSecond = d != null && d.secondName != null ? d.secondName : "";
                        String dMiddle = d != null && d.middleName != null ? d.middleName : "";
                        String dPhone = d != null && d.phoneNumber != null ? d.phoneNumber : "";
                        String dPost = d != null && d.post != null ? d.post : "";

                        model.addRow(new Object[]{
                            id, hotelName, city, addrName, streetNumber, houseNumber, doorNumber,
                            aFirst, aSecond, aMiddle, aPhone, aPost,
                            dFirst, dSecond, dMiddle, dPhone, dPost
                        });
                    }

                    JFrame viewFrame = new JFrame("Full DB - " + hotelDatabase.size() + " records");
                    viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    viewFrame.setSize(900, 400);
                    viewFrame.setLocationRelativeTo(frame);

                    JTable table = new JTable(model);
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    JScrollPane scroll = new JScrollPane(table);
                    viewFrame.add(scroll, java.awt.BorderLayout.CENTER);


                    JPanel bottom = new JPanel();
                    JButton deleteBtn = new JButton("Delete Selected");
                    deleteBtn.addActionListener(ae -> {
                        int sel = table.getSelectedRow();
                        if (sel >= 0) {
                            ((javax.swing.table.DefaultTableModel) table.getModel()).removeRow(sel);
                            // rebuild database from table so indexes stay consistent
                            rebuildDatabaseFromTable(table);
                        } else {
                            JOptionPane.showMessageDialog(viewFrame, "Select a row to delete.");
                        }
                    });

                    JButton applyBtn = new JButton("Save Changes");
                    applyBtn.addActionListener(ae -> {
                        rebuildDatabaseFromTable(table);
                        try {
                            Path dbDir = Paths.get("db");
                            if (!Files.exists(dbDir)) Files.createDirectories(dbDir);
                            CSVbd.save(hotelDatabase, dbDir.resolve("hotels.csv").toString());
                            JOptionPane.showMessageDialog(viewFrame, "Saved " + hotelDatabase.size() + " records to db/hotels.csv");
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(viewFrame, "Failed to save CSV: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });

                    JButton saveBtn = new JButton("Save CSV");
                    saveBtn.addActionListener(ae -> {
                        // ensure table edits are captured before saving
                        rebuildDatabaseFromTable(table);
                        try {
                            Path dbDir = Paths.get("db");
                            if (!Files.exists(dbDir)) Files.createDirectories(dbDir);
                            CSVbd.save(hotelDatabase, dbDir.resolve("hotels.csv").toString());
                            JOptionPane.showMessageDialog(viewFrame, "Saved " + hotelDatabase.size() + " records to db/hotels.csv");
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(viewFrame, "Failed to save CSV: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });

                    bottom.add(deleteBtn);
                    bottom.add(applyBtn);
                    bottom.add(saveBtn);
                    viewFrame.add(bottom, java.awt.BorderLayout.SOUTH);

                    viewFrame.setVisible(true);
                });
            }
            else if (mainMenuButtonNames=="Edit an entry"){
                button.addActionListener(e -> {
                    //TODO
                });
            }

            step++;
            taskManager.getTask().add(button);
        }
    }
    private static void searchRoom(){
        System.out.println("Search opened");
        System.out.println(taskManager.getTaskCount());
        //TODO
    }
    private static void listRedactor(){
        String listRedactorButtons[] = {
            "<-",
            "Done",
            "View field"
        };
        Person PersonTempBuff[] = new Person[10];
        final int[] PersonTempBuffPtr = {0};

        JPanel listRedactorPanel = new JPanel();
        taskManager.addTask(listRedactorPanel);
        taskManager.getTask().setBounds(0,0,width, height);
        taskManager.getTask().setLayout(null);
        taskManager.getTask().setVisible(true);

        int yPosition = 10;
        int jump = 25;

        JTextField idField = new JTextField();
        idField.setBounds(150, yPosition, 200, 25);
        JLabel idLabel = new JLabel("ID:");
        idLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(idLabel);
        taskManager.getTask().add(idField);
        yPosition += jump;
        
        JTextField hostelNameField = new JTextField();
        hostelNameField.setBounds(150, yPosition, 200, 25);
        JLabel hostelNameLabel = new JLabel("Hotel Name:");
        hostelNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(hostelNameLabel);
        taskManager.getTask().add(hostelNameField);
        yPosition += jump;

        JTextField cityNameField = new JTextField();
        cityNameField.setBounds(150, yPosition, 200, 25);
        JLabel cityNameLabel = new JLabel("City:");
        cityNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(cityNameLabel);
        taskManager.getTask().add(cityNameField);
        yPosition += jump;

        JTextField streetNameField = new JTextField();
        streetNameField.setBounds(150, yPosition, 200, 25);
        JLabel streetNameLabel = new JLabel("St. name:");
        streetNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(streetNameLabel);
        taskManager.getTask().add(streetNameField);
        yPosition += jump;

        JTextField streetNumberField = new JTextField();
        streetNumberField.setBounds(150, yPosition, 200, 25);
        JLabel streetNumberLabel = new JLabel("St. number:");
        streetNumberLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(streetNumberLabel);
        taskManager.getTask().add(streetNumberField);
        yPosition += jump;

        JTextField houseNumberField = new JTextField();
        houseNumberField.setBounds(150, yPosition, 200, 25);
        JLabel houseNumberLabel = new JLabel("House number:");
        houseNumberLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(houseNumberLabel);
        taskManager.getTask().add(houseNumberField);
        yPosition += jump;

        JTextField doorNumberField = new JTextField();
        doorNumberField.setBounds(150, yPosition, 200, 25);
        JLabel doorNumberLabel = new JLabel("Door number:");
        doorNumberLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(doorNumberLabel);
        taskManager.getTask().add(doorNumberField);
        yPosition += jump;

        JTextField PersonFNameField = new JTextField();
        PersonFNameField.setBounds(150, yPosition, 200, 25);
        JLabel PersonFNameLabel = new JLabel("Person first Name:");
        PersonFNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(PersonFNameLabel);
        taskManager.getTask().add(PersonFNameField);
        yPosition += jump;

        JTextField PersonSNameField = new JTextField();
        PersonSNameField.setBounds(150, yPosition, 200, 25);
        JLabel PersonSNameLabel = new JLabel("Person last Name:");
        PersonSNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(PersonSNameLabel);
        taskManager.getTask().add(PersonSNameField);
        yPosition += jump;

        JTextField PersonMNameField = new JTextField();
        PersonMNameField.setBounds(150, yPosition, 200, 25);
        JLabel PersonMNameLabel = new JLabel("Person mid Name:");
        PersonMNameLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(PersonMNameLabel);
        taskManager.getTask().add(PersonMNameField);
        yPosition += jump;

        JTextField PersonPhoneNumberField = new JTextField();
        PersonPhoneNumberField.setBounds(150, yPosition, 200, 25);
        JLabel PersonPhoneNumberLabel = new JLabel("Person ph number:");
        PersonPhoneNumberLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(PersonPhoneNumberLabel);
        taskManager.getTask().add(PersonPhoneNumberField);
        yPosition += jump;

        JTextField PersonPostField = new JTextField();
        PersonPostField.setBounds(150, yPosition, 200, 25);
        JLabel PersonPostLabel = new JLabel("Person post:");
        PersonPostLabel.setBounds(50, yPosition, 100, 25);
        taskManager.getTask().add(PersonPostLabel);
        taskManager.getTask().add(PersonPostField);
        yPosition += jump;




        JButton addPersonToArray = new JButton("Add person");
        addPersonToArray.setSize(70, 20);
        addPersonToArray.setLocation(width/2-70/2, yPosition);
        addPersonToArray.addActionListener(e->{

            PersonTempBuff[PersonTempBuffPtr[0]%10] = new Person();

            PersonTempBuff[PersonTempBuffPtr[0]%10].firstName = PersonFNameField.getText().trim().isEmpty() ? "null" : PersonFNameField.getText().trim();
            PersonTempBuff[PersonTempBuffPtr[0]%10].secondName = PersonSNameField.getText().trim().isEmpty() ? "null" : PersonSNameField.getText().trim();
            PersonTempBuff[PersonTempBuffPtr[0]%10].middleName = PersonMNameField.getText().trim().isEmpty() ? "null" : PersonMNameField.getText().trim();
            PersonTempBuff[PersonTempBuffPtr[0]%10].phoneNumber = PersonPhoneNumberField.getText().trim().isEmpty() ? "null" : PersonPhoneNumberField.getText().trim();
            PersonTempBuff[PersonTempBuffPtr[0]%10].post = PersonPostField.getText().trim().isEmpty() ? "null" : PersonPostField.getText().trim();
            
            System.out.println(
                     PersonTempBuff[PersonTempBuffPtr[0]%10].firstName +
                "\n"+PersonTempBuff[PersonTempBuffPtr[0]%10].secondName+
                "\n"+PersonTempBuff[PersonTempBuffPtr[0]%10].middleName+
                "\n"+PersonTempBuff[PersonTempBuffPtr[0]%10].phoneNumber+
                "\n"+PersonTempBuff[PersonTempBuffPtr[0]%10].post
            );
            

            PersonFNameField.setText("");
            PersonSNameField.setText("");
            PersonMNameField.setText("");
            PersonPhoneNumberField.setText("");
            PersonPostField.setText("");

            PersonTempBuffPtr[0]++;
        });
        yPosition+=jump;


        int step = 0;
        for (String listRedactorButtonsParser : listRedactorButtons){
            JButton button = new JButton(listRedactorButtonsParser);

            if(listRedactorButtonsParser == "<-"){
                button.setSize(45, 20);
                button.setLocation(0, 0);

                button.addActionListener(e -> {
                    taskManager.getTask().setVisible(false);
                    taskManager.removeTask();
                    mainMenu();
                });
            }
            else if (listRedactorButtonsParser == "Done"){
                button.setSize(200, 10);
                button.setLocation(width/2 - 100, height-50);

                button.addActionListener(e -> {

                    HotelList HotelPack = new HotelList();
                    HotelPack.ID = idField.getText().trim().isEmpty() ? "null" : idField.getText().trim();
                    HotelPack.administator = PersonTempBuff[0%10];
                    HotelPack.director = PersonTempBuff[1%10];
                    HotelPack.hotelName = hostelNameField.getText().trim().isEmpty() ? "null" : hostelNameField.getText().trim();
                    HotelPack.hotelAddress = new Address();
                    HotelPack.hotelAddress.cityName = cityNameField.getText().trim().isEmpty() ? "null" : cityNameField.getText().trim();
                    HotelPack.hotelAddress.addressName = streetNameField.getText().trim().isEmpty() ? "null" : streetNameField.getText().trim();
                    HotelPack.hotelAddress.streetNumber = streetNumberField.getText().trim().isEmpty() ? 0 : Integer.parseInt(streetNumberField.getText().trim());
                    HotelPack.hotelAddress.houseNumber = houseNumberField.getText().trim().isEmpty() ? 0 : Integer.parseInt(houseNumberField.getText().trim());
                    HotelPack.hotelAddress.doorNumber = doorNumberField.getText().trim().isEmpty() ? 0 : Integer.parseInt(doorNumberField.getText().trim());

                    // persist to in-memory DB and save CSV
                    hotelDatabase.add(HotelPack);
                    try {
                        Path dbDir = Paths.get("db");
                        if (!Files.exists(dbDir)) Files.createDirectories(dbDir);
                        CSVbd.save(hotelDatabase, dbDir.resolve("hotels.csv").toString());
                    } catch (IOException ex) {
                        System.err.println("Failed to auto-save CSV: " + ex.getMessage());
                        ex.printStackTrace();
                    }

                    System.out.println(
                        HotelPack.ID +
                        "\n"+HotelPack.hotelName+
                        "\n"+HotelPack.hotelAddress.cityName+
                        "\n"+HotelPack.hotelAddress.addressName+
                        "\n"+HotelPack.hotelAddress.streetNumber+
                        "\n"+HotelPack.hotelAddress.houseNumber+
                        "\n"+HotelPack.hotelAddress.doorNumber+
                        "\n"+"Administator:"+HotelPack.administator.firstName+" "+HotelPack.administator.secondName+
                        "\n"+"Director:"+HotelPack.director.firstName+" "+HotelPack.director.secondName
                    );

                    

                    taskManager.getTask().setVisible(false);
                    taskManager.removeTask();
                    mainMenu();
                });
            }
            else if (listRedactorButtonsParser == "View field"){
                button.setSize(60, 10);
                button.setLocation(width - 100, height-50);
                button.addActionListener(e ->{
                    JFrame viewFieldFrame = new JFrame("View field");
                    viewFieldFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    viewFieldFrame.setSize(300,400);
                    viewFieldFrame.setVisible(true);
                    JTextArea viewFieldArea = new JTextArea();
                    viewFieldArea.setEditable(false);
                    viewFieldFrame.add(viewFieldArea);
                    StringBuilder fieldData = new StringBuilder();
                    fieldData.append("ID: ").append(idField.getText().trim().isEmpty() ? "null" : idField.getText().trim()).append("\n");
                    fieldData.append("Hotel Name: ").append(hostelNameField.getText().trim().isEmpty() ? "null" : hostelNameField.getText().trim()).append("\n");
                    fieldData.append("City: ").append(cityNameField.getText().trim().isEmpty() ? "null" : cityNameField.getText().trim()).append("\n");
                    fieldData.append("Street Name: ").append(streetNameField.getText().trim().isEmpty() ? "null" : streetNameField.getText().trim()).append("\n");
                    fieldData.append("Street Number: ").append(streetNumberField.getText().trim().isEmpty() ? "null" : streetNumberField.getText().trim()).append("\n");
                    fieldData.append("House Number: ").append(houseNumberField.getText().trim().isEmpty() ? "null" : houseNumberField.getText().trim()).append("\n");
                    fieldData.append("Door Number: ").append(doorNumberField.getText().trim().isEmpty() ? "null" : doorNumberField.getText().trim()).append("\n");
                    fieldData.append("Person first Name: ").append(PersonFNameField.getText().trim().isEmpty() ? "null" : PersonFNameField.getText().trim()).append("\n");
                    fieldData.append("Person last Name: ").append(PersonSNameField.getText().trim().isEmpty() ? "null" : PersonSNameField.getText().trim()).append("\n");
                    fieldData.append("Person mid Name: ").append(PersonMNameField.getText().trim().isEmpty() ? "null" : PersonMNameField.getText().trim()).append("\n");
                    fieldData.append("Person ph number: ").append(PersonPhoneNumberField.getText().trim().isEmpty() ? "null" : PersonPhoneNumberField.getText().trim()).append("\n");
                    fieldData.append("Person post: ").append(PersonPostField.getText().trim().isEmpty() ? "null" : PersonPostField.getText().trim()).append("\n");
                    viewFieldArea.setText(fieldData.toString());
                });
            }
            else{
                button.setSize(200,50);
                button.setLocation(100, 50+step*50);
            }
            step++;
            taskManager.getTask().add(button);
        }

        taskManager.getTask().add(addPersonToArray);
        
        frame.add(taskManager.getTask());
    }

    // helpers used by DB viewer
    private static String toStr(Object o) {
        return o == null ? "" : o.toString();
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty()) return 0;
        try { return Integer.parseInt(t); } catch (Exception ex) { return 0; }
    }

    private static boolean hasPersonData(Person p) {
        if (p == null) return false;
        return (p.firstName != null && !p.firstName.isEmpty()) || (p.secondName != null && !p.secondName.isEmpty()) || (p.phoneNumber != null && !p.phoneNumber.isEmpty()) || (p.post != null && !p.post.isEmpty()) || (p.middleName != null && !p.middleName.isEmpty());
    }

    private static void rebuildDatabaseFromTable(JTable table) {
        javax.swing.table.TableModel tm = table.getModel();
        int rows = tm.getRowCount();
        List<HotelList> newList = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            HotelList h = new HotelList();
            h.ID = toStr(tm.getValueAt(r, 0));
            h.hotelName = toStr(tm.getValueAt(r, 1));
            h.hotelAddress = new Address();
            h.hotelAddress.cityName = toStr(tm.getValueAt(r, 2));
            h.hotelAddress.addressName = toStr(tm.getValueAt(r, 3));
            h.hotelAddress.streetNumber = parseIntSafe(toStr(tm.getValueAt(r, 4)));
            h.hotelAddress.houseNumber = parseIntSafe(toStr(tm.getValueAt(r, 5)));
            h.hotelAddress.doorNumber = parseIntSafe(toStr(tm.getValueAt(r, 6)));

            Person a = new Person();
            a.firstName = toStr(tm.getValueAt(r, 7));
            a.secondName = toStr(tm.getValueAt(r, 8));
            a.middleName = toStr(tm.getValueAt(r, 9));
            a.phoneNumber = toStr(tm.getValueAt(r, 10));
            a.post = toStr(tm.getValueAt(r, 11));
            h.administator = (hasPersonData(a) ? a : null);

            Person d = new Person();
            d.firstName = toStr(tm.getValueAt(r, 12));
            d.secondName = toStr(tm.getValueAt(r, 13));
            d.middleName = toStr(tm.getValueAt(r, 14));
            d.phoneNumber = toStr(tm.getValueAt(r, 15));
            d.post = toStr(tm.getValueAt(r, 16));
            h.director = (hasPersonData(d) ? d : null);

            newList.add(h);
        }
        hotelDatabase = newList;
    }
}
