package org.vitrivr.cineast.core.features.testing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.vitrivr.cineast.core.util.LogHelper;

public class Recognition {

  public static String recognize (String imagePath, String WeightsPath, String CharDictPath, String ordMapDictPath){

    BufferedImage image = getBufferedImage(imagePath);

    boolean isVis = true;
    int heightNew = 32;
    int scaleRate = heightNew / image.getHeight();
    int widthNew  = scaleRate * image.getWidth();

    if (widthNew <= 100)
      widthNew = 100;

    image = resizeImage(image, widthNew, heightNew);

    BufferedImage visImage = image;

    int[][] pixels = new int[widthNew][heightNew];

    for( int i = 0; i < widthNew; i++ )
      for( int j = 0; j < heightNew; j++ )
        pixels[i][j] = image.getRGB( i, j );


    // .pb file not there yet, code from YOLO
    byte[] GRAPH_DEF = new byte[0];
    try {
      GRAPH_DEF = Files
          .readAllBytes((Paths.get("resources/restoring/restoring.pb")));
    } catch (IOException e) {
      throw new RuntimeException(
          "could not load graph for recognition: " + LogHelper.getStackTrace(e));
    }
    Graph graph = new Graph();
    graph.importGraphDef(GRAPH_DEF);
    Session session = new Session(graph);



    Operation inputdata = graph.opBuilder("Placeholder", "input")
        .setAttr("dtype", DataType.fromClass(Float.class))
        .build();


    //load model (from stackover. not adapted to this code yet)
    SavedModelBundle load = SavedModelBundle.load("resources/restoring/restoring.pb", "serve");
    Graph graphTwo = load.graph();
    Tensor result = load.session().runner()
        .feed("myInput", tensorInput)
        .fetch("myOutput")
        .run().get(0);

    /*
    float[][] resultArray;
    try (Graph g = load.graph()) {
      try (Session s = load.session();
          Tensor result = s.runner().feed("data", data).fetch("prediction").run().get(0)) {
        resultArray = result.copyTo(new float[10][1]);
      }
    }
    load.close();
    //return resultArray;
    */

    return "";
  }

  /**
   * Find image files in test data path
   *
   * @return image
   */
  public static BufferedImage getBufferedImage(String imagePath) {
    BufferedImage img = null;
    try {
      //not correct path
      img = ImageIO.read(new File(imagePath));
    } catch (
        IOException e) {
      e.printStackTrace();
    }
    return img;
  }

  /**
   * Resize the image to usable size
   * @param img BufferedImage
   * @param resizeWidth int
   * @param resizeHeight int
   * @return BufferedImage
   */
  public static BufferedImage resizeImage (BufferedImage img, int resizeWidth, int resizeHeight){
    // creates output image
    BufferedImage outputImage = new BufferedImage((int) resizeWidth,
        (int) resizeHeight, img.getType());

    // scales the input image to the output image
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(img, 0, 0, (int) resizeWidth, (int) resizeHeight, null);
    g2d.dispose();

    // writes to output file
    try {
      ImageIO.write(outputImage, "jpg", new File("data/outputImg.jpg"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    return outputImage;
  }



}

