package com.sergiocruz.matematica.Ui

import android.media.MediaPlayer
import android.view.View
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.helper.rotateYAnimation
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import it.sephiroth.android.library.xtooltip.Typefaces

object TooltipManager {

    private var tooltip: Tooltip? = null

    private var mp: MediaPlayer? = null

    fun showTooltipOn(anchor: View, message: String) {
        mp = MediaPlayer.create(anchor.context, R.raw.bling)
        mp?.setOnCompletionListener {
            mp?.reset()
            mp?.release()
            mp = null
        }
        mp?.start()

        anchor.rotateYAnimation()

        tooltip?.dismiss()
        tooltip = Tooltip.Builder(anchor.context)
                .anchor(anchor, 0, 0, false)
                .text(message)
                .styleId(R.style.ToolTipAltStyle)
                .typeface(Typefaces[anchor.context, "fonts/GillSans.ttc"])
                .maxWidth((anchor.context.resources.displayMetrics.density * 150).toInt())
                .arrow(true)
                .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME)
                .showDuration(3500)
                .overlay(true)
                .create()

        tooltip
                ?.doOnHidden {
                    tooltip = null
                    mp?.reset()
                    mp?.release()
                    mp = null
                }
                ?.show(anchor, Tooltip.Gravity.LEFT, true)
    }

}

