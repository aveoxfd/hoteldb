import javax.swing.JPanel;

public class Task {
    private JPanel taskMemory[];
    private int taskCount = 0;
    Task(int size){
        taskMemory = new JPanel[size];
    }
    Task(){
        taskMemory = new JPanel[10];
    }
    public void addTask(JPanel task, int index){
        taskMemory[index > 0 -1 ? taskCount++ : index] = task;
    }
    public void addTask(JPanel task){
        taskMemory[taskCount++] = task;
        System.out.println("Task added, current task count: " + taskCount);
    }
    public void removeTask(int index){
        taskMemory[index > 0 ? --taskCount : index] = null;
    }
    public void removeTask(){
        if (taskCount > 0) {
            taskMemory[--taskCount] = null;
        }
    }
    public JPanel getTask(int index){
        if (index >= 0 && index < taskCount) {
            return taskMemory[index-1];
        }
        return null;
    }
    public JPanel getTask(){
        if (taskCount > 0) {
            return taskMemory[taskCount - 1];
        }
        return null;
    }
    public int getTaskCount() {
        return taskCount;
    }
    public void setConditionTask(int index, boolean condition){
        if (index >= 0 && index < taskCount) {
            taskMemory[index].setVisible(condition);
        }
    }
    public void setConditionTask(boolean condition){
        if (taskCount >= 0 && taskCount < taskCount) {
            taskMemory[taskCount].setVisible(condition);
        }
    }  
}

