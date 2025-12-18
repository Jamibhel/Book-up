package com.example.bookup.helpers;

import android.text.Spanned;
import android.text.Html;
import io.noties.markwon.Markwon;
import android.content.Context;

public class MarkdownHelper extends com.example.bookup.utils.MarkdownHelper {
    private Markwon markwon;

    public MarkdownHelper() {
    }

    public void init(Context context) {
        markwon = Markwon.create(context);
    }

    public Spanned parseMarkdown(String markdownText) {
        if (markwon != null) {
            return markwon.toMarkdown(markdownText);
        } else {
            return Html.fromHtml(markdownText);
        }
    }
}