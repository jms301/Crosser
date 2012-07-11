package org.tearne.crosser.plant

import org.tearne.crosser.plant.exception.ChromosomeException

case class Chromosome(left: Tid, right: Tid) {
	if(left.alleles.size != right.alleles.size) throw new ChromosomeException("Chromosomes not of same length: %d != %d".format(left.alleles.size, right.alleles.size))
	
	val size: Int = left.alleles.size
	def proportionOf(plant: RootPlant): Double = {
		def numberInTid(t: Tid): Int = {
			t.alleles.foldLeft(0){(acc,a) => acc + (if(a == plant) 1 else 0)}
		}
		println((numberInTid(left) + numberInTid(right)))
		(numberInTid(left) + numberInTid(right))/(2.0 * size)
	}
}