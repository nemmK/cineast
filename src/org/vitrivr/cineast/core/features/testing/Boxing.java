/**
 * based on https://github.com/argman/EAST
 */

package org.vitrivr.cineast.core.features.testing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.vitrivr.cineast.core.features.neuralnet.tf.GraphBuilder;
import org.vitrivr.cineast.core.util.LogHelper;

public class Boxing {

  /**
   * Find image files in test data path
   *
   * @return image
   */
  public static BufferedImage getBufferedImage() {
    BufferedImage img = null;
    try {
      //not correct path
      img = ImageIO.read(new File("data/img_3.jpg"));
    } catch (
        IOException e) {
      e.printStackTrace();
    }
    return img;
  }

  /**
   * Resize image to a size multiple of 32 which is required by the network
   * @param img BufferedImage to resize
   * @return resized BufferedImage
   */
  public static BufferedImage resizeImage(BufferedImage img) {
    //Image values
    int maxLength = 2400;
    int height = img.getHeight();
    int width = img.getWidth();
    float resizeHeight = height;
    float resizeWidth = width;
    float ratio = 1;

    if (Math.max(resizeHeight, resizeWidth) > maxLength) {
      if (resizeHeight > resizeWidth)
        ratio = maxLength / resizeHeight;
      else
        ratio = maxLength / resizeWidth;
    }

    resizeHeight = (int) (resizeHeight * ratio);
    resizeWidth = (int) (resizeWidth * ratio);

    if (resizeHeight % 32 != 0)
      resizeHeight = (resizeHeight / 32 - 1) * 32;

    if (resizeWidth % 32 != 0)
      resizeWidth = (resizeWidth / 32 - 1) * 32;

    resizeHeight = Math.max(32, resizeHeight);
    resizeWidth = Math.max(32, resizeWidth);

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

    //is the ratio needed?
    float ratioHeight = resizeHeight / height;
    float ratioWidth = resizeWidth / width;

    return img;
  }

  /**
   * Restore text boxes from score map and geo map
   * @param scoreMap INDArray
   * @param geoMap INDArray
   * @return merged text box
   */
  public static INDArray detect (INDArray scoreMap, INDArray geoMap){
    if (scoreMap.shape().length == 4){
      scoreMap = scoreMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
      geoMap = geoMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
    }

    //count amount of values bigger than 0.8
    int numberRows = 0;
    for(int i = 0; i < scoreMap.rows(); i ++) {
      for (int j = 0; j < scoreMap.columns(); j++) {
        if (scoreMap.getDouble(i, j) > 0.8) {
          numberRows++;
        }
      }
    }
    //save the index in a (x,2) (row, column - of original array) tupel of the values bigger than 0.8 for scoreMap
    INDArray temp = Nd4j.zeros(numberRows,2);
    int rowIndex = 0;
    for(int i = 0; i < scoreMap.rows(); i ++) {
      for (int j = 0; j < scoreMap.columns(); j++) {
        if (scoreMap.getDouble(i, j) > 0.8) {
          double[] value = {i,j};
          INDArray rowToInsert = Nd4j.create(value);
          temp.putRow(rowIndex, rowToInsert);
          rowIndex++;
        }
      }
    }

    //sort from smallest to largest value
    Nd4j.sortRows(temp,1,true);

    //restore
    INDArray origin = Nd4j.zeros(numberRows,2);
    origin.putColumn(0, temp.getColumn(1).dup());
    origin.putColumn(1, temp.getColumn(0).dup());
    origin.muli(4);

    //geoMap
    INDArray textBoxRestored = restoreRectangle(origin, geoMap);
    long [] shape = textBoxRestored.shape();
    INDArray boxes = Nd4j.zerosLike(Nd4j.create(shape[0],9));

    boxes.get(NDArrayIndex.all(), NDArrayIndex.interval(0,8)).assign(textBoxRestored.reshape(-1,8));

    INDArray row = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(0));
    INDArray column = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(1));

    //boxes[:, 8] = score_map[xy_text[:, 0], xy_text[:, 1]]
    //assign last row which should still be empty with the values of score map

    boxes = mergeQuadrangle(boxes);

    long [] boxesShape = boxes.shape();
    if (boxesShape[0] == 0)
      return null;

    //fill the polygons todo
    /*
    for i, box in enumerate(boxes):
        mask = np.zeros_like(score_map, dtype=np.uint8)
        cv2.fillPoly(mask, box[:8].reshape((-1, 4, 2)).astype(np.int32) // 4, 1)
        boxes[i, 8] = cv2.mean(score_map, mask)[0]
    boxes = boxes[boxes[:, 8] > box_thresh
     */
    for ( int i = 0 ; i < boxes.rows() ; i++){
      INDArray mask = Nd4j.zerosLike(scoreMap);

    }

    return boxes;
  }

  /**
   *
   * @param poly
   * @return
   */
  public static INDArray sortPolynom(INDArray poly){
    INDArray sumArray = poly.sum(1);
    //convert sumArray to 1D array and get index of Min
    int minAxis = 0;
    if (Math.abs(poly.getDouble(0,0)- poly.getDouble(1,0)) > Math.abs(poly.getDouble(0,1) - poly.getDouble(1,1)))
      return poly;
    else {
      //poly[[0, 3, 2, 1]];
      return poly ;
    }
  }

  /**
   *
   * @param origin INDArray
   * @param geometry INDArray
   * @return
   */
  public static INDArray restoreRectangle(INDArray origin, INDArray geometry){
    INDArray d = geometry.get(NDArrayIndex.all(), NDArrayIndex.interval(0,4));
    INDArray angle = geometry.get(NDArrayIndex.all(), NDArrayIndex.indices(4));

    int numberRows = 0;
    for(int i = 0; i < angle.rows(); i ++) {
      if (angle.getDouble(i, 0) >= 0.0) {
        numberRows++;
      }
    }
    int numberRowsOne = 0;
    for(int i = 0; i < angle.rows(); i ++) {
      if (angle.getDouble(i, 0) < 0.0) {
        numberRowsOne++;
      }
    }

    INDArray originZero = Nd4j.zeros(numberRows,origin.columns());
    INDArray dZero = Nd4j.zeros(numberRows,d.columns());
    INDArray angleZero = Nd4j.zeros(numberRows,angle.columns());
    INDArray originOne = Nd4j.zeros(numberRows,origin.columns());
    INDArray dOne = Nd4j.zeros(numberRowsOne,d.columns());
    INDArray angleOne = Nd4j.zeros(numberRowsOne,angle.columns());

    int rowIndex = 0;
    for(int i = 0; i < angle.rows(); i ++) {
        if (angle.getDouble(i, 0) >= 0.0) {
          INDArray rowToInsert = originZero.getRow(i);
          originZero.putRow(rowIndex, rowToInsert);
          dZero.putRow(rowIndex, rowToInsert);
          angleZero.putRow(rowIndex, rowToInsert);

          rowIndex++;
        }
    }

    rowIndex = 0;
    for(int i = 0; i < angle.rows(); i ++) {
      if (angle.getDouble(i, 0) < 0.0) {
        INDArray rowToInsert = originZero.getRow(i);
        originZero.putRow(rowIndex, rowToInsert);
        dZero.putRow(rowIndex, rowToInsert);
        angleZero.putRow(rowIndex, rowToInsert);

        rowIndex++;
      }
    }

    INDArray tempRotX = angleZero;
    //For rotate_matrixX to get cos/sin values
    for(int i = 0; i < tempRotX.rows(); i ++) {
      for (int j = 0; j < tempRotX.columns(); j++) {
        if( i == 0){
          tempRotX.put(i,j,Math.cos(tempRotX.getDouble(i,j)));
        }
        if ( i == 1){
          tempRotX.put(i,j,Math.sin(tempRotX.getDouble(i,j)));
        }

      }
    }

    INDArray tempRotY = angleZero;
    //For rotate_matrixY to get cos/sin values
    for(int i = 0; i < tempRotY.rows(); i ++) {
      for (int j = 0; j < tempRotY.columns(); j++) {
        //bigger than zero to do
        //calculation for AngleZero
        if( i == 0){
          tempRotY.put(i,j,Math.sin(tempRotY.getDouble(i,j))).neg();
        }
        if ( i == 1){
          tempRotY.put(i,j,Math.cos(tempRotY.getDouble(i,j)));
        }

      }
    }


    originOne = geometry;
    long [] originShape = originZero.shape();
    long [] originOneShape = originOne.shape();
    long [] dShape = dZero.shape();
    long [] dOneShape = dOne.shape();
    INDArray p3Origin;
    INDArray pZero;
    INDArray pOne;
    INDArray pTwo;
    INDArray pThree;


    //TODO transpose not correct
    if(originShape[0] > 0){
      INDArray p = Nd4j.create(dShape[0],10);
      p.putRow(0,Nd4j.zeros(dShape[0]));
      p.putRow(1,(dZero.getRow(0).add(dZero.getRow(2))).neg());
      p.putRow(2,dZero.getRow(1).add(dZero.getRow(3)));
      p.putRow(3,(dZero.getRow(0).add(dZero.getRow(2))).neg());
      p.putRow(4,dZero.getRow(1).add(dZero.getRow(3)));
      p.putRow(5,Nd4j.zeros(dShape[0]));
      p.putRow(6,Nd4j.zeros(dShape[0]));
      p.putRow(7,Nd4j.zeros(dShape[0]));
      p.putRow(8,dZero.getRow(3));
      p.putRow(9,dZero.getRow(2).neg());

      p.transpose().reshape(-1,5,2);

      INDArray rotateX = Nd4j.create(2,angle.columns());
      INDArray rotateY = Nd4j.create(2,angle.columns());

      rotateX.putRow(0,tempRotX.getRow(0));
      rotateX.putRow(1,tempRotX.getRow(1));
      rotateX.transpose();
      rotateX.repeat(1,5).reshape(-1,2,5).transpose();
      rotateY.putRow(0,tempRotY.getRow(0));
      rotateY.putRow(1,tempRotY.getRow(1));
      rotateY.transpose();
      rotateY.repeat(1,5).transpose();

      //TODO sum ist not correct
      INDArray tempX = Nd4j.sum(rotateX.mul(p),2);
      INDArray tempY = Nd4j.sum(rotateY.mul(p),2);

      //TODO what does the np.newaxis
      INDArray pRotateX = tempX;
      INDArray pRotateY = tempY;
      pRotateX = pRotateX.get(NDArrayIndex.all(), NDArrayIndex.all());
      pRotateY = pRotateY.get(NDArrayIndex.all(), NDArrayIndex.all());

      INDArray pRotate = Nd4j.concat(2,pRotateX,pRotateY);

      p3Origin = originZero.sub(pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(4),NDArrayIndex.all()));
      pZero = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(0),NDArrayIndex.all()).add(p3Origin);
      pOne = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(1),NDArrayIndex.all()).add(p3Origin);
      pTwo = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(2),NDArrayIndex.all()).add(p3Origin);
      pThree = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(3),NDArrayIndex.all()).add(p3Origin);

      //same like before newaxis
      pZero = Nd4j.concat(1, pZero,pOne,pTwo, pThree);
    }
    else {
      pZero = Nd4j.zeros(0,4,2);
    }

    if(originOneShape[0] < 0){
      INDArray p = Nd4j.create(dOneShape[0],10);
      p.putRow(0,(dOne.getRow(1).add(dOne.getRow(3))).neg());
      p.putRow(1,(dOne.getRow(0).add(dOne.getRow(2))).neg());
      p.putRow(2,Nd4j.zeros(dOneShape[0]));
      p.putRow(3,(dOne.getRow(0).add(dOne.getRow(2))).neg());
      p.putRow(4,Nd4j.zeros(dOneShape[0]));
      p.putRow(5,Nd4j.zeros(dOneShape[0]));
      p.putRow(6,(dOne.getRow(1).add(dOne.getRow(3))).neg());
      p.putRow(7,Nd4j.zeros(dOneShape[0]));
      p.putRow(8,dOne.getRow(1).neg());
      p.putRow(9,dZero.getRow(2).neg());

      p.transpose().reshape(-1,5,2);


      INDArray tempRotOneX = angleZero;
      //For rotate_matrixX to get cos/sin values
      for(int i = 0; i < tempRotOneX.rows(); i ++) {
        for (int j = 0; j < tempRotOneX.columns(); j++) {
          if( i == 0){
            tempRotOneX.put(i,j,Math.cos( - tempRotOneX.getDouble(i,j)));
          }
          if ( i == 1){
            tempRotOneX.put(i,j,Math.sin(- tempRotOneX.getDouble(i,j))).neg();
          }

        }
      }

      INDArray tempRotOneY = angleZero;
      //For rotate_matrixY to get cos/sin values
      for(int i = 0; i < tempRotOneY.rows(); i ++) {
        for (int j = 0; j < tempRotOneY.columns(); j++) {
          //bigger than zero to do
          //calculation for AngleZero
          if( i == 0){
            tempRotOneY.put(i,j,Math.sin( - tempRotOneY.getDouble(i,j))).neg();
          }
          if ( i == 1){
            tempRotOneY.put(i,j,Math.cos( - tempRotOneY.getDouble(i,j)));
          }

        }
      }

      INDArray rotateX = Nd4j.create(2,angle.columns());
      INDArray rotateY = Nd4j.create(2,angle.columns());

      rotateX.putRow(0,tempRotOneX.getRow(0));
      rotateX.putRow(1,tempRotOneX.getRow(1));
      rotateX.transpose();
      rotateX.repeat(1,5).reshape(-1,2,5).transpose();

      rotateY.putRow(0,tempRotOneY.getRow(0));
      rotateY.putRow(1,tempRotOneY.getRow(1));
      rotateY.transpose();
      rotateY.repeat(1,5).transpose();

      //TODO sum ist not correct
      INDArray tempX = Nd4j.sum(rotateX.mul(p),2);
      INDArray tempY = Nd4j.sum(rotateY.mul(p),2);

      //TODO what does the np.newaxis
      INDArray pRotateX = tempX;
      INDArray pRotateY = tempY;
      pRotateX = pRotateX.get(NDArrayIndex.all(), NDArrayIndex.all());
      pRotateY = pRotateY.get(NDArrayIndex.all(), NDArrayIndex.all());

      INDArray pRotate = Nd4j.concat(2,pRotateX,pRotateY);

      p3Origin = originZero.sub(pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(4),NDArrayIndex.all()));
      pZero = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(0),NDArrayIndex.all()).add(p3Origin);
      pOne = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(1),NDArrayIndex.all()).add(p3Origin);
      pTwo = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(2),NDArrayIndex.all()).add(p3Origin);
      pThree = pRotate.get(NDArrayIndex.all(),NDArrayIndex.indices(3),NDArrayIndex.all()).add(p3Origin);

      //same like before newaxis
      pOne = Nd4j.concat(1, pZero,pOne,pTwo, pThree);
    }
    else {
      pOne = Nd4j.zeros(0,4,2);
    }

    return Nd4j.concat(0,pZero,pOne);
  }

  //In the other class
  public static INDArray mergeQuadrangle(INDArray boxes){

    return null;
  }

  public static void main(String[] args) {

    //debugging
    INDArray origin = Nd4j.rand(5,3);
    INDArray geometry = Nd4j.rand(5,3);
    //restoreRectangle(origin,geometry);
    detect(origin,geometry);
    INDArray poly = Nd4j.rand(5,6);
    //sortPolynom(poly);


    //Tensorflow
    final Graph preprocessingGraph;
    final Session preprocessingSession;
    final String imageOutName;
    final Graph boxingGraph;
    final Session boxingSession;

    byte[] GRAPH_DEF = new byte[0];
    try {
      GRAPH_DEF = Files
          .readAllBytes((Paths.get("resources/Boxing.pb"))); //TODO change path
    } catch (IOException e) {
      throw new RuntimeException(
          "could not load graph for Boxing: " + LogHelper.getStackTrace(e));
    }
    boxingGraph = new Graph();
    boxingGraph.importGraphDef(GRAPH_DEF);
    boxingSession = new Session(boxingGraph);

    preprocessingGraph = new Graph();

    GraphBuilder graphBuilder = new GraphBuilder(preprocessingGraph);

    Output<Float> imageFloat = graphBuilder.placeholder("T", Float.class);

    final int[] size = new int[]{416, 416};

    final Output<Float> output =

        graphBuilder.resizeBilinear( // Resize using bilinear interpolation
            graphBuilder.expandDims( // Increase the output tensors dimension
                imageFloat,
                graphBuilder.constant("make_batch", 0)),
            graphBuilder.constant("size", size)
        );

    imageOutName = output.op().name();

    preprocessingSession = new Session(preprocessingGraph);

  }
  //transform model in a .pb file


}
