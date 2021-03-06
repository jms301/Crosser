package org.tearne.crosser.config

import org.specs2.mutable._
import org.specs2.specification.Scope
import java.nio.file.Paths
import org.specs2.matcher.TraversableBaseMatchers
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.io.File
import java.nio.file.Files
import org.tearne.crosser.cross._
import org.tearne.crosser.plant.Species
import org.tearne.crosser.plant.RootPlant

@RunWith(classOf[JUnitRunner])
class ConfigSpec extends Specification {
	
	"Example config" should {
		"have a name" in new GoodConfig{ 
			config.name must_== "MyCross"
		}
		"have correct crosses" in new GoodConfig {
			val crosses: Map[String, Cross] = config.crosses
			crosses.size must_== 3
			crosses("F1") must_== f1
			crosses("BC1") must_== bc1
		}
	}
	
	trait GoodConfig extends Scope {
		val path = Paths.get("src/test/resource/example.config")
		Files.exists(path) must beTrue
		val config = new CrosserConfig(path)
		
		//Expected objects
		val species: Species = Species(IndexedSeq(11,23,45,22,10,80,121))
		
		val prefVar = RootPlant("PreferedVariety", species)
		
		val donor1 = RootPlant("Donor1", species)
		val locus1_1 = Locus(donor1, 5, 50, "D1C1")
		val locus1_2 = Locus(donor1, 6, 60, "D1C2")
		
		val donor2 = RootPlant("Donor2", species)
		val locus2 = Locus(donor2, 1, 10, "D2C1")
		
		val f1 = Cross(
			donor1,
			donor2,
			HeterozygousProtocol(Set(locus1_1, locus1_2, locus2)),
			"F1"
		)
		
		val bc1 = Cross(
			f1,
			prefVar,
			HeterozygousProtocol(Set(locus1_1, locus1_2, locus2), Some(1)),
			"BC1"
		)
		
		val self = Cross(
			bc1,
			bc1,
			HomozygousProtocol(Set(locus1_1, locus1_2, locus2)),
			"Self"
		)
	}
}