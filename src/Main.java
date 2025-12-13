
public class Main {
    public static void main(String[] args) {
        IO.println(String.format("Hello and welcome!"));
        NetworkController controller = new NetworkController();
        YapChatGUI gui = new YapChatGUI(controller);

        controller.setGUI(gui); // optional reference back

        gui.setVisible(true);

    }}