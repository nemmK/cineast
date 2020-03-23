/**
 * based on https://github.com/argman/EAST
 */

package org.vitrivr.cineast.core.features.testing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnailator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Output;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.vitrivr.cineast.core.features.neuralnet.tf.GraphBuilder;
import org.vitrivr.cineast.core.util.LogHelper;

public class Boxing {

  /**
   * Find image files in test data path
   *
   * @return image
   */
  public static BufferedImage getBufferedImage(String imagePath) {
    BufferedImage img = null;
    try {
      img = ImageIO.read(new File(imagePath));
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
    int resizeHeight = height;
    int resizeWidth = width;
    float ratio = 1;

    BufferedImage image = img;

    if (Math.max(resizeHeight, resizeWidth) > maxLength) {
      if (resizeHeight > resizeWidth)
        ratio = (float) maxLength / resizeHeight;
      else
        ratio = (float) maxLength / resizeWidth;
    }

    resizeHeight = (int) (resizeHeight * ratio);
    resizeWidth = (int) (resizeWidth * ratio);

    if (resizeHeight % 32 != 0){
      resizeHeight = (((resizeHeight / 32)) - 1) * 32;
    }

    if (resizeWidth % 32 != 0) {
      resizeWidth = (((resizeWidth / 32)) - 1) * 32;
    }

    resizeHeight = Math.max(32, resizeHeight);
    resizeWidth = Math.max(32, resizeWidth);

    image = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TRANSLUCENT);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(img, 0, 0, resizeWidth, resizeHeight, null);
    g2.dispose();

    return image;
  }

  /**
   * Restore text boxes from score map and geo map
   * @param scoreMap INDArray
   * @param geoMap INDArray
   * @return merged text box
   */
  public static INDArray detect (INDArray scoreMap, INDArray geoMap){

    /*
    long [] scoreShape = scoreMap.shape();
    long [] geoShape = geoMap.shape();
    INDArray scoreNew = Nd4j.create(scoreShape[1],scoreShape[2]);
    INDArray geoNew = Nd4j.create(geoShape[1],geoShape[2],geoShape[3]);

     */

    // decrease shape of the array (scoremap - remove first and last dimension; geomap -remove first dimension)
    if (scoreMap.shape().length == 4){
      //squeeze removes dimension of size 1
      //scoreMap = scoreMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
      scoreMap = Nd4j.squeeze(scoreMap,0);
      scoreMap = Nd4j.squeeze(scoreMap,2);
      geoMap = Nd4j.squeeze(geoMap,0);
      //geoMap = geoMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
    }

    /*
    //debug - to check if dimension is correct
    long [] scoreSh = scoreMap.shape();
    long [] geoSh = geoMap.shape();

     */

    //count amount of values bigger than 0.8 = scroe_map_thresh
    int numberRows = 0;
    for(int row = 0; row < scoreMap.rows(); row ++) {
      for (int column = 0; column < scoreMap.columns(); column++) {
        if (scoreMap.getDouble(row, column) > 0.8) {
          numberRows++;
        }
      }
    }

    //save the index in a (x,2) (row, column - of original array) tuple of the values bigger than 0.8 for scoreMap
    INDArray temp = Nd4j.zeros(numberRows,2);
    int rowIndex = 0;
    for(int row = 0; row < scoreMap.rows(); row ++) {
      for (int column = 0; column < scoreMap.columns(); column++) {
        if (scoreMap.getDouble(row, column) > 0.8) {
          double[] value = {row,column};
          INDArray rowToInsert = Nd4j.create(value);
          temp.putRow(rowIndex, rowToInsert);
          rowIndex++;
        }
      }
    }

    //sort from smallest to largest value
    Nd4j.sortRows(temp,1,true);

    //restore this formula is given in the eval.py....
    //origin switches the two columns and multiplies it by 4
    INDArray origin = Nd4j.zeros(numberRows,2);
    origin.putColumn(0, temp.getColumn(1).dup());
    origin.putColumn(1, temp.getColumn(0).dup());
    origin.muli(4);

    //TODO geomap

    INDArray textBoxRestored = restoreRectangle(origin, geoMap);

    long [] shape = textBoxRestored.shape();
    INDArray boxes = Nd4j.zerosLike(Nd4j.create(shape[0],9));

    boxes.get(NDArrayIndex.all(), NDArrayIndex.interval(0,8)).assign(textBoxRestored.reshape(-1,8));

    INDArray row = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(0));
    INDArray column = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(1));

    INDArray index = origin.getColumn(0);
    INDArray indexOnde = origin.getColumn(1);

    //boxes[:, 8] = score_map[xy_text[:, 0], xy_text[:, 1]]
    //assign last row which should still be empty with the values of score map

    boxes = mergeQuadrangle(boxes);

    long [] boxesShape = boxes.shape();
    if (boxesShape[0] == 0)
      return null;

    //TODO fill the polygons
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
    //The first four columns of geometry
    int rankGeo = geometry.rank();

    INDArray d = geometry.get(NDArrayIndex.all(), NDArrayIndex.interval(0,4));
    //Only the fifth column of geometry (index 4 bc starting at 0)
    INDArray angle = geometry.get(NDArrayIndex.all(), NDArrayIndex.point(4));
    int number = d.rank();

    //counts amount of values bigger than 0.0
    int numberRows = 0;
    for(int i = 0; i < angle.rows(); i ++) {
      if (angle.getDouble(i, 0) >= 0.0) {
        numberRows++;
      }
    }

    //counts amount of values smaller than 0.0
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

    //fills the arrays if values bigger than 0.0
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

    //fills the arrays if values smaller than 0.0
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
        //TODO bigger than zero to do
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

      //TODO same like before newaxis
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

    String imagePath = "data/img_1.jpg";
    //String weightsPath = "east_checkpoint/model.ckpt-49491.data-00000-of-00001";

    /*Get the resized image */
    BufferedImage image = getBufferedImage(imagePath);
    image = resizeImage(image);

    float[][][][] floatImage = new float[1][image.getHeight()][image.getWidth()][3];

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        floatImage[0][y][x][0] = new Color(image.getRGB(x, y)).getRed() / 127.5f - 1.0f;
        floatImage[0][y][x][1] = new Color(image.getRGB(x, y)).getGreen() / 127.5f - 1.0f;
        floatImage[0][y][x][2] = new Color(image.getRGB(x, y)).getBlue() / 127.5f - 1.0f;
      }
    }

    /* Load model */
    SavedModelBundle load = SavedModelBundle.load("data/export", "serve");

    /* score = feature_fusion/Conv_7/Sigmoid:0, geometry = geometry = feature_fusion/concat_3:0; input layer = input_images:0 */
    List<Tensor<?>> tensor = load.session().runner()
        .feed("input_images:0", Tensor.create(floatImage, Float.class))
        .fetch("feature_fusion/Conv_7/Sigmoid:0").fetch("feature_fusion/concat_3:0")
        .run();

    //extract the two tensor we use
    Tensor scoreTensor = tensor.get(0);
    Tensor geometryTensor = tensor.get(1);

    //shape is needed to create the corresponding array
    long [] shapeScore = scoreTensor.shape();
    long [] shapeGeo = geometryTensor.shape();

    //create an array with the tensors shape
    float [][][][] scoremap = new float[(int)shapeScore[0]][(int)shapeScore[1]][(int)shapeScore[2]][(int)shapeScore[3]];
    float [][][][] geomap = new float[(int)shapeGeo[0]][(int)shapeGeo[1]][(int)shapeGeo[2]][(int)shapeGeo[3]];

    //copyTo() only copies into arrays
    scoreTensor.copyTo(scoremap);
    geometryTensor.copyTo(geomap);

    //convert the array to INDArray
    INDArray scoreIND = Nd4j.create(scoremap);
    INDArray geoIND = Nd4j.create(geomap);

    detect(scoreIND,geoIND);


  }
}