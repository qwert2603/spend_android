package com.qwert2603.spenddemo.base_mvi.load_refresh

/**
 * Interface for model that can be used in [LRViewState].
 */
interface InitialModelHolder<in I> {
    /**
     * @param i initial model from [LRPartialChange.InitialModelLoaded].
     * @return new view state with changed initial model.
     */
    fun changeInitialModel(i: I): InitialModelHolder<I>
}