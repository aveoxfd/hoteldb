import javax.swing.*;

public class Main {
    static String mainButtonsNames[] = {
        "Create a new list",
        "Search",
        "View full information"
    };

    static Task taskManager = new Task(10);


    public static void main(String args[]){ //main menu
        JFrame frame = new JFrame("Hostel Database");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);
        frame.setLayout(null);

        JPanel mainMenu = new JPanel();
        mainMenu.setBounds(0,0,400,400);
        mainMenu.setLayout(null);
        mainMenu.setVisible(true);
        taskManager.addTask(mainMenu);
        frame.add(taskManager.getTask());



        int step = 0;
        for (String mainMenuButtonNames : mainButtonsNames){
            JButton button = new JButton(mainMenuButtonNames);
            button.setSize(200,50);
            button.setLocation(100, 50+step*50);

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
            else if(mainMenuButtonNames=="View full information"){
                button.addActionListener(e -> {
                    //TODO
                });
            }

            step++;
            mainMenu.add(button);
        }
    }
    private static void searchRoom(){
        System.out.println("Search opened");
        System.out.println(taskManager.getTaskCount());
        //TODO
    }
    private static void listRedactor(){
        JPanel listRedactorPanel = new JPanel();
        listRedactorPanel.setBounds(0,0,400,400);
        listRedactorPanel.setLayout(null);
        listRedactorPanel.setVisible(true);
        taskManager.addTask(listRedactorPanel);


    }
}
