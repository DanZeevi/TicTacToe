package com.zdan.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.zdan.tictactoe.GameViewModel.GameStatusEnum
import com.zdan.tictactoe.GameViewModel.GameStatusEnum.*
import com.zdan.tictactoe.SquareTypeEnum.*
import com.zdan.tictactoe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    private lateinit var viewModel: GameViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVars()
    }

    private fun initVars() {

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                this.lifecycleOwner = this@MainActivity
                this.game = viewModel
            }
    }

    companion object {
        const val TAG = "MainActivity"

        @BindingAdapter("android:src")
        @JvmStatic
        fun setSquareImage(imageView: ImageView, type: SquareTypeEnum) {
            val imageId = when (type) {
                X -> R.drawable.ic_x
                O -> R.drawable.ic_o
                Empty -> R.drawable.empty
            }
            imageView.setImageResource(imageId)
        }

        @BindingAdapter("android:text")
        @JvmStatic
        fun setTitleText(textView: TextView, statusEnum: GameStatusEnum) {
            val textId = when (statusEnum) {
                GameOnX -> R.string.gameOnX
                GameOnO -> R.string.gameOnO
                WinByX ->  R.string.xWin
                WinByO ->  R.string.oWin
                Tie -> R.string.Tie
            }
            textView.text = textView.context.getString(textId)
        }

        @BindingAdapter("android:visibility")
        @JvmStatic
        fun setStrikeVisibility(view: View, isEqual: Boolean) {
            view.visibility =
            when (isEqual) {
                true -> View.VISIBLE
                else -> View.GONE
            }
        }
    }
}



