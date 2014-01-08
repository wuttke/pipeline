package eu.wuttke.pipeline.ngs

import org.broadinstitute.sting.queue.QScript
import org.broadinstitute.sting.queue.extensions.gatk._

class WholeExomePipeline extends QScript {

  qscript =>

  @Input(doc = "input BAM file - or list of BAM files", fullName = "input", shortName = "i", required = true)
  var input: Seq[File] = _

  @Input(doc = "Reference fasta file", fullName = "reference", shortName = "R", required = true)
  var reference: File = _

  @Input(doc = "Known indels VCF files", fullName = "indels", shortName = "I", required = true)
  var indelsVcf: Seq[File] = Nil

  trait CommonArguments extends CommandLineGATK {
    this.reference_sequence = qscript.reference
    this.memoryLimit = 4
  }

  def script() {
    for (bam <- qscript.input) {
      // local re-alignment around InDels
      val tc = new RealignerTargetCreator() with CommonArguments
      tc.input_file :+= bam
      tc.known = qscript.indelsVcf
      tc.out = swapExt(bam, ".bam", ".realigner-targets.list")
      tc.log = swapExt(bam, ".bam", ".realigner-targets.log")
      tc.allowPotentiallyMisencodedQuals = true
      add(tc)

      val ir = new IndelRealigner() with CommonArguments
      ir.known = qscript.indelsVcf
      ir.targetIntervals = tc.out
      ir.out = swapExt(bam, ".bam", ".realigned.bam")
      ir.log = swapExt(bam, ".bam", ".realigned.log")
      ir.allowPotentiallyMisencodedQuals = true
      add(ir)
    }
  }

}