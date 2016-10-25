package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Point{
	Double ra;
	Double dec;
	Double startFanBeam;
	Double startNS;
	Double endFanBeam;
	Double endNS;
	Map<Integer, List<Traversal>> traversalMap = new HashMap<Integer, List<Traversal>>();
	boolean uniq;
	
	
}