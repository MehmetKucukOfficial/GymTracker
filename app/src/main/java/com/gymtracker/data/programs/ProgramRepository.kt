package com.gymtracker.data.programs

import com.gymtracker.data.dao.ProgramDao
import com.gymtracker.data.entities.Program
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepository @Inject constructor(
    private val dao: ProgramDao
) {
    val programs = dao.getAll()

    fun observeProgram(programId: Long) = dao.observeById(programId)

    fun observeWorkouts(programId: Long) = dao.getWorkoutsForProgram(programId)

    suspend fun purchase(program: Program) {
        dao.updateProgram(program.copy(isPurchased = true))
    }
}
