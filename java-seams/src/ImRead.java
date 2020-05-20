import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;


/**
 * Simple class for reading/writing image files. 
 * @author jskripchuk and acbart
 *
 */
public class ImRead {
    public static int[][][] readImage(String filename) throws Exception {
        BufferedImage image = ImageIO.read(new File(filename));
        
        int[][][] newImage = new int[image.getHeight()][image.getWidth()][3];
        
        
        for(int i = 0; i < image.getHeight(); i++) {
            for(int j = 0; j < image.getWidth(); j++) {
                Color c = new Color(image.getRGB(j, i));
                
                newImage[i][j][0] = c.getRed();
                newImage[i][j][1] = c.getGreen();
                newImage[i][j][2] = c.getBlue();
                
            }
        }
        
        return newImage;
    }
    
    public static void writeImage(int[][][] imageArr, String outputFilename) throws Exception{
        BufferedImage newImg = new BufferedImage(imageArr[0].length, imageArr.length, BufferedImage.TYPE_INT_RGB);
        
        for(int i = 0; i < newImg.getHeight(); i++) {
            for(int j = 0; j < newImg.getWidth(); j++) {
                int[] rgbVals = imageArr[i][j];
                Color c = new Color(rgbVals[0],rgbVals[1],rgbVals[2]);
                newImg.setRGB(j, i, c.getRGB());
            }
        }
        
        File output = new File(outputFilename);
        ImageIO.write(newImg, "jpg", output);
    }
}
