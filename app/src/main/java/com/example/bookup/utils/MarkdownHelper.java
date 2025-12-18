package com.example.bookup.utils;

import android.content.Context;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

/**
 * Helper class for rendering Markdown text in the application.
 */
public class MarkdownHelper {
    private static final String TAG = "MarkdownHelper";
    private static volatile Markwon markwon;

    public MarkdownHelper() {
        // Private constructor to prevent instantiation
    }

    @NonNull
    public static Markwon getInstance(@NonNull Context context) {
        if (markwon == null) {
            synchronized (MarkdownHelper.class) {
                if (markwon == null) {
                    markwon = createMarkwonInstance(context);
                }
            }
        }
        return markwon;
    }

    @NonNull
    private static Markwon createMarkwonInstance(@NonNull Context context) {
        try {
            return Markwon.builder(context)
                    .usePlugin(HtmlPlugin.create())
                    .usePlugin(GlideImagesPlugin.create(context))
                    .build();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Markwon with plugins, falling back to basic instance", e);
            return Markwon.create(context);
        }
    }

    @NonNull
    public static Spanned renderMarkdown(@NonNull Context context, @Nullable String markdown) {
        return getInstance(context).toMarkdown(markdown != null ? markdown : "");
    }

    public static void setMarkdown(@NonNull Context context, @NonNull TextView textView, 
                                 @Nullable String markdown) {
        getInstance(context).setMarkdown(textView, markdown != null ? markdown : "");
    }
}