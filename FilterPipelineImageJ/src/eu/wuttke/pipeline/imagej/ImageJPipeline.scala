package eu.wuttke.pipeline.imagej

import ij.IJ
import ij.ImagePlus
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
import ij.gui.Roi

object ImageJPipeline {

  def main(args: Array[String]) {
	  val file1 = "ex/file1.tif"
	  val file2 = "ex/file2.tif"
	  val img1 = new ImagePlus(file1)
	  val img2 = new ImagePlus(file2)
	
	  // ColorThresholder
	  val mask = makeMask(img1, 3)
	  new FileSaver(mask).saveAsPng("ex/file1_mask_01_thresh.png")
	  
	  // Blur
	  val gb = new GaussianBlur()
	  gb.run(mask.getProcessor())
	  new FileSaver(mask).saveAsPng("ex/file1_mask_02_blur.png")
	  
	  // Invert Mask
	  val bgMask = new ImagePlus("BG Mask", mask.getProcessor().duplicate()) 
	  IJ.run(bgMask, "Invert", "")
	  new FileSaver(bgMask).saveAsPng("ex/file1_mask_03_invert.png")

	  val ic = new ImageCalculator()
	  
	  // Apply img1
	  val img1_fg = ic.run("mul, create, float", img1, mask)
	  new FileSaver(img1_fg).saveAsPng("ex/file1_masked.png")
	  
	  // Apply inverted mask
	  val img1_bg = ic.run("mul, create, float", img1, bgMask)
	  new FileSaver(img1_bg).saveAsPng("ex/file1_background.png")
	  
	  // Apply img2
	  val img2_fg = ic.run("mul, create, float", img2, mask)
	  new FileSaver(img2_fg).saveAsPng("ex/file2_masked.png")
	  
	  // Apply inverted mask
	  val img2_bg = ic.run("mul, create, float", img2, bgMask)
	  new FileSaver(img2_bg).saveAsPng("ex/file2_background.png")

	  // measure
	  val fgVal2 = getMean(img2_fg)
	  val bgVal2 = getMean(img2_bg)
	  print("RED fore mean = " + fgVal2 + ", back mean = " + bgVal2 + "\n")

	  val fgVal1 = getMean(img1_fg)
	  val bgVal1 = getMean(img1_bg)
	  print("GREEN fore mean = " + fgVal1 + ", back mean = " + bgVal1 + "\n")
	  
	  val fgBg1 = fgVal1 / bgVal1
	  val fgBg2 = fgVal2 / bgVal2
	  print("RED quot = " + fgBg2 + ", GREEN quot = " + fgBg1 + ", TOTAL = " + fgBg2/fgBg1)
	  // sum statt mean, normalisieren auf sum der mask?
  }
  
  def getMean(img : ImagePlus) : Float = {
  	  val rt = new ResultsTable()
	  val ana = new Analyzer(img, Measurements.MEAN, rt)
	  ana.measure()
	  rt.getColumn(1)(0)
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
  
  
}