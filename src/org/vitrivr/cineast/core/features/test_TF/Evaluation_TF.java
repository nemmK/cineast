package org.vitrivr.cineast.core.features.test_TF;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import javax.imageio.ImageIO;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.Condition;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;

public class Evaluation_TF {

  public BufferedImage getBufferedImage() {
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

  //resize image to a size multiple of 32 which is required by the network
  //returns the resized image
  public BufferedImage resizeImage(BufferedImage img) {
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
    //not correct path
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

  //restore text boxes from score map and geo map
  //void not correct type
  public INDArray detect (INDArray scoreMap, INDArray geoMap, Timer timter){
    if (scoreMap.shape().length == 4){
      scoreMap = scoreMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
      geoMap = geoMap.get(NDArrayIndex.indices(0), NDArrayIndex.all(),NDArrayIndex.all(),NDArrayIndex.indices(0));
    }

    //TODO get array of entries bigger than 0.8 and store them in an array (sorted)
    INDArray temp = Nd4j.zeros(3,5);

    //restore
    INDArray textBoxRestored = restoreRectangle(temp, geoMap);
    long [] shape = textBoxRestored.shape();
    INDArray boxes = Nd4j.zerosLike(Nd4j.create(shape[0],9));
    boxes.get(NDArrayIndex.all(), NDArrayIndex.interval(0,8)).assign(textBoxRestored.reshape(-1,8));
    INDArray row = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(0));
    INDArray column = temp.get(NDArrayIndex.all(), NDArrayIndex.indices(1));

    //TODO how to get indexes of array to assign?
    //boxes.get(NDArrayIndex.all(), NDArrayIndex.indices(8)).assign(scoreMap.get(row,column));

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

  public INDArray restoreRectangle(INDArray origin, INDArray geometry){
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


}
