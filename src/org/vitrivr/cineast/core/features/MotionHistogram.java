package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.abstracts.SubDivMotionHistogram;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.MathHelper;

public class MotionHistogram extends SubDivMotionHistogram {

  public MotionHistogram() {
    super("features_motionhistogram", "feature", MathHelper.SQRT2);
  }


  @Override
  public void processSegment(SegmentContainer shot) {
    if (!phandler.idExists(shot.getId())) {

      Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, shot.getPaths());

      double sum = pair.first.get(0);
      FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));

      persist(shot.getId(), sum, fv);
    }
  }

  protected void persist(String shotId, double sum, ReadableFloatVector fs) {
    PersistentTuple tuple = this.phandler.generateTuple(shotId, sum, fs);
    this.phandler.persist(tuple);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, sc.getPaths());

    FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
  }


  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    supply.get().createFeatureEntity("features_MotionHistogram", true);

  }

}
