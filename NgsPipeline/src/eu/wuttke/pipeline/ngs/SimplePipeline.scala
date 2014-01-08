package eu.wuttke.pipeline.ngs

import org.broadinstitute.sting.queue.QScript
import org.broadinstitute.sting.queue.extensions.picard._
import org.broadinstitute.sting.queue.extensions.gatk._
import org.broadinstitute.sting.queue.util.Logging
import net.sf.samtools.SAMFileHeader.SortOrder
import net.sf.samtools.SAMFileReader.ValidationStringency
import org.broadinstitute.sting.gatk.phonehome.GATKRunReport.PhoneHomeOption
import org.broadinstitute.sting.gatk.walkers.genotyper.GenotypeLikelihoodsCalculationModel.GENOTYPING_MODE
import org.broadinstitute.sting.gatk.walkers.variantrecalibration.VariantRecalibratorArgumentCollection.Mode

class SimplePipeline extends QScript with Logging {

  @Input(doc = "Input descriptor file.", shortName = "I")
  var inputDescriptorFile: File = _

  @Input(doc = "The reference file for the bam files.", shortName = "R")
  var referenceFile: File = _

  @Input(doc = "The path to the binary of bwa", fullName = "path_to_bwa", shortName = "bwa")
  var bwaPath: File = _

  @Input(doc = "GATK key file", fullName = "gatk_key", shortName = "K")
  var gatkKey: File = _

  @Input(doc = "InDel reference Mills", fullName = "indel_mills")
  var indelMills: File = _
  
  @Input(doc = "InDel reference 1000G", fullName = "indel_1000g")
  var indel1000g: File = _

  @Input(doc = "dbSNP reference", fullName = "dbsnp")
  var dbSnp: File = _
  
  @Input(doc = "HapMap reference", fullName = "hapmap")
  var snpsHapMap : File = _

  @Input(doc = "SNPs reference Omni 2.5M", fullName = "omni")
  var snpsOmni : File = _
  
  @Input(doc = "SNPs reference 1000G", fullName = "omni")
  var snps1000g : File = _
  
  @Argument(doc = "Maximum number of cores.", shortName = "C", required = false)
  var nCores = 1

  @Argument(doc = "Maximum amount of memory (GB).", shortName = "M", required = false)
  var maxMemory = 4

  @Argument(doc = "Log dir", shortName = "L", required = false)
  val queueLogDir: String = "logs/"

  trait GatkCommonArguments extends CommandLineGATK {
    this.memoryLimit = SimplePipeline.this.maxMemory
    this.reference_sequence = SimplePipeline.this.referenceFile
    this.et = PhoneHomeOption.NO_ET
    this.K = gatkKey
    this.isIntermediate = true
  }

  trait ExternalCommonArgs extends CommandLineFunction {
    this.memoryLimit = SimplePipeline.this.maxMemory
    this.isIntermediate = true
  }

  def script() {
    val samples: List[InputDescriptor] = InputDescriptor.readDecriptorFile(inputDescriptorFile)
    logger.info("read %d samples" format samples.length)
    
    // sample-level QC
    val bamFiles = samples.map(doSampleLevelQc(_))
    
    // Haplotype calling
    val rawVcfFile = swapExt(bamFiles(0), ".reduced.bam", ".raw.vcf")
    add(MyHaplotypeCaller(bamFiles, rawVcfFile))
    
    // variant recalibration
    val recalibratedVcfFile = doVariantRecalibration(rawVcfFile)
  }

  def doSampleLevelQc(sample: InputDescriptor) : File = {
    val samFile = swapExt(sample.fastqFile1, ".fastq", ".sam")
    val bamFile = swapExt(samFile, ".sam", ".bam")
    val dedupBamFile = swapExt(samFile, ".sam", ".marked.bam")
    val dedupMetricsFile = swapExt(samFile, ".sam", ".marked.metrics.txt")
    val realignerTargetsFile = swapExt(samFile, ".sam", ".realigner-targets.txt")
    val realignedBamFile = swapExt(samFile, ".sam", ".realigned.bam")
    val recalibratedDataFile = swapExt(samFile, ".sam", ".recal_data.grp")
    val postRecalibratedDataFile = swapExt(samFile, ".sam", ".post_recal_data.grp")
    val recalibrationPlotFile = swapExt(samFile, ".sam", ".recalibration_plots.pdf")
    val recalibratedBamFile = swapExt(samFile, ".sam", ".recalibrated.bam")
    val reducedBamFile = swapExt(samFile, ".sam", ".reduced.bam")
    
    add(MyBwaMem(sample.fastqFile1, sample.fastqFile2, samFile, sample.readGroup),
      MySortSam(samFile, bamFile, SortOrder.coordinate),
      MyMarkDuplicates(bamFile, dedupBamFile, dedupMetricsFile),
      MyRealignerTargetCreator(dedupBamFile, realignerTargetsFile),
      MyIndelRealigner(dedupBamFile, realignerTargetsFile, realignedBamFile),
      MyBaseRecalibratorCountCovariatesBeforeRecalibration(realignedBamFile, recalibratedDataFile),
      MyBaseRecalibratorCountCovariatesAfterRecalibration(realignedBamFile, recalibratedDataFile, postRecalibratedDataFile),
      MyAnalyzeCovariates(recalibratedDataFile, postRecalibratedDataFile, recalibrationPlotFile),
      MyPrintRecalibratedReads(realignedBamFile, recalibratedDataFile, recalibratedBamFile),
      MyReduceReads(recalibratedBamFile, reducedBamFile))
      
    reducedBamFile
  }
  
  def doVariantRecalibration(rawVcfFile : File) : File = {
    val recalibratedSnpRawIndelVcfFile = swapExt(rawVcfFile, ".raw.vcf", ".filtered_snp_raw_indel.vcf")
    val recalibratedVcfFile = swapExt(rawVcfFile, ".raw.vcf", ".recal.vcf")
    val recalSnpsFile = swapExt(rawVcfFile, ".raw.vcf", ".snps.recal")
    val tranchesSnpsFile = swapExt(rawVcfFile, ".raw.vcf", ".snps.tranches")
    val plotSnpsFile = swapExt(rawVcfFile, ".raw.vcf", ".snps.plot.R")
    val recalIndelsFile = swapExt(rawVcfFile, ".raw.vcf", ".indels.recal")
    val tranchesIndelsFile = swapExt(rawVcfFile, ".raw.vcf", ".indels.tranches")
    val plotIndelsFile = swapExt(rawVcfFile, ".raw.vcf", ".indels.plot.R")

    add(MyVariantRecalibratorSnps(rawVcfFile, recalSnpsFile, tranchesSnpsFile, plotSnpsFile),
        MyApplyRecalibrationSnps(rawVcfFile, recalSnpsFile, tranchesSnpsFile, recalibratedSnpRawIndelVcfFile),
        MyVariantRecalibratorIndels(recalibratedSnpRawIndelVcfFile, recalIndelsFile, tranchesIndelsFile, plotIndelsFile),
        MyApplyRecalibrationIndels(recalibratedSnpRawIndelVcfFile, recalIndelsFile, tranchesIndelsFile, recalibratedVcfFile))

    recalibratedVcfFile
  }

  // BWA mem aligner, add read group information 
  case class MyBwaMem(inFastQ1: File, inFastQ2: File, outSam: File, readGroupInfo: ReadGroupInfo)
  extends CommandLineFunction with ExternalCommonArgs {
    @Input(doc = "FastQ file 1 to be aligned") var fastQ1 = inFastQ1
    @Input(doc = "FastQ file 2 to be aligned") var fastQ2 = inFastQ2
    @Output(doc = "output sam file") var sam = outSam

    this.analysisName = queueLogDir + outSam + ".bwa_mem"
    this.jobName = queueLogDir + outSam + ".bwa_mem"

    val readGroupInfoStr = "@RG\tID:" + readGroupInfo.id +
      "\tSM:" + readGroupInfo.sampleName +
      "\tPL:" + readGroupInfo.platform +
      "\tLB:" + readGroupInfo.library +
      "\tPU:" + readGroupInfo.platformUnit

    def commandLine = {
      bwaPath + " mem -M -R '" + readGroupInfoStr +
        "' -t " + nCores + " " + referenceFile + " " + fastQ1 + " " + fastQ2 + " > " + sam +
        " 2> " + new File(queueLogDir, "bwa_mem.log").getAbsolutePath()
    }
  }

  // Picard SortSam
  case class MySortSam(inSam: File, outBam: File, sortOrderP: SortOrder)
  extends SortSam with ExternalCommonArgs {
    this.input :+= inSam
    this.output = outBam
    this.sortOrder = sortOrderP
    this.analysisName = queueLogDir + outBam + ".sortSam"
    this.jobName = queueLogDir + outBam + ".sortSam"
    this.createIndex = true
    this.validationStringency = ValidationStringency.LENIENT
  }

  // Picard MarkDuplicates
  case class MyMarkDuplicates(inBam: File, outBam: File, metricsFile: File) 
  extends MarkDuplicates with ExternalCommonArgs {
    this.input :+= inBam
    this.output = outBam
    this.metrics = metricsFile
    this.memoryLimit = 16
    this.analysisName = queueLogDir + outBam + ".dedup"
    this.jobName = queueLogDir + outBam + ".dedup"
    this.createIndex = true
    this.validationStringency = ValidationStringency.LENIENT
  }

  // Realigner Target Creator
  case class MyRealignerTargetCreator(inBam: File, outTargets: File)
  extends RealignerTargetCreator with GatkCommonArguments {
    this.input_file = inBam :: Nil
    this.known = indelMills :: indel1000g :: Nil
    this.out = outTargets
    this.fix_misencoded_quality_scores = true
    this.log_to_file = queueLogDir + inBam + ".realigner-target-creator.log"
  }

  // Indel Realigner
  case class MyIndelRealigner(inBam: File, inTargets: File, outBam: File)
  extends IndelRealigner with GatkCommonArguments {
    this.input_file = inBam :: Nil
    this.out = outBam
    this.known = indelMills :: indel1000g :: Nil
    this.targetIntervals = inTargets
    this.fix_misencoded_quality_scores = true
    this.log_to_file = queueLogDir + inBam + ".indel-realigner.log"
  }

  // Base Recalibration: Count Covariates before BQSR
  case class MyBaseRecalibratorCountCovariatesBeforeRecalibration(inBam : File, outCovarFile : File)
  extends BaseRecalibrator with GatkCommonArguments {
	this.input_file = inBam :: Nil
    this.out = outCovarFile
    this.knownSites = dbSnp :: indelMills :: indel1000g :: Nil
    this.log_to_file = queueLogDir + inBam + ".recalibrator-before.log"
  }

  // Base Recalibration: Count Covariates after BQSR
  case class MyBaseRecalibratorCountCovariatesAfterRecalibration(inBam : File, inCovarFile : File, outRecalFile : File)
  extends BaseRecalibrator with GatkCommonArguments {
	this.input_file = inBam :: Nil
    this.out = outRecalFile
    this.knownSites = dbSnp :: indelMills :: indel1000g :: Nil
    this.BQSR = inCovarFile
    this.log_to_file = queueLogDir + inBam + ".recalibrator-after.log"
  }
  
  // Base Recalibration: Analyze Covariates
  case class MyAnalyzeCovariates(inBeforeCovarFile : File, inAfterCovarFile : File, outPlotFile : File)
  extends AnalyzeCovariates with GatkCommonArguments {
    this.before = inBeforeCovarFile
    this.after = inAfterCovarFile
    this.plots = outPlotFile
    this.isIntermediate = false
    this.log_to_file = queueLogDir + inBeforeCovarFile + ".recalibrator-plots.log"
  }
  
  // Base Recalibration: Print Reads with BQSR
  case class MyPrintRecalibratedReads(inBam : File, inCovarFile : File, outBam : File)
  extends PrintReads with GatkCommonArguments {
    this.input_file = inBam :: Nil
    this.out = outBam
    this.BQSR = inCovarFile
    this.log_to_file = queueLogDir + inBam + ".recalibrator-print.log"
  }
  
  // Reduce Reads (remove redundant reads)
  case class MyReduceReads(inBam : File, outBam : File)
  extends ReduceReads with GatkCommonArguments {
    this.input_file = inBam :: Nil
    this.out = outBam
  }
  
  // Haplotype Caller
  case class MyHaplotypeCaller(inBams : Seq[File], outVcf : File) 
  extends HaplotypeCaller with GatkCommonArguments {
    this.input_file = inBams
    this.genotyping_mode = GENOTYPING_MODE.DISCOVERY 
    this.stand_emit_conf = 10
    this.stand_call_conf = 30
    this.out = outVcf
  }

  // Variant Recalibration
  case class MyVariantRecalibratorSnps(inVcf : File, outRecalFile : File, outTranchesFile : File, outPlotRscriptFile : File)
  extends VariantRecalibrator with GatkCommonArguments {
    this.input_file = inVcf :: Nil    
    this.an = "DP" :: "QD" :: "FS" :: "MQRankSum" :: "ReadPosRankSum" :: Nil
    this.mode = Mode.SNP
    this.tranche = "100.0" :: "99.9" :: "99.0" :: "90.0" :: Nil
    this.recal_file = outRecalFile
    this.tranches_file = outTranchesFile
    this.rscript_file = outPlotRscriptFile
    
    override def commandLine() =
      super.commandLine +  
      			" -resource:hapmap,known=false,training=true,truth=true,prior=15.0 " + snpsHapMap  +
    		  	" -resource:omni,known=false,training=true,truth=true,prior=12.0 " + snpsOmni + 
    		  	" -resource:1000G,known=false,training=true,truth=false,prior=10.0 " + snps1000g +
    		  	" -resource:dbsnp,known=true,training=false,truth=false,prior=2.0 " + dbSnp 
    		  		
  }
  
  case class MyApplyRecalibrationSnps(inVcf : File, inRecalFile : File, inTranchesFile : File, outVcf : File)
  extends ApplyRecalibration with GatkCommonArguments {
    this.input_file = inVcf :: Nil
    this.mode = Mode.SNP
    this.ts_filter_level = 99.0
    this.recal_file = inRecalFile
    this.tranches_file = inTranchesFile
    this.out = outVcf     
  }
  
  case class MyVariantRecalibratorIndels(inVcf : File, outRecalFile : File, outTranchesFile : File, outPlotRscriptFile : File)
  extends VariantRecalibrator with GatkCommonArguments {
    this.input_file = inVcf :: Nil 
    this.an = "DP" :: "FS" :: "MQRankSum" :: "ReadPosRankSum" :: Nil
    this.mode = Mode.INDEL
    this.tranche = "100.0" :: "99.9" :: "99.0" :: "90.0" :: Nil
    this.maxGaussians = 4
    this.recal_file = outRecalFile
    this.tranches_file = outTranchesFile
    this.rscript_file = outPlotRscriptFile
    
    override def commandLine() =
      super.commandLine +
       	" -resource:mills,known=true,training=true,truth=true,prior=12.0 " + indelMills
    
  }
  
  case class MyApplyRecalibrationIndels(inVcf : File, inRecalFile : File, inTranchesFile : File, outVcf : File)
  extends ApplyRecalibration with GatkCommonArguments {
    this.input_file = inVcf :: Nil
    this.mode = Mode.INDEL
    this.ts_filter_level = 99.0
    this.recal_file = inRecalFile
    this.tranches_file = inTranchesFile
    this.out = outVcf
    this.isIntermediate = false
  }
   
}