package eu.wuttke.pipeline.ngs

import java.io.File

class ReadGroupInfo(
  val id: String,
  val library: String,
  val platform: String,
  val platformUnit: String, // Barcode
  val sampleName: String) {}

class InputDescriptor {
  var readGroup: ReadGroupInfo = _
  var fastqFile1: File = _
  var fastqFile2: File = _
}

object InputDescriptor {
  def readDecriptorFile(fn: File): List[InputDescriptor] = {
    val src = io.Source.fromFile(fn)
    var res = List[InputDescriptor]()
    src.getLines.foreach(l => if (!l.startsWith("#")) {
      val fields = l.split("\t")
      if (fields.length != 7)
        throw new IllegalArgumentException("input file line does not contain 7 columns")

      val id = new InputDescriptor()
      id.readGroup = new ReadGroupInfo(fields(0), fields(1), fields(2), fields(3), fields(4))
      id.fastqFile1 = new File(fields(5));
      id.fastqFile2 = new File(fields(6));
      res = id :: res
    })
    res.reverse
  }
}