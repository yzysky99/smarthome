package com.android.smarthome.image;

public class ImageMessage {
	private int allLength;//总长度
	private int imageNameLength;//图片名字长度
	private long imageLength;//图片长度
	private String imageName;//图片名字
	private byte[] image;//图片
	public int getAllLength() {
		return allLength;
	}
	public void setAllLength(int allLength) {
		this.allLength = allLength;
	}
	public int getImageNameLength() {
		return imageNameLength;
	}
	public void setImageNameLength(int imageNameLength) {
		this.imageNameLength = imageNameLength;
	}
	public long getImageLength() {
		return imageLength;
	}
	public void setImageLength(long imageLength) {
		this.imageLength = imageLength;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public byte[] getImage() {
		return image;
	}
	
	public void setImage(byte[] image) {
		this.image = image;
	}


}
