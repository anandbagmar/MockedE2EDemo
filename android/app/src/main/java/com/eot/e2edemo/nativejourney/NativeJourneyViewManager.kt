package com.eot.e2edemo.nativejourney

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

class NativeJourneyViewManager : SimpleViewManager<LinearLayout>() {
  override fun getName(): String = "NativeJourneyView"

  override fun createViewInstance(reactContext: ThemedReactContext): LinearLayout {
    val container = LinearLayout(reactContext).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Color.parseColor("#FFF8E7"))
      setPadding(dp(reactContext, 16))
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    container.addView(
      badge(
        reactContext,
        label = "AndroidX Native View",
        "#ef6c3d",
        "#fff0df",
      ),
    )
    container.addView(
      heading(
        reactContext,
        "A longer AndroidX native screen",
        24f,
      ),
    )
    container.addView(
      body(
        reactContext,
        "This section is rendered entirely with native AndroidX views so the workflow pauses for a full platform-native interlude before it returns to React Native.",
      ),
    )

    listOf(
      "Arrival board with a compact welcome note",
      "Agenda highlight with the next meeting milestone",
      "Room status card with a short readiness summary",
      "Speaker prep note with a few extra lines of copy",
      "Snack table reminder for the host team",
      "Exit checklist with a calmer wrap-up paragraph",
      "Final handoff card before the planner resumes",
    ).forEachIndexed { index, text ->
      container.addView(
        card(
          reactContext,
          "%02d".format(index + 1),
          text,
          ),
        )
    }

    return container
  }

  private fun heading(
    context: Context,
    label: String,
    sizeSp: Float,
  ): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor("#17324d"))
      setTypeface(typeface, Typeface.BOLD)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 10) }
      this.text = label
    }

  private fun body(context: Context, label: String): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor("#4d6378"))
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
      setLineSpacing(dp(context, 5).toFloat(), 1.0f)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 14) }
      this.text = label
    }

  private fun card(
    context: Context,
    index: String,
    text: String,
  ): View =
    LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dp(context, 18))
      setBackgroundColor(Color.WHITE)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 16) }

      addView(
        badge(
          context,
          label = index,
          "#ef6c3d",
          "#fff8f2",
          bottomMargin = dp(context, 10),
        ),
      )
      addView(
        heading(context, "Native card $index", 20f),
      )
      addView(
        body(context, text),
      )
    }

  private fun dp(context: Context, value: Int): Int =
    TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      value.toFloat(),
      context.resources.displayMetrics,
    ).toInt()

  private fun badge(
    context: Context,
    label: String,
    textColor: String,
    backgroundColor: String,
    bottomMargin: Int = dp(context, 12),
  ): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor(textColor))
      setBackgroundColor(Color.parseColor(backgroundColor))
      setTypeface(typeface, Typeface.BOLD)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
      letterSpacing = 0.08f
      setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6))
      this.text = label.uppercase()
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { this.bottomMargin = bottomMargin }
    }
}

class NativeHybridViewManager : SimpleViewManager<LinearLayout>() {
  override fun getName(): String = "NativeHybridView"

  override fun createViewInstance(reactContext: ThemedReactContext): LinearLayout {
    val root = LinearLayout(reactContext).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(Color.parseColor("#FFF8E7"))
      setPadding(dp(reactContext, 16))
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    root.addView(
      badge(
        reactContext,
        label = "AndroidX Hybrid Native Views",
        "#0f766e",
        "#e3f5ef",
      ),
    )
    root.addView(
      heading(
        reactContext,
        "A compact AndroidX hybrid native views screen",
        24f,
      ),
    )
    root.addView(
      body(
        reactContext,
        "This screen combines React Native layout with fixed native AndroidX views, and it does not scroll.",
      ),
    )

    root.addView(
      hybridRow(
        reactContext,
        "01",
        "Timeline summary",
        "A native callout for the current event stage.",
      ),
    )
    root.addView(
      hybridRow(
        reactContext,
        "02",
        "Navigation handoff",
        "A second native row that keeps the flow compact and easy to scan.",
      ),
    )

    root.addView(
      body(
        reactContext,
        "The next button sits below this native component so the app can continue back into the regular React Native flow.",
      ),
    )

    return root
  }

  private fun heading(
    context: Context,
    label: String,
    sizeSp: Float,
  ): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor("#17324d"))
      setTypeface(typeface, Typeface.BOLD)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 10) }
      this.text = label
    }

  private fun body(context: Context, label: String): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor("#4d6378"))
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
      setLineSpacing(dp(context, 5).toFloat(), 1.0f)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 14) }
      this.text = label
    }

  private fun hybridRow(
    context: Context,
    index: String,
    title: String,
    text: String,
  ): View =
    LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dp(context, 16))
      setBackgroundColor(Color.WHITE)
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { bottomMargin = dp(context, 14) }

      addView(
        badge(
          context,
          label = index,
          "#0f766e",
          "#eef8f5",
          bottomMargin = dp(context, 10),
        ),
      )
      addView(
        heading(context, title, 18f),
      )
      addView(
        body(context, text),
      )
    }

  private fun dp(context: Context, value: Int): Int =
    TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      value.toFloat(),
      context.resources.displayMetrics,
    ).toInt()

  private fun badge(
    context: Context,
    label: String,
    textColor: String,
    backgroundColor: String,
    bottomMargin: Int = dp(context, 12),
  ): TextView =
    TextView(context).apply {
      setTextColor(Color.parseColor(textColor))
      setBackgroundColor(Color.parseColor(backgroundColor))
      setTypeface(typeface, Typeface.BOLD)
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
      letterSpacing = 0.08f
      setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6))
      this.text = label.uppercase()
      layoutParams =
        LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { this.bottomMargin = bottomMargin }
    }
}
