package de.schwerin.stoppCoronaDE.model.exceptions

/**
 * Exception indicates some data populating has failed.
 */
class DataPopulationFailedException(vararg throwables: Throwable) : Throwable(throwables.contentToString())