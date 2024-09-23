import ij.*;
import ij.io.*;
import ij.plugin.*;
import ij.process.*;

import java.awt.Image;
import java.io.*;
import java.nio.*;

public class Koala_Cplx_Reader extends ImagePlus implements PlugIn {
    
    private LittleEndian LE = new LittleEndian();

    public void run(String arg) {  
        String path = getPath(arg);  
        if (null == path) return;  
        if (!parse(path)) return;  
        if (null == arg || 0 == arg.trim().length()) this.show(); // was opened by direct call to the plugin  
                          // not via HandleExtraFileTypes which would  
                          // have given a non-null arg.  
    }
  
    /** Accepts URLs as well. */  
    private String getPath(String arg) {  
    	if (null != arg) {  
            if (0 == arg.indexOf("http://")  
             || new File(arg).exists()) return arg;  
        }  
        // else, ask:  
        OpenDialog od = new OpenDialog("Choose a .bin file", null);  
        String dir = od.getDirectory();  
        if (null == dir) return null; // dialog was canceled  
        dir = dir.replace('\\', '/'); // Windows safe  
        if (!dir.endsWith("/")) dir += "/";  
        return dir + od.getFileName();  
    }  
  
    /** Opens URLs as well. */  
    private InputStream open(String path) throws Exception {  
        if (0 == path.indexOf("http://"))  
            return new java.net.URL(path).openStream();  
        return new FileInputStream(path);  
    }

    private boolean parse(String path) {
        // Open file and read header 
		// header size 1 byte
        byte[] buf = new byte[8];  
        InputStream stream;
        try {  
            stream = open(path);  
            stream.read(buf, 0, 8);
            stream.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }

        int width = LE.readInt(buf,0);  
        int height = LE.readInt(buf,4);
        int header_size = 8;
			
        // Build a new FileInfo object with all file format parameters and file data  
        FileInfo fi = new FileInfo();
        //header information is store in fileInfo debugInfo
        String header =  "Koala complex binary file header information" + System.lineSeparator();
        header += "Width : " + width + ", height : " + height + System.lineSeparator();
        fi.debugInfo = header;
        fi.fileType = fi.GRAY32_FLOAT;  
        fi.fileFormat = fi.RAW;
        int islash = path.lastIndexOf('/');  
        if (0 == path.indexOf("http://")) {  
            fi.url = path;  
        } else {  
            fi.directory = path.substring(0, islash+1);  
        }  
        fi.fileName = path.substring(islash+1);
		fi.description = "Koala binary file";
        fi.width = width;  
        fi.height = height;
		
        fi.nImages = 2;  
        fi.gapBetweenImages = 0;
        int endianness = 0;
        if (endianness == 0) fi.intelByteOrder = true; // little endian
        else fi.intelByteOrder = false; // big endian
        fi.whiteIsZero = false; // no inverted LUT  
        fi.longOffset = fi.offset = header_size; // header size, in bytes  
        
        //add header information to description field -> not used 
        fi.description = "Endianness : "+endianness;
  
        // Now make a new ImagePlus out of the FileInfo  
        // and integrate its data into this PlugIn, which is also an ImagePlus  
        try {  
            FileOpener fo = new FileOpener(fi);  
            ImagePlus imp = fo.openImage();
            ImageStack stack = imp.getStack();
            ImageProcessor realIp = stack.getProcessor(1);
            ImageProcessor imagIp = stack.getProcessor(2);
            int size = width * height;
            float[] realPix = (float[]) realIp.getPixels();
            float[] imagPix = (float[]) imagIp.getPixels();
            float[] ampPix = new float[size];
            float[] phasePix = new float[size];
            
            for (int i = 0; i < size; i++) {
                ampPix[i] = (float) Math.hypot((double)realPix[i],(double)imagPix[i]);
                phasePix[i] = (float) Math.atan2((double)imagPix[i],(double)realPix[i]);
            }
            ImageProcessor ampIp = new FloatProcessor(width, height, ampPix);
            ImageProcessor phaseIp = new FloatProcessor(width, height, phasePix);
            //stack.setProcessor(ampIp, 1);
            //stack.setProcessor(phaseIp, 2);
            ImagePlus amplitude = new ImagePlus(imp.getTitle()+"-Amplitude", ampIp);
            ImagePlus phase = new ImagePlus(imp.getTitle()+"-Phase", phaseIp);
            amplitude.show();
            phase.show();
            this.setStack(imp.getTitle(), imp.getStack());  
            Object obinfo = imp.getProperty("Info");  
            if (null != obinfo) this.setProperty("Info", obinfo);  
            this.setFileInfo(imp.getOriginalFileInfo());
            //imp.show();
            imp.close();
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        } 
        return true;  
    }
}