package com.zdan.tictactoe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zdan.tictactoe.GameViewModel.GameStatusEnum.*
import com.zdan.tictactoe.SquareTypeEnum.*
import com.zdan.tictactoe.WinnerFormEnum.*
import java.lang.RuntimeException
import kotlin.collections.ArrayList

class GameViewModel :  ViewModel(){

    val grid = Grid()

    lateinit var currentTurn: SquareTypeEnum

    private val _winnerFormLiveData = MutableLiveData<WinnerFormEnum>()
    val winnerFormLiveData : LiveData<WinnerFormEnum>
        get() = _winnerFormLiveData

    private var _gameStatusLiveData = MutableLiveData<GameStatusEnum>()
    val gameStatusLiveData : LiveData<GameStatusEnum>
        get() = _gameStatusLiveData

    init {
        newGame()
    }

    /**
     * reset #grid #currentTurn #_gameStatusLiveData #_indicesWinnerLiveData #_winnerFormLiveData
     */
    fun newGame() {
        grid.init()
        currentTurn = SquareTypeEnum.init()
        _gameStatusLiveData.value = GameStatusEnum.init()
        _winnerFormLiveData.value = None
    }

    /***
     *   checks if turn is valid at square index and perform turn
     *   @return false if turn invalid
     *   updates #_gameStatusLiveData for tie or win
     *   @param index
     ****/
    fun turn(index: Int) : Boolean {
        Log.d(TAG, "onClick $index")
        if (gameStatusLiveData.value == GameOnX || gameStatusLiveData.value == GameOnO) {
            if (grid.turn(currentTurn, index)) {
                when {
                    checkWin() -> {
                        _gameStatusLiveData.value = when (grid.getSquare(index)) {
                            X -> WinByX
                            O -> WinByO
                            else -> throw RuntimeException("illegal win occured")
                        }
                    }
                    checkTie() -> _gameStatusLiveData.value = Tie
                    else -> {
                        nextTurn()
                    }
                }
                return true
            }
        }
        return false
    }

    /**
     * update game to next turn
     * #currenTurn and #_gameStatusLiveData
     * @throws RuntimeException if #currentTurn equals Empty
     */
    private fun nextTurn() {
        currentTurn = currentTurn.nextTurn()
        _gameStatusLiveData.value = when (currentTurn) {
            X -> GameOnX
            O -> GameOnO
            Empty -> throw RuntimeException("illegal turn")
        }
    }

    /***
     * @returns true if game is a tie
     * that is if all squares are not empty
     * assuming #checkWin() called before
     ***/
    private fun checkTie() : Boolean {
        return (0 until Grid.NUM_OF_SQUARES).none { grid.getSquare(it) == Empty }
    }

    /***
     * @returns true if there is a win in game
     * that is if there is a strike either vertical, horizontal or diagonal of same type
     ***/
    private fun checkWin(): Boolean {
        Log.d(TAG, "Board: ${grid.toString()}")

        _winnerFormLiveData.value =
            when {
                // horizontal strike
                checkSquaresMatch(grid.getHorizontalLine(0)) -> Horizontal0
                checkSquaresMatch(grid.getHorizontalLine(1)) -> Horizontal1
                checkSquaresMatch(grid.getHorizontalLine(2)) -> Horizontal2
                // vertical strike
                checkSquaresMatch(grid.getVerticalLine(0)) -> Vertical0
                checkSquaresMatch(grid.getVerticalLine(1)) -> Vertical1
                checkSquaresMatch(grid.getVerticalLine(2)) -> Vertical2
                // diagonals
                checkSquaresMatch(grid.getMainDiagonal()) -> DiagonalMain
                checkSquaresMatch(grid.getInverseDiagonal()) -> DiagonalInverse
                // none
                else -> None.also { return false }
            }
        return true
    }

    /***
     *  @returns true if squares of given #indices match and are not empty
     *  else - false
     *  @param indices
     ***/
    private fun checkSquaresMatch(indices: java.util.ArrayList<Int>) : Boolean {
        if (grid.getSquare(indices[0]) == grid.getSquare(indices[1]) &&
            grid.getSquare(indices[0]) == grid.getSquare(indices[2])
            && grid.getSquare(indices[0]) != Empty) {
//            _indicesWinnerLiveData.value = indices
            return true
        }
        return false
    }

    enum class GameStatusEnum {
        GameOnX,
        GameOnO,
        WinByX,
        WinByO,
        Tie;

        companion object {
            fun init(): GameStatusEnum = GameOnX
        }

    }

    companion object {
        const val TAG = "Game"
    }
}

class Grid {

    private val _gridLiveData = MutableLiveData<ArrayList<SquareTypeEnum>>()
    val gridLiveData : LiveData<ArrayList<SquareTypeEnum>>
        get() = _gridLiveData

    fun init() {
        // initialize grid - empty all squares in grid
        val grid = ArrayList<SquareTypeEnum>(NUM_OF_SQUARES)
        grid.clear()
        for (index in 0 until NUM_OF_SQUARES) {
            grid.add(Empty)
        }
        _gridLiveData.value = grid
    }

    /**
     * play turn of #type at #index
     * @return true if turn was played and false if turn is invalid
     * @param type whose turn it is
     * @param index where is played
     * @throws RuntimeException if #index out of bounds or #type not empty
     */
    fun turn(type: SquareTypeEnum, index: Int): Boolean {
        Log.d(TAG, "turn $type in $index")
        // check input validity or throw exception
        when {
            index >= NUM_OF_SQUARES || index < 0 -> {
                throw RuntimeException("index out of bounds")
            }
            type == Empty -> {
                throw RuntimeException("type not X or O")
            }
        }
        // populate empty square of index according to turn type
        if (getSquare(index) == Empty) {
            setSquare(index, type)
            return true
        }
        return false
    }
    /***
     * @returns square of given #index
     * @throws RuntimeException if index out of bounds
     */
    fun getSquare(index: Int): SquareTypeEnum? = gridLiveData.value?.get(index)
        .apply {
            when {
                index < 0 || index >= NUM_OF_SQUARES -> throw RuntimeException("illegal index")
            }
        }

    /***
     *  set square by #index and #type
     *  and updates grid
     *  @param index
     *  @param type
     ***/
    private fun setSquare(index: Int, type: SquareTypeEnum) {
        Log.d(TAG, "set square $index $type")
        _gridLiveData.value?.set(index, type)
        val tempBoard = gridLiveData.value
        _gridLiveData.value = tempBoard
    }

    /***
     * @returns array of indices for a horizontal line (0, 1, 2) etc.
     * @param index defines number of line (0-2)
     * @throws RuntimeException if index illegal
     ***/
    fun getHorizontalLine(index: Int) : ArrayList<Int> {
        return ArrayList<Int>(BOARD_DIMENSION)
            .also {
                if (index < 0 || index >= BOARD_DIMENSION) {
                    throw RuntimeException("illegal index")
                }
                for (i in 0 until BOARD_DIMENSION) {
                    it += i + (index * BOARD_DIMENSION)
                }
            }
    }

    /***
     * @returns array of indices for a vertical line (0, 3, 6) etc.
     * @param index defines number of line (0-2)
     * @throws RuntimeException if index illegal
     ***/
    fun getVerticalLine(index: Int)= ArrayList<Int>()
        .also {
            if (index < 0 || index >= BOARD_DIMENSION) {
                throw RuntimeException("illegal index")
            }
            for (i in 0 until BOARD_DIMENSION) {
                it += index + (i * BOARD_DIMENSION)
            }
        }

    /***
     * returns array of indices for main diagonal (0, 4, 8)
     ***/
    fun getMainDiagonal()  = ArrayList<Int>()
        .apply {
            for (i in 0 until BOARD_DIMENSION) {
                this += i * (4)
            }
        }

    /***
     * returns array of indices for inverse diagonal (2, 4, 6)
     ***/
    fun getInverseDiagonal() = ArrayList<Int>()
        .apply {for (i in 0 until BOARD_DIMENSION) {
            this += 2 + (i * 2)
        }
        }

    companion object {

        const val NUM_OF_SQUARES = 9
        const val BOARD_DIMENSION = 3
        const val TAG = "Board"
    }
}

enum class WinnerFormEnum {
    Horizontal0, Horizontal1, Horizontal2,
    Vertical0, Vertical1, Vertical2,
    DiagonalMain, DiagonalInverse,
    None
}

enum class SquareTypeEnum {
    Empty, X, O;

    fun nextTurn() = when (this) {
        X -> O
        O -> X
        Empty -> throw RuntimeException("unknown state")
    }

    companion object {
        fun init(): SquareTypeEnum = X
    }
}