package localgoat.image.old.graphics2d.image;

import localgoat.image.Colour;
import localgoat.image.old.graphics2d.filter.MonoFunction;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.JFileChooser;


public class ImageReader{
	public static Image getImage(File f){
		return getImage(f, MonoFunction.NUL);
	}


	public static Image getImage(File f, MonoFunction function){
		try{
			InputStream fs = new FileInputStream(f);
			int bflen = 14;
			byte[] bf = new byte[bflen];
			fs.read(bf, 0, bflen);
			int bilen = 40;
			byte[] bi = new byte[bilen];
			fs.read(bi, 0, bilen);
			int nwidth = (bi[7] & 0xFF) << 24 | (bi[6] & 0xFF) << 16 | (bi[5] & 0xFF) << 8 | bi[4] & 0xFF;
			int nheight = (bi[11] & 0xFF) << 24 | (bi[10] & 0xFF) << 16 | (bi[9] & 0xFF) << 8 | bi[8] & 0xFF;
			int nbitcount = (bi[15] & 0xFF) << 8 | bi[14] & 0xFF;
			int nsizeimage = (bi[23] & 0xFF) << 24 | (bi[22] & 0xFF) << 16 | (bi[21] & 0xFF) << 8 | bi[20] & 0xFF;
			if(nbitcount == 24){
				int npad = nsizeimage / nheight - nwidth * 3;
				Image rv = new Image(nwidth, nheight);
				byte[] brgb = new byte[(nwidth + npad) * 3 * nheight];
				fs.read(brgb, 0, (nwidth + npad) * 3 * nheight);
				int nindex = 0;
				for(int j = nheight - 1; j >= 0; j--){
					for(int i = 0; i < nwidth; i++){
						rv.colours[i][j] = function.apply(
							Colour.toInt(
								brgb[nindex + 2] & 0xFF,
								brgb[nindex + 1] & 0xFF,
								brgb[nindex] & 0xFF
							)
						);
						nindex += 3;
					}
					nindex += npad;
				}
				fs.close();
				return rv;
			}
			fs.close();
		}
		catch(Exception e){

			e.printStackTrace();
		}
		return null;
	}

	public static Image getImage(MonoFunction function){
		JFileChooser chooser = new JFileChooser();
		File file = null;
		while(file == null){
			chooser.showOpenDialog(null);
			file = chooser.getSelectedFile();
		}
		return getImage(file, function);
	}

	public static Image getImage(){
		return getImage(MonoFunction.NUL);
	}
}
