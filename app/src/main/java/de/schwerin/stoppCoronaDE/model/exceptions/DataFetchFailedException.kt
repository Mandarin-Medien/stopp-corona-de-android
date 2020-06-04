package de.schwerin.stoppCoronaDE.model.exceptions

/**
 * Exception indicates some fetching has failed.
 */
class DataFetchFailedException(vararg throwables: Throwable) : Throwable(throwables.contentToString())