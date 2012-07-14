package org.tearne.crosser.plant

import org.tearne.crosser.cross.Crossable
import org.tearne.crosser.plant.exception.PlantException
import org.tearne.crosser.distribution.ChromosomeBank
import org.tearne.crosser.util.Random

sealed trait ConcretePlant extends Crossable{
	val chromosomes: IndexedSeq[Chromosome]
}

case class RootPlant(val name: String, val species: Species) extends ConcretePlant{
	val chromosomes = species.buildChromosomesFrom(this)

	def sample(cBank: ChromosomeBank, rnd: Random): ConcretePlant = throw new UnsupportedOperationException("TODO")
	
	override def toString() = name
	def canEqual(other: Any) = other.isInstanceOf[RootPlant]
	override def equals(other: Any) = {
		other match {
			case that: RootPlant => 
				(that canEqual this) &&
				name == that.name && 	
				species == that.species
		}
	}
}

case class Plant(val name: String, val chromosomes: IndexedSeq[Chromosome], val species: Species) extends ConcretePlant {
	
	def sample(cBank: ChromosomeBank, rnd: Random): ConcretePlant = throw new UnsupportedOperationException("TODO")
	
	if(species.cMLengths != chromosomes.map(_.size))
		throw new PlantException("Chromosomes for plant %s do not match species (%s)".format(name, species))
}