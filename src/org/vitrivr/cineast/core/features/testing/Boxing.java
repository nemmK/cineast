/**
 * based on https://github.com/argman/EAST
 */

package org.vitrivr.cineast.core.features.testing;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Index;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import javax.imageio.ImageIO;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class Boxing {

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


  public static BufferedImage resizeImage(BufferedImage img) {
    int maxSizeLen = 2400;
    int height = img.getHeight();
    int width = img.getWidth();
    float resizeHeight = height;
    float resizeWidth = width;
    float ratio = 1;

    if (Math.max(resizeHeight, resizeWidth) > maxSizeLen) {
      if (resizeHeight > resizeWidth)
        ratio = maxSizeLen / resizeHeight;
      else
        ratio = maxSizeLen / resizeWidth;
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


  public static INDArray detect (INDArray scoreMap, INDArray geoMap){
    if (scoreMap.shape().length == 4){
      scoreMap = scoreMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
      geoMap = geoMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
    }

    //save the index in a (x,2) (row,column - of original array) tupel of the values bigger than 0.8 for scoreMap
    INDArray temp = Nd4j.zeros(0,2);
    int rowIndex = 0;
    for(int i = 0; i < scoreMap.rows(); i ++)
      for (int j = 0; j < scoreMap.columns(); j++){
        if (scoreMap.getDouble(i,j) > 0.8){
          double [] value = {i, scoreMap.getDouble(i,j)};
          INDArray rotToInsert = Nd4j.create(value);
          temp.putRow(rowIndex,rotToInsert);
          rowIndex++;
        }
      }


    //restore
    //TODO change input of method of textBoxResored
    INDArray textBoxRestored = restoreRectangle(temp, geoMap);
    long [] shape = textBoxRestored.shape();
    INDArray boxes = Nd4j.zerosLike(Nd4j.create(shape[0],9));
    boxes.get(NDArrayIndex.all(), NDArrayIndex.interval(0,8)).assign(textBoxRestored.reshape(-1,8));
    INDArray row = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(0));
    INDArray column = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(1));

    //TODO how to get indexes of array to assign?
    //boxes.get(NDArrayIndex.all(), NDArrayIndex.indices(8)).assign(scoreMap.get(row,column));

    long [] boxesShape = boxes.shape();
    if (boxesShape[0] == 0)
      return null;

    /*
    for i, box in enumerate(boxes):
        mask = np.zeros_like(score_map, dtype=np.uint8)
        cv2.fillPoly(mask, box[:8].reshape((-1, 4, 2)).astype(np.int32) // 4, 1)
        boxes[i, 8] = cv2.mean(score_map, mask)[0]
    boxes = boxes[boxes[:, 8] > box_thresh
     */
    for ( int i = 0 ; i < boxes.columns() ; i++){
      INDArray mask = Nd4j.zerosLike(scoreMap);

    }

    return boxes;
  }

  public static INDArray sortPolynom(INDArray poly){
    INDArray sumArray = poly.sum(1);
    //convert sumArray to 1D array and get index of Min
    int minAxis = 0;
    if (Math.abs(poly.getDouble(0,0)- poly.getDouble(1,0)) > Math.abs(poly.getDouble(0,1) - poly.getDouble(1,1)))
      return poly;
    else {
      poly[[0, 3, 2, 1]];
      return poly ;
    }
  }

  public static INDArray restoreRectangle(INDArray origin, INDArray geometry){
    INDArray d = geometry.get(NDArrayIndex.all(), NDArrayIndex.interval(0,4));
    INDArray angle = geometry.get(NDArrayIndex.all(), NDArrayIndex.indices(4));
    //TODO get array with entries bigger than zero
    INDArray originZero = geometry;
    INDArray dZero = geometry;
    long [] originShape = originZero.shape();
    long [] dShape = dZero.shape();
    if(originShape[0] > 0){
      }

    return d;
  }

  public static void main(String[] args) {

    //debugging
    INDArray origin = Nd4j.rand(5,3);
    INDArray geometry = Nd4j.rand(5,3);
    restoreRectangle(origin,geometry);

    INDArray poly = Nd4j.rand(5,6);
    sortPolynom(poly);


  }
  //transform model in a .pb file


}
