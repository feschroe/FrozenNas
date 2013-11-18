package org.frozennas;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MyS3ObjectSummary extends S3ObjectSummary {
	
	private String decryptedKey = "";
	private boolean isFolder = false;

	//constructor file
	public MyS3ObjectSummary(S3ObjectSummary object) {
		super();
		this.setBucketName(object.getBucketName());
		this.setETag(object.getETag());
		this.setKey(object.getKey());
		this.setLastModified(object.getLastModified());
		this.setOwner(object.getOwner());
		this.setSize(object.getSize());
		this.setStorageClass(object.getStorageClass());
		
		this.setDecryptedKey(object.getKey());
		isFolder = false;
	}
	
	//constructor folder
	public MyS3ObjectSummary(String commonprefix) {
		super();
		this.setKey(commonprefix);
		this.setSize(0);
		
		this.setDecryptedKey(commonprefix);
		isFolder = true;
	}
	
	
	public void setDecryptedKey(String key){
		decryptedKey = key;	
	}

	public String getDecryptedKey(){
		return decryptedKey;	
	}
	
	/*public void setIsFolder(boolean b){
		isFolder = b;
	}*/
	
	public boolean isFolder(){		
		return isFolder;
	}

}
