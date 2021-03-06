package org.vitrivr.cineast.core.data.query.containers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;

import georegression.struct.point.Point2D_F32;

public class ImageQueryContainer extends QueryContainer {

	private MultiImage img;
	private VideoFrame videoFrame;
	private ArrayList<SubtitleItem> subitem = new ArrayList<SubtitleItem>(1);
	private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
	private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
	private float relativeStart = 0, relativeEnd = 0;
	
	
	public ImageQueryContainer(MultiImage img){
		this.img = img;
	}
	
	@Override
	public MultiImage getAvgImg() {
		return this.img;
	}

	@Override
	public MultiImage getMedianImg() {
		return this.img;
	}

	@Override
	public VideoFrame getMostRepresentativeFrame() {
		if(this.videoFrame == null){
			int id = (getStart() + getEnd()) /2; 
			this.videoFrame = new VideoFrame(id, 0,this.img, new VideoDescriptor(25, 40, this.img.getWidth(), this.img.getHeight()));
		}
		return this.videoFrame;
	}

	@Override
	public int getStart() {
		return 0;
	}

	@Override
	public int getEnd() {
		return 0;
	}

	@Override
	public List<SubtitleItem> getSubtitleItems() {
		return this.subitem;
	}

	@Override
	public float getRelativeStart() {
		return relativeStart;
	}

	@Override
	public float getRelativeEnd() {
		return relativeEnd;
	}
	
	public void setRelativeStart(float relativeStart){
		this.relativeStart = relativeStart;
	}
	
	public void setRelativeEnd(float relativeEnd){
		this.relativeEnd = relativeEnd;
	}

	@Override
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
		return this.paths;
	}
	
	@Override
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
		return this.bgPaths;
	}

	@Override
  public List<VideoFrame> getVideoFrames() {
		ArrayList<VideoFrame> _return = new ArrayList<VideoFrame>(1);
		_return.add(this.videoFrame);
		return _return;
	}

	
	public void addPath(LinkedList<Point2D_F32> path){
		this.paths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
	}
	
	public void addBgPath(LinkedList<Point2D_F32> path){
		this.bgPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(0, path));
	}

}
