import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ShowPicture {
    public static void show(String name) throws IIOException{

        try {
            File imageFile = new File("src/img/" + name);
            BufferedImage img = null;
            img = ImageIO.read(imageFile);
            if(img != null){
                display(img);
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void display(BufferedImage img){
        System.out.println("Display image.");
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        frame.setSize(1280, 720);
        label.setIcon(new ImageIcon(img));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}

