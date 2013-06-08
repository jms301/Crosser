package org.tearne.crosser

import sampler.math.StatisticsComponent
import org.tearne.crosser.scheme.Scheme
import java.nio.file.Paths
import java.nio.file.Files
import java.io.FileNotFoundException
import sampler.data.ParallelSampleBuilder
import org.tearne.crosser.util.AlleleCount
import sampler.io.CSVTableWriter
import sampler.data.Types._
import sampler.r.ScriptRunner
import org.tearne.crosser.cross.Cross
import org.tearne.crosser.plant.RootPlant
import scala.collection.immutable.ListMap
import sampler.data.Empirical._
import sampler.data.Empirical
import sampler.math.Probability
import sampler.math.Random
import sampler.data.EmpiricalMetricSubComponent
import org.tearne.crosser.cross.Crossable
import java.io.FileWriter
import java.nio.charset.Charset

object ExampleAppOne{
	
	def main(args: Array[String]) {
		val path = Paths.get(args(0)).toAbsolutePath
		if(!Files.exists(path)) throw new FileNotFoundException(path.toString())
		
		new Application(new Scheme(path))
	}
	
	class Application(scheme: Scheme) extends CrosserServiceFactory with EmpiricalMetricSubComponent with StatisticsComponent{
		val tolerance = scheme.tolerance
		val recombinationProb = scheme.recombinationProb
		val chunkSize = scheme.chunkSize

		def buildDistribution(plant: Crossable, donor: Crossable) = (plant, donor) match {
			case (cross: Cross, donor: RootPlant) => {
				val crossDistribution = crossSamplerService.getDistributionFor(cross)
				new ParallelSampleBuilder(chunkSize)(crossDistribution)(seq => {
					println("loop size = "+seq.size)
					metric.max(seq.take(seq.size - chunkSize).toEmpiricalSeq, seq.toEmpiricalSeq) < tolerance ||
					seq.size == 1e8
				})
				.map(_.alleleCount(donor).proportion).seq
			}
			case _ => throw new UnsupportedOperationException()
		}
		
		val requiredOutputs = scheme.outputTables
		val distributions = requiredOutputs.map{case (plant, donor) => 
			buildDistribution(plant, donor).toEmpiricalSeq
		}
		
		val plants = requiredOutputs.map(_._1.name)
		val contributionsFrom = requiredOutputs.map(_._2.name)
		val titles = (plants zip contributionsFrom).map{case (p,d) => p + ":" + d}
		
		def getQuantiles(quantile: Double) = distributions.map{dist => 
			dist.quantile(Probability(quantile))
		}
		val columns = Seq(
			new Column(titles, "Contribution"),
			new Column(getQuantiles(0.025), "2.5%"),
			new Column(getQuantiles(0.5), "Median"),
			new Column(getQuantiles(0.975), "97.5%")
		)
		new CSVTableWriter(Paths.get("confidence.csv"), true).apply(columns: _*)
		
		
		val toWrite = titles zip distributions.map(_.values) 
		val data = toWrite.map{case (title, samples) =>
			(Stream.continually(title) zip samples)
		}.flatten.unzip
		new CSVTableWriter(Paths.get("distributions.csv"), true).apply(
				new Column(data._1, "distribution"),
				new Column(data._2, "sample")
		)
		
		val rmd =
"""
Generated Report
			
This plot shows an (equal tailed) 95% credible interval around the median proportion of preferred variety in each cross.
```{r}
data = read.csv("confidence.csv")
melted = melt(data, id='Contribution')
ggplot(melted, aes(x=Contribution, y=value, group=variable, colour=variable)) +
	geom_line()
```

The next plot shows an approximation of the distributions themselves.	
```{r}			
data = read.csv("distributions.csv")
ggplot(data, aes(x=sample, colour=distribution)) +
	geom_freqpoly(binwidth=0.001)
```
"""
		val writer = Files.newBufferedWriter(Paths.get("myTest.Rmd"), Charset.forName("UTF-8"))
		writer.write(rmd)
		writer.close()
		
		val knit = 
"""
require(knitr)
require(markdown)
require(ggplot2)
require(reshape)

#opts_chunk$set(echo=FALSE, message=FALSE, results='hide'	)
knit('myTest.Rmd')
markdownToHTML('myTest.md', 'myTest.html', options=c('use_xhml'))
"""
		ScriptRunner(knit, Paths.get("knit.R").toAbsolutePath())
	}
}