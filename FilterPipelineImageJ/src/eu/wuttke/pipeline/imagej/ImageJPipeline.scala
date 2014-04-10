package eu.wuttke.pipeline.imagej

import ij.IJ
import ij.ImagePlus
import ij.Prefs
import ij.gui.ImageRoi
import ij.io.FileSaver
import ij.measure.Measurements
import ij.measure.ResultsTable
import ij.plugin.ImageCalculator
import ij.plugin.filter.Analyzer
import ij.plugin.filter.GaussianBlur
import ij.process.ByteProcessor
import ij.process.ColorProcessor
import ij.process.ImageProcessor
import java.io.File

object ImageJPipeline {

  def main(args: Array[String]) {
      if (args.length != 2) {
        print("USAGE: ImageJPipeline file1 file2\n")
        return
      }
      
	  val file1 = args(0) //"ex/file1.tif"
	  val file2 = args(1) //"ex/file2.tif"
	  
	  val bn1 = file1.substring(file1.lastIndexOf("/") + 1, file1.lastIndexOf("."))
	  val bn2 = file2.substring(file2.lastIndexOf("/") + 1, file2.lastIndexOf("."))
	  
	  val img1 = new ImagePlus(file1)
	  val img2 = new ImagePlus(file2)
	  
	  val f1 = new File(file1)
	  if (!f1.exists()) {
	    print("file1 does not exist " + file1)
	    return
	  }
	  
	  if (img1.getProcessor() == null) {
	    print("failure loading img1 " + file1)
	    return
	  }
	  
	  if (img2.getProcessor() == null) {
	    print("failure loading img2 " + file2)
	    return
	  }
	  
	  Prefs.setTransparentIndex(0)
	  
	  // ColorThresholder
	  val colmask = makeMask(img1, 3)
	  new FileSaver(colmask).saveAsPng("out/" + bn1 + "_mask_01_thresh.png")
	  
	  // Blur
	  val gb = new GaussianBlur()
	  gb.run(colmask.getProcessor())
	  new FileSaver(colmask).saveAsPng("out/" + bn1 + "_mask_02_blur.png")

	  // Thresh again
  	  val bwmask = threshMask(colmask, 127)
	  new FileSaver(bwmask).saveAsPng("out/" + bn1 + "_mask_03_thresh.png")

	  // Invert Mask
	  var bgMask = new ImagePlus("BG Mask", bwmask.getProcessor().duplicate()) 
	  IJ.run(bgMask, "Invert", "")
	  new FileSaver(bgMask).saveAsPng("out/" + bn1 + "_mask_04_invert.png")

	  val ic = new ImageCalculator()
	  
	  // Apply img1
	  val img1_fg = ic.run("mul, create, float", img1, bwmask)
	  new FileSaver(img1_fg).saveAsPng("out/" + bn1 + "_masked.png")
	  
	  // Apply inverted mask
	  val img1_bg = ic.run("mul, create, float", img1, bgMask)
	  new FileSaver(img1_bg).saveAsPng("out/" + bn1 + "_background.png")
	  
	  // Apply img2
	  val img2_fg = ic.run("mul, create, float", img2, bwmask)
	  new FileSaver(img2_fg).saveAsPng("out/" + bn2 + "_masked.png")
	  
	  // Apply inverted mask
	  val img2_bg = ic.run("mul, create, float", img2, bgMask)
	  new FileSaver(img2_bg).saveAsPng("out/" + bn2 + "_background.png")

	  print("FG1\tBG1\tFG2\tBG2\tAREA\tFG1/BG1\tFG2/BG2\n")
	  val img1_fg_values = getMean2(img1_fg, bwmask)
	  val img1_bg_values = getMean2(img1_bg, bgMask)
	  val img2_fg_values = getMean2(img2_fg, bwmask)
	  val img2_bg_values = getMean2(img2_bg, bgMask)
	  
	  print(img1_fg_values._1 + "\t")
	  print(img1_bg_values._1 + "\t")
	  print(img2_fg_values._1 + "\t")
	  print(img2_bg_values._1 + "\t")
	  print(img1_fg_values._2 + "\t")
	  
	  val quot1 = img1_fg_values._1 / img1_bg_values._1
	  val quot2 = img2_fg_values._1 / img2_bg_values._1
	  
	  print(quot1 + "\t")
	  print(quot2 + "\n")
	  //print((quot1/quot2) + "\n")
  }
  
  def getMean2(img : ImagePlus, mask : ImagePlus) : Tuple2[Float, Int] = {
    val maskPixels = mask.getProcessor().getPixels().asInstanceOf[Array[Byte]]
    val imgPixels = img.getProcessor().getPixels().asInstanceOf[Array[Float]]
    var area : Int = 0
    var sum : Float = 0 
    for (i <- 0 until imgPixels.length) {
      if (maskPixels(i) != 0) {
        area += 1
        sum += imgPixels(i)        
      }
    }

    (sum/area, area)
  }
  
  // Color Thresholder w Brightness Threshold
  def makeMask(img : ImagePlus, brightnessThreshold : Byte) : ImagePlus = {
      val ip1 = img.getProcessor()
   	  val width = ip1.getWidth()
	  val height = ip1.getHeight()
	  val numPixels = width*height

	  val hSource = new Array[Byte](numPixels)
	  val sSource = new Array[Byte](numPixels)
	  val bSource = new Array[Byte](numPixels)
			
	  val cp = ip1.asInstanceOf[ColorProcessor]
	  cp.getHSB(hSource, sSource, bSource)

	  val maskProcessor = new ByteProcessor(width, height);  
      val pixels = maskProcessor.getPixels().asInstanceOf[Array[Byte]]
	  for (pixel <- 0 until numPixels) {
	    pixels(pixel) = if ((bSource(pixel) & 0x0ff) > brightnessThreshold) -1 else 0
	  }
      
	  new ImagePlus("Mask", maskProcessor)
  }
  
  def threshMask(img : ImagePlus, threshIndex : Byte) : ImagePlus = {
      val ip1 = img.getProcessor().asInstanceOf[ByteProcessor]
   	  val width = ip1.getWidth()
	  val height = ip1.getHeight()
	  val numPixels = width*height

	  val maskProcessor = new ByteProcessor(width, height);  
      val pixels = maskProcessor.getPixels().asInstanceOf[Array[Byte]]
      val source = ip1.getPixels().asInstanceOf[Array[Byte]]
	  for (pixel <- 0 until numPixels) {
	    pixels(pixel) = if ((source(pixel) & 0x0ff) > threshIndex) -1 else 0
	  }
      
	  new ImagePlus("Mask", maskProcessor)
  }
  
  
}