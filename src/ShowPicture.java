import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ShowPicture {
    public static void show(String name) {
        File imageFile = new File("src/img/" + name);
        BufferedImage img = null;

        try {
            img = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        if(img != null){
            display(img);
        }
    }

    public static void display(BufferedImage img){
        System.out.println("Display image.");
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        frame.setSize(img.getWidth(), img.getHeight());
        label.setIcon(new ImageIcon(img));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}

