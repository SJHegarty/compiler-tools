package localgoat.image;

public interface ImageCodec<I extends Image>{
	public int type();
	public I decode(ImageData data);
	public I convert(Image image);
	public ImageData encode(I image);
}
