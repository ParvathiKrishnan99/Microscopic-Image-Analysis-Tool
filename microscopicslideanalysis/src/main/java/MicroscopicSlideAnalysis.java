import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * purpose of this class is to analyse the result of a set of microscopic images of algae growth over a period of time
 * Created by Parvathi Krishnan on 1/13/2017.
 */
public class MicroscopicSlideAnalysis {
    private static String folder = "C:\\work\\tools\\ImageMagick\\images";
    private static String outputFolder = "C:\\tmp\\output";
    private static String calculateThreasholdCommand = "C:\\work\\tools\\ImageMagick\\convert ${filename} -format \"%[fx:100*mean]\" info:";
    private static String convertToBlackAndWhiteCommand = "C:\\work\\tools\\ImageMagick\\convert ${filename} -type Grayscale ${output}";
    private static String inputToken = "${filename}";
    private static String outputToken = "${output}";
    private static Map<String, Double> namePixelsMap = new HashMap<String, Double>();

    /**
     * calculate the percentage of pixels above a threshold level
     * @param folder folder where input files are present
     * @throws Exception on error
     */
    public void calculatePixelsAboveThreasholdPercentageMap(final File folder)throws Exception{
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                calculatePixelsAboveThreasholdPercentageMap(fileEntry);
            } else {
                String filename = fileEntry.getName();
                if( filename.endsWith(".jpg") || filename.endsWith(".png")){
                    //create black and white picture
                    String blackAndWhiteImage = createBlackAndWhiteImage(filename);
                    //calculate the percentage of pixels above threshold
                    String pixelsAboveThreadsholdPercentage = executeCommand(getCommandReplacingToken(calculateThreasholdCommand, inputToken, blackAndWhiteImage));
                    if(pixelsAboveThreadsholdPercentage != null) {
                        namePixelsMap.put(filename, Double.valueOf(pixelsAboveThreadsholdPercentage));
                    }
                }
            }
        }
    }

    /**
     * Convert the color picture to black and white first, so that this can be used for any images
     * @param filename source file eg: source.png
     * @return black and white image filename eg: bw_source.png
     */
    private String createBlackAndWhiteImage(String filename){
        String blackAndWhiteImage = outputFolder + "\\bw_" + filename;
        String command = getCommandReplacingToken(convertToBlackAndWhiteCommand, inputToken, folder + "\\" + filename);
        command = getCommandReplacingToken(command, outputToken, blackAndWhiteImage);
        try {
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
        }
        catch(Exception e){
            blackAndWhiteImage = folder + "\\" + filename;
        }
        return blackAndWhiteImage;
    }

    private String executeCommand(String command) throws Exception{
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = reader.readLine();
        return line;
    }

    private String getCommandReplacingToken(String command, String replace, String filename) {
        return command.replace(replace, filename);
    }

    public static void main(String[] args) throws Exception{
        MicroscopicSlideAnalysis microscopicSlideAnalysis = new MicroscopicSlideAnalysis();
        microscopicSlideAnalysis.calculatePixelsAboveThreasholdPercentageMap(new File(folder));
        for(Map.Entry<String, Double> entry :  namePixelsMap.entrySet()){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}

