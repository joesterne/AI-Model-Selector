package com.example.data

import kotlinx.coroutines.flow.Flow

class ProgramRepository(private val programDao: ProgramDao) {
    val allPrograms: Flow<List<ProgramEntity>> = programDao.getAllPrograms()

    suspend fun insert(program: ProgramEntity) = programDao.insertProgram(program)

    suspend fun deleteById(id: Int) = programDao.deleteProgramById(id)
}
