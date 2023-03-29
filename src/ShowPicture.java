import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ShowPicture {
    public static void show() {
        var frame = new JFrame();
        var icon = new ImageIcon("img/IMG_5116.jpg");
        var label = new JLabel(icon);
        frame.add(label);
        frame.pack();
        frame.setVisible(true);
    }
}

