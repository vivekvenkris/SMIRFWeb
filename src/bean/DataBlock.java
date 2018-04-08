package bean;

import org.w3c.dom.Node;

public class DataBlock {
	
	private String dbID;
	private String node;
	private String key;
	private Integer	 pwcID;
	private Integer maxSize;
	private Integer size;
	
	
	public DataBlock(String node, Node datablockXMlNode) {
		this.key = datablockXMlNode.getAttributes().getNamedItem("key").getNodeValue();
		this.pwcID = Integer.parseInt(datablockXMlNode.getAttributes().getNamedItem("pwc_id").getNodeValue());
		this.maxSize =  Integer.parseInt(datablockXMlNode.getAttributes().getNamedItem("size").getNodeValue());
		this.size =  Integer.parseInt(datablockXMlNode.getTextContent());
		this.node = node;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DataBlock  && ((DataBlock)obj).dbID.equals(this.dbID) && ((DataBlock)obj).node.equals(this.node)) return true;
		return false;
	}
	
	public boolean hasOverflown() {
		
		return this.size >= (this.maxSize -1);
		
	}
	
	@Override
	public String toString() {
		
		return 	(node + " " + key + " " + pwcID + "  " + maxSize +  " " + size);

	}
	public String getDbID() {
		return dbID;
	}
	public void setDbID(String dbID) {
		this.dbID = dbID;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Integer getPwcID() {
		return pwcID;
	}
	public void setPwcID(Integer pwcID) {
		this.pwcID = pwcID;
	}
	public Integer getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	
	
	
	

}
