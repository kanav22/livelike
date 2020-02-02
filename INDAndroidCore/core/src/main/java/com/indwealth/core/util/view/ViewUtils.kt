package com.indwealth.core.util.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.ParcelableSpan
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager
import com.indwealth.core.R
import com.indwealth.core.util.CoreUtils
import com.indwealth.core.util.openUrl
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import timber.log.Timber

object ViewUtils {
    object ViewPagerUtils {
        val FadeInOutTransformer = ViewPager.PageTransformer { page, pos ->
            if (pos <= -1.0F || pos >= 1.0F) {
                page.translationX = page.width * pos
                page.alpha = 0.0F
            } else if (pos == 0.0F) {
                page.translationX = page.width * pos
                page.alpha = 1.0F
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                page.translationX = page.width * -pos
                page.alpha = 1.0F - Math.abs(pos)
            }
        }
    }

    object EditTextUtils {
        abstract class DefaultTextWatcher : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
            }
        }
    }

    object TextViewUtils {

        fun smartLinkify(lifecycleOwner: LifecycleOwner, vararg textViews: TextView) {
            for (textView in textViews)
                smartLinkify(lifecycleOwner, textView)
        }

        fun smartLinkify(lifecycleOwner: LifecycleOwner, textView: TextView) {
            val weakRefTv = WeakReference(textView)
            val text = textView.text.toString()
            val content = SpannableString(text)

            val linkMovementMethod = LinkMovementMethod.getInstance()
            val listOfSpan = mutableListOf<ClickableSpan>()

            val spanEmail = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    weakRefTv.get()?.let {
                        CoreUtils.sendEmailToSupport(it.context)
                    }
                }
            }

            val spanCAMSEmail = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    weakRefTv.get()?.let {
                        CoreUtils.sendEmailToSupport(it.context)
                    }
                }
            }
            linkify(textView, textView.context.getString(R.string.support_email), content, spanEmail)
            linkify(textView, textView.context.getString(R.string.cams_support_email), content, spanCAMSEmail)
            listOfSpan.add(spanEmail)

            val spanTerms = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    weakRefTv.get()?.context?.openUrl("https://indwealth.in/terms-of-services")
                }
            }
            linkify(textView, "terms & conditions", content, spanTerms)
            listOfSpan.add(spanTerms)

            val spanCrif = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    weakRefTv.get()?.context?.openUrl("https://indwealth.in/crif-information")
                }
            }
            linkify(textView, "CRIF High Mark", content, spanCrif)
            listOfSpan.add(spanCrif)

            val spanPrivacy = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    weakRefTv.get()?.context?.openUrl("https://indwealth.in/privacy-policy")
                }
            }
            linkify(textView, "privacy policy", content, spanPrivacy)
            listOfSpan.add(spanPrivacy)

            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun clear() {
                    Timber.d("REMOVE SPAN ONDESTORYED")
                    listOfSpan.forEach {
                        textView.text = ""
                        content.removeSpan(it)
                        textView.movementMethod = null
                    }
                    listOfSpan.clear()
                }
            })

            textView.movementMethod = linkMovementMethod
            textView.text = content
        }

        fun linkify(textView: TextView, toFind: String, content: SpannableString, onclick: ClickableSpan) {
            val index = textView.text.toString().toLowerCase().indexOf(toFind.toLowerCase())
            if (index != -1)
                content.setSpan(onclick, index, index + toFind.length, 0)
        }

        fun underLineAndOnclick(textView: TextView, onclick: ClickableSpan) {
            val span = SpannableString(textView.text.toString())
            linkify(textView, textView.text.toString(), span, onclick)
            textView.movementMethod = LinkMovementMethod.getInstance()
            //        textView.setLinkTextColor(Color.WHITE);
            textView.text = span
        }

        fun underLineAndOnclickThisWord(textView: TextView, word: String, onclick: View.OnClickListener) {
            val span = SpannableString(textView.text.toString())
            linkify(textView, word, span, object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onclick.onClick(null)
                }
            })
            textView.movementMethod = LinkMovementMethod.getInstance()
            //        textView.setLinkTextColor(Color.WHITE);
            textView.text = span
        }

        fun highlightWords(textView: TextView, vararg words: String) {
            highlightWordsWithColor(textView, Color.WHITE, *words)
        }

        fun highlightWordsWithColor(textView: TextView, color: Int, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            return highlightWordsWithColor(textView, content, color, *words)
        }

        fun highlightWordsWithColorAndBold(textView: TextView, color: Int, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words) {
                setSpan(textView.text.toString(), word, false, content, ForegroundColorSpan(color))
                setSpan(textView.text.toString(), word, false, content, StyleSpan(Typeface.BOLD))
            }
            textView.text = content
            return content
        }

        fun makeWordsBigger(textView: TextView, relativeSize: Float, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words)
                setSpan(textView.text.toString(), word, false, content, RelativeSizeSpan(relativeSize))
            textView.text = content
            return content
        }

        fun makeWordsUnderLined(textView: TextView, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words)
                setSpan(textView.text.toString(), word, false, content, UnderlineSpan())
            textView.text = content
            return content
        }

        fun makeWordsUnderLined(text: String, vararg words: String): SpannableString {
            val content = SpannableString(text)
            for (word in words)
                setSpan(text, word, false, content, UnderlineSpan())
            return content
        }

        fun makeWordsBold(textView: TextView, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words)
                setSpan(textView.text.toString(), word, false, content, StyleSpan(Typeface.BOLD))
            textView.text = content
            return content
        }

        fun makeWordsBoldAndWhite(textView: TextView, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words)
                setSpan(textView.text.toString(), word, false, content, StyleSpan(Typeface.BOLD))
            highlightWordsWithColor(textView, content, Color.WHITE, *words)
            textView.text = content
            return content
        }

        fun makeWordsBoldAndBigger(textView: TextView, relativeSize: Float, vararg words: String): SpannableString {
            val text = textView.text.toString()
            val content = SpannableString(text)
            for (word in words) {
                setSpan(textView.text.toString(), word, false, content, RelativeSizeSpan(relativeSize))
                setSpan(textView.text.toString(), word, false, content, StyleSpan(Typeface.BOLD))
            }
            textView.text = content
            return content
        }

        fun highlightWordsWithColor(textView: TextView, content: SpannableString, color: Int, vararg words: String): SpannableString {
            for (word in words)
                setSpan(textView.text.toString(), word, false, content, ForegroundColorSpan(color))
            textView.text = content
            return content
        }

        fun highlightWords(content: CharSequence, vararg words: String): SpannableString {
            return highlightWords(content, false, *words)
        }

        fun highlightWords(content: CharSequence, caseSensitive: Boolean, vararg words: String): SpannableString {
            val span = SpannableString(content)
            for (word in words)
                setSpan(content.toString(), word, caseSensitive, span, ForegroundColorSpan(Color.WHITE))
            return span
        }

        internal fun setSpan(text: String, toFind: String, caseSensitive: Boolean, content: SpannableString, onclick: ParcelableSpan) {
            val index = (if (caseSensitive) text else text.toLowerCase()).indexOf(if (caseSensitive) toFind else toFind.toLowerCase())
            if (index != -1)
                content.setSpan(onclick, index, index + toFind.length, 0)
        }

        fun setTextColor(color: Int, vararg textViews: TextView) {
            for (textView in textViews)
                textView.setTextColor(color)
        }

        fun setListener(listener: View.OnClickListener, vararg views: View) {
            for (view in views)
                view.setOnClickListener(listener)
        }

        fun slideInOutOnClick(onClick: View, hideThis: View) {
            onClick.setOnClickListener {
                if (hideThis.visibility == View.GONE)
                    showView(hideThis)
                else
                    hideView(hideThis)
            }
        }

        fun showView(view: View) {
            view.visibility = View.VISIBLE
            val init = view.height
            //        view.setAlpha(0.0f);
            view.animate()
                .yBy((-init).toFloat())
                .y(0f)
                //                .alpha(1.0f)
                .setListener(null)
        }

        fun setVisibility(visibility: Int, vararg views: View) {
            for (view in views)
                view.visibility = visibility
        }

        fun hideView(view: View) {
            view.animate()
                .y(0f)
                //                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        view.visibility = View.GONE
                    }
                })
        }

        fun saveAsImageFile(context: Context, view: View): File? {
            view.isDrawingCacheEnabled = true
            val bitmap = view.drawingCache
            val file: File
            return try {
                file = File(context.cacheDir, "share.png")
                val ostream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, ostream)
                ostream.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}