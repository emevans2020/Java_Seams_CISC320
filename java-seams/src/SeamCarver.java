/**
 * A class for carving out low energy seams from images.
 * @author acbart and jskripchuk
 *
 */
public class SeamCarver {

    private int width;
    private int height;
    public int[][][] image;
    private int[][] energies;
    private int[][] seams;
    private int[][] backs;

    /**
     * Main method that executes the 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        String inputFilename = "landscape.jpg";
        String inputFilename = "surfer.jpg";

        String outputFilename = "surfer50.jpg";
//        int newN = 600;
        
        int[][][] img = ImRead.readImage(inputFilename);
        System.out.println(img.length); //First dim is height
        System.out.println(img[0].length); //Second dim is width
        System.out.println(img[0][0].length); //Third dim is RGB
        
//        int newN = (int) (img[0].length - (img[0].length/4));
        int newN = (img[0].length/2);
        /*
        // Simple test case, useful for debugging!
        int[] WHITE = {1,1,1};
        int[] BLACK = {0,0,0};
        img = new int[][][]{
            {BLACK, BLACK, BLACK, WHITE, BLACK, WHITE}, 
            {WHITE, WHITE, BLACK, WHITE, WHITE, WHITE}, 
            {WHITE, WHITE, WHITE, BLACK, BLACK, WHITE}
            };*/
        
        SeamCarver sm = new SeamCarver(img);
        sm.scaleImage(newN); // img[0].length * 4/6
        
        ImRead.writeImage(sm.image, outputFilename);
    }
    
    /**
     * Create a new seam carver, initializing its fields.
     * @param img
     */
    public SeamCarver(int[][][] img) {
        width = img[0].length;
        height = img.length;
        this.image = img;
        energies = new int[height][width];
        seams = new int[height][width];
        backs = new int[height][width];
    }

    /**
     * Fill up the seams and backs based on the energies.
     */
    public void calculateSeams() {
        // Copy top row of seams to backs
        seams = new int[height][width];
        System.arraycopy(energies[0], 0, seams[0], 0, width);
        // Flow down using Dynamic Programming
        int[] previousRow = seams[0];
        for (int y=1; y<height; y+=1) {
            for (int x=0; x<width; x+=1) {
                // Might be the parent
                int minLocation = x;
                int minEnergy = previousRow[x];
                // Might be left-parent
                if (x > 0) {
                    if (previousRow[x-1] <= minEnergy) {
                        minLocation = x-1;
                        minEnergy = previousRow[x-1];
                    }
                }
                // Might be right-parent
                if (x+1 < width-1) {
                    if (previousRow[x+1] < minEnergy) {
                        minLocation = x+1;
                        minEnergy = previousRow[x+1];
                    }
                }
                // Increase energy here
                seams[y][x] += minEnergy + energies[y][x];
                backs[y][x] = minLocation;
            }
            previousRow = seams[y];
        }
    }

    /**
     * Determine the path through the lowest energy seam.
     */
    public int[] backtraceSeam() {
        // Find position of smallest value in bottom row
        int y = height-1;
        int minIndex = argMin(seams[y]);
        int[] path = new int[height];
        for (; y>=0; y-=1) {
            path[y] = minIndex;
            minIndex = backs[y][minIndex];
        }
        return path;
    }

    /**
     * Find the lowest energy seam using dynamic programming.
     */
    public int[] findLowestEnergySeam() {
        calculateSeams();
        return backtraceSeam();
    }

    /**
     * Calculate the energy of every pixel in the image.
     */
    public void calculateAllEnergies() {
        for (int y=0; y<height; y+=1) {
            for (int x=0; x<width; x+=1) {
                energies[y][x] = calculateEnergy(x, y);
            }
        }
    }

    /**
     * Remove the lowest energy seam's pixels in every row of the image
     */
    public int[] removeLowestSeam() {
        int[] path = findLowestEnergySeam();
        int[][][] copied = new int[height][width-1][3];
        // Go through the old image
        for (int y=0; y<height; y+= 1) {
            int skipped = 0;
            for (int x=0; x<width; x+=1) {
                if (path[y] != x) {
                    copied[y][x-skipped] = image[y][x];
                } else {
                    skipped = 1;
                }
            }
        }
        width -= 1;
        image = copied;
        return path;
    }

    /**
     * Iteratively remove the lowest energy seam. 
     * @param newWidth
     */
    public void scaleImage(int newWidth) {
        for (int i=width; i>newWidth; i-=1) {
            // Uncomment these print methods to get better insight into
            // how the data looks when you run the program!
            //printGrid("Image", img);
            //printGrid("Energies", energies);
            calculateAllEnergies();
            int[] path = removeLowestSeam();
            //printGrid("Seams", seams);
            //printGrid("Backs", backs);
            //System.out.println(Arrays.toString(path));
            //printGrid("Modified Image", img);
        }
    }

    /**
     * Calculate the energy of the image at the given pixel based on its
     * neighbors.
     */
    public int calculateEnergy(int x, int y) {
        int maxX= width-1;
        int maxY = height-1;
        int xLeft = x > 0 ? x-1 : 0;
        int xRight = x<maxX ? x+1 : maxX;
        int yUp = y > 0 ? y-1 : 0;
        int yDown = y<maxY ? y+1 : maxY;
        int xDiff = squareDiff(this.image[y][xLeft], this.image[y][xRight]);
        int yDiff = squareDiff(this.image[yUp][x], this.image[yDown][x]);
        return xDiff + yDiff;
    }

    /**
     * Determine the index with the lowest value in the array.
     */
    public static int argMin(int[] array) {
        int minIndex = 0;
        int minValue = array[minIndex];
        for (int i =0; i < array.length; i+=1) {
            if (minValue > array[i]) {
                minIndex = i;
                minValue = array[i];
            }
        }
        return minIndex;
    }

    /**
     * Determine the square difference between the two RGB values.
     */
    public static int squareDiff(int[] v1, int[] v2) {
        int rDiff = v2[0]-v1[0];
        int gDiff = v2[1]-v1[1];
        int bDiff = v2[2]-v1[2];
        return rDiff*rDiff + gDiff*gDiff + bDiff*bDiff;
    }

    /**
     * Print out the given label and 2D array.
     */
    public static void printGrid(String label, int[][] values) {
        System.out.println(label+":");
        for (int y=0; y<values.length; y+=1) {
            System.out.print("  ");
            for (int x=0;x<values[y].length; x+=1) {
                System.out.print(values[y][x]+" ");
            }
            System.out.println();
        }
    }

    /**
     * Print out the given label and 3D array (only use W and B, instead
     * of actual innermost array values).
     */
    public static void printGrid(String label, int[][][] values) {
        System.out.println(label+":");
        for (int y=0; y<values.length; y+=1) {
            System.out.print("  ");
            for (int x=0;x<values[y].length; x+=1) {
                if (values[y][x][0] > 0) {
                    System.out.print("W ");                    
                } else {
                    System.out.print("B ");
                }
            }
            System.out.println();
        }
    }

}
